/*
  ====================================================================
  AZULEJO

  Built for the Coastline server network
  Copyright (C) 2025
  Some base code copyright (C) 2010-2014 Albert Pham and contributors
  Please see LICENSE.txt for more information.
  ====================================================================
*/

package com.thrandos.azulejo.launcher.creator;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.thrandos.azulejo.launcher.Launcher;
import com.thrandos.azulejo.launcher.creator.controller.WelcomeController;
import com.thrandos.azulejo.launcher.creator.dialog.WelcomeDialog;
import com.thrandos.azulejo.launcher.creator.model.creator.CreatorConfig;
import com.thrandos.azulejo.launcher.creator.model.creator.RecentEntry;
import com.thrandos.azulejo.launcher.creator.model.creator.Workspace;
import com.thrandos.azulejo.launcher.persistence.Persistence;
import com.thrandos.azulejo.launcher.swing.SwingHelper;
import lombok.Getter;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executors;

public class Creator {

    @Getter private final File dataDir;
    @Getter private final CreatorConfig config;
    @Getter private final ListeningExecutorService executor = MoreExecutors.listeningDecorator(Executors.newCachedThreadPool());

    public Creator() {
        this.dataDir = getAppDataDir();
        this.config = Persistence.load(new File(dataDir, "config.json"), CreatorConfig.class);

        // Remove deleted workspaces
        List<RecentEntry> recentEntries = config.getRecentEntries();
        Iterator<RecentEntry> it = recentEntries.iterator();
        while (it.hasNext()) {
            RecentEntry workspace = it.next();
            if (!Workspace.getWorkspaceFile(workspace.getPath()).exists()) {
                it.remove();
            }
        }
    }

    public void showWelcome() {
        WelcomeDialog dialog = new WelcomeDialog();
        WelcomeController controller = new WelcomeController(dialog, this);
        controller.show();
    }

    private static File getFileChooseDefaultDir() {
        JFileChooser chooser = new JFileChooser();
        FileSystemView fsv = chooser.getFileSystemView();
        return fsv.getDefaultDirectory();
    }

    private static File getAppDataDir() {
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("win")) {
            return new File(getFileChooseDefaultDir(), "SKCraft Modpack Creator");
        } else {
            return new File(System.getProperty("user.home"), ".skcraftcreator");
        }
    }

    public static void main(String[] args) throws Exception {
        Launcher.setupLogger();
        System.setProperty("skcraftLauncher.killWithoutConfirm", "true");

        final Creator creator = new Creator();

        SwingUtilities.invokeAndWait(() -> {
            SwingHelper.setSwingProperties("Modpack Creator");
            SwingHelper.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

            try {
                creator.showWelcome();
            } catch (Exception e) {
                SwingHelper.showErrorDialog(null, "Failed to start the modpack creator program.", "Start Error", e);
            }
        });
    }

}
