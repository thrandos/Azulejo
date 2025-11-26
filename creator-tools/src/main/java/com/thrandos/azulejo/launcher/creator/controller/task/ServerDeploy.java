/*
  ====================================================================
  AZULEJO

  Built for the Coastline server network
  Copyright (C) 2025
  Some base code copyright (C) 2010-2014 Albert Pham and contributors
  Please see LICENSE.txt for more information.
  ====================================================================
*/


package com.thrandos.azulejo.launcher.creator.controller.task;

import com.thrandos.azulejo.concurrency.ProgressObservable;
import com.thrandos.azulejo.launcher.LauncherException;
import com.thrandos.azulejo.launcher.LauncherUtils;
import com.thrandos.azulejo.launcher.builder.ServerCopyExport;
import com.thrandos.azulejo.launcher.creator.dialog.DeployServerDialog.DeployOptions;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class ServerDeploy implements Callable<ServerDeploy>, ProgressObservable {

    private final File srcDir;
    private final DeployOptions options;

    public ServerDeploy(File srcDir, DeployOptions options) {
        this.srcDir = srcDir;
        this.options = options;
    }

    @Override
    public ServerDeploy call() throws Exception {
        File modsDir = new File(options.getDestDir(), "mods");

        if (options.isCleanMods() && modsDir.isDirectory()) {
            List<File> failures = new ArrayList<File>();

            try {
                LauncherUtils.interruptibleDelete(modsDir, failures);
            } catch (IOException e) {
                Thread.sleep(1000);
                LauncherUtils.interruptibleDelete(modsDir, failures);
            }

            if (failures.size() > 0) {
                throw new LauncherException(failures.size() + " failed to delete", "There were " + failures.size() + " failures during cleaning.");
            }
        }

        String[] args = {
                "--source", srcDir.getAbsolutePath(),
                "--dest", options.getDestDir().getAbsolutePath()
        };
        ServerCopyExport.main(args);

        return this;
    }

    @Override
    public double getProgress() {
        return -1;
    }

    @Override
    public String getStatus() {
        return "Deploying server files...";
    }

}
