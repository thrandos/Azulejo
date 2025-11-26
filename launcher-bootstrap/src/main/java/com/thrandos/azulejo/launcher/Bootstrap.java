/*
  ====================================================================
  AZULEJO

  Built for the Coastline server network
  Copyright (C) 2025
  Some base code copyright (C) 2010-2014 Albert Pham and contributors
  Please see LICENSE.txt for more information.
  ====================================================================
*/

package com.thrandos.azulejo.launcher;

import com.thrandos.azulejo.launcher.bootstrap.*;
import lombok.Getter;
import lombok.extern.java.Log;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.logging.Level;

import static com.thrandos.azulejo.launcher.bootstrap.SharedLocale.tr;

@Log
public class Bootstrap {

    private static final int BOOTSTRAP_VERSION = 1;

    @Getter private final File baseDir;
    @Getter private final boolean portable;
    @Getter private final File binariesDir;
    @Getter private final Properties properties;
    private final String[] originalArgs;

    public static void main(String[] args) throws Throwable {
        SimpleLogFormatter.configureGlobalLogger();
        SharedLocale.loadBundle("com.thrandos.azulejo.launcher.lang.Bootstrap", Locale.getDefault());

        boolean portable = isPortableMode();

        Bootstrap bootstrap = new Bootstrap(portable, args);
        try {
            bootstrap.cleanup();
            bootstrap.launch();
        } catch (Throwable t) {
            Bootstrap.log.log(Level.WARNING, "Error", t);
            Bootstrap.setSwingLookAndFeel();
            SwingHelper.showErrorDialog(null, tr("errors.bootstrapError"), tr("errorTitle"), t);
        }
    }

    public Bootstrap(boolean portable, String[] args) throws IOException {
        this.properties = BootstrapUtils.loadProperties(Bootstrap.class, "bootstrap.properties");

        File baseDir = portable ? new File(".") : getUserLauncherDir();

        this.baseDir = baseDir;
        this.portable = portable;
        this.binariesDir = new File(baseDir, "launcher");
        this.originalArgs = args;

        binariesDir.mkdirs();
    }

    public void cleanup() {
        File[] files = binariesDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().endsWith(".tmp");
            }
        });

        if (files != null) {
            for (File file : files) {
                file.delete();
            }
        }
    }

    public void launch() throws Throwable {
        File[] files = binariesDir.listFiles(new LauncherBinary.Filter());
        List<LauncherBinary> binaries = new ArrayList<LauncherBinary>();

        if (files != null) {
            for (File file : files) {
                Bootstrap.log.info("Found " + file.getAbsolutePath() + "...");
                binaries.add(new LauncherBinary(file));
            }
        }

        if (!binaries.isEmpty()) {
            launchExisting(binaries, true);
        } else {
            launchInitial();
        }
    }

    public void launchInitial() throws Exception {
        Bootstrap.log.info("Downloading the launcher...");
        // Prefer JavaFX UI if requested, else fall back to Swing
        boolean useJavaFX = shouldUseJavaFX(originalArgs);
        if (useJavaFX) {
            try {
                com.thrandos.azulejo.launcher.bootstrap.fx.BootstrapFxApp.launchWith(this);
                return;
            } catch (Throwable t) {
                Bootstrap.log.log(Level.WARNING, "Failed to start JavaFX bootstrapper UI, falling back to Swing", t);
            }
        }

        Thread thread = new Thread(new Downloader(this));
        thread.start();
    }

    private static boolean shouldUseJavaFX(String[] args) {
        // Honor --javafx flag like the main launcher
        if (args != null) {
            for (String arg : args) {
                if ("--javafx".equalsIgnoreCase(arg)) return true;
                if ("--swing".equalsIgnoreCase(arg)) return false;
            }
        }
        // default to JavaFX like the launcher does
        String uiProp = System.getProperty("launcher.ui", "javafx");
        return "javafx".equalsIgnoreCase(uiProp);
    }

    public void launchExisting(List<LauncherBinary> binaries, boolean redownload) throws Exception {
        Collections.sort(binaries);
        LauncherBinary working = null;
        Class<?> clazz = null;

        for (LauncherBinary binary : binaries) {
            File testFile = binary.getPath();
            try {
                testFile = binary.getExecutableJar();
                Bootstrap.log.info("Trying " + testFile.getAbsolutePath() + "...");
                clazz = load(testFile);
                Bootstrap.log.info("Launcher loaded successfully.");
                working = binary;
                break;
            } catch (Throwable t) {
                Bootstrap.log.log(Level.WARNING, "Failed to load " + testFile.getAbsoluteFile(), t);
            }
        }

        if (working != null) {
            for (LauncherBinary binary : binaries) {
                if (working != binary) {
                    log.info("Removing " + binary.getPath() + "...");
                    binary.remove();
                }
            }

            execute(clazz);
        } else {
            if (redownload) {
                launchInitial();
            } else {
                throw new IOException("Failed to find launchable .jar");
            }
        }
    }

    public void execute(Class<?> clazz) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        Method method = clazz.getDeclaredMethod("main", String[].class);
        String[] launcherArgs;

        if (portable) {
            launcherArgs = new String[] {
                    "--portable",
                    "--dir",
                    baseDir.getAbsolutePath(),
                    "--bootstrap-version",
                    String.valueOf(BOOTSTRAP_VERSION) };
        } else {
            launcherArgs = new String[] {
                    "--dir",
                    baseDir.getAbsolutePath(),
                    "--bootstrap-version",
                    String.valueOf(BOOTSTRAP_VERSION)  };
        }

        String[] args = new String[originalArgs.length + launcherArgs.length];
        System.arraycopy(launcherArgs, 0, args, 0, launcherArgs.length);
        System.arraycopy(originalArgs, 0, args, launcherArgs.length, originalArgs.length);

        log.info("Launching with arguments " + Arrays.toString(args));

    method.invoke(null, (Object) args);
    }

    public Class<?> load(File jarFile) throws MalformedURLException, ClassNotFoundException {
        URL[] urls = new URL[] { jarFile.toURI().toURL() };
        URLClassLoader child = new URLClassLoader(urls, this.getClass().getClassLoader());
    return Class.forName(getProperties().getProperty("launcherClass"), true, child);
    }

    public static void setSwingLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Ignore: failing to set look & feel is non-fatal
        }
    }

    private static File getFileChooseDefaultDir() {
        JFileChooser chooser = new JFileChooser();
        FileSystemView fsv = chooser.getFileSystemView();
        return fsv.getDefaultDirectory();
    }

    private File getUserLauncherDir() {
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("win")) {
            return new File(getFileChooseDefaultDir(), getProperties().getProperty("homeFolderWindows"));
        }

        File dotFolder = new File(System.getProperty("user.home"), getProperties().getProperty("homeFolder"));
        String xdgFolderName = getProperties().getProperty("homeFolderLinux");

        if (osName.contains("linux") && !dotFolder.exists() && xdgFolderName != null && !xdgFolderName.isEmpty()) {
            String xdgDataHome = System.getenv("XDG_DATA_HOME");
            if (xdgDataHome.isEmpty()) {
                xdgDataHome = System.getProperty("user.home") + "/.local/share";
            }

            return new File(xdgDataHome, xdgFolderName);
        }

        return dotFolder;
    }

    private static boolean isPortableMode() {
        return new File("portable.txt").exists();
    }


}
