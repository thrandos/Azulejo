/*
  ====================================================================
  AZULEJO

  Built for the Coastline server network
  Copyright (C) 2025-2026
  Some base code copyright (C) 2010-2014 Albert Pham and contributors
  Please see LICENSE.txt for more information.
  ====================================================================
*/


package com.thrandos.azulejo.launcher.selfupdate;

import com.thrandos.azulejo.concurrency.DefaultProgress;
import com.thrandos.azulejo.concurrency.ProgressObservable;
import com.thrandos.azulejo.launcher.Launcher;
import com.thrandos.azulejo.launcher.install.FileMover;
import com.thrandos.azulejo.launcher.install.Installer;
import com.thrandos.azulejo.launcher.util.SharedLocale;
import lombok.NonNull;

import java.io.File;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SelfUpdater implements Callable<File>, ProgressObservable {

    private final Launcher launcher;
    private final URL url;
    private final Installer installer;
    private ProgressObservable progress = new DefaultProgress(0, SharedLocale.tr("updater.updating"));

    public SelfUpdater(@NonNull Launcher launcher, @NonNull URL url) {
        this.launcher = launcher;
        this.url = url;
        this.installer = new Installer(launcher.getInstallerDir());
    }

    @Override
    public File call() throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        try {
            File dir = launcher.getLauncherBinariesDir();
            File file = new File(dir, System.currentTimeMillis() + ".jar");
            File tempFile = installer.getDownloader().download(url, "", 10000, "launcher.jar");

            progress = installer.getDownloader();
            installer.download();

            installer.queue(new FileMover(tempFile, file));

            progress = installer;
            installer.execute(launcher);

            return file;
        } finally {
            executor.shutdownNow();
        }
    }

    @Override
    public double getProgress() {
        return progress.getProgress();
    }

    @Override
    public String getStatus() {
        return progress.getStatus();
    }

}
