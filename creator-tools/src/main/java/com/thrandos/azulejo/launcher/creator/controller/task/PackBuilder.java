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
import com.thrandos.azulejo.launcher.builder.PackageBuilder;
import com.thrandos.azulejo.launcher.creator.model.creator.Pack;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class PackBuilder implements Callable<PackBuilder>, ProgressObservable {

    private final Pack pack;
    private final File outputDir;
    private final String version;
    private final String manifestFilename;
    private final boolean clean;
    private final boolean downloadUrls;

    public PackBuilder(Pack pack, File outputDir, String version, String manifestFilename, boolean clean, boolean downloadUrls) {
        this.pack = pack;
        this.outputDir = outputDir;
        this.version = version;
        this.manifestFilename = manifestFilename;
        this.clean = clean;
        this.downloadUrls = downloadUrls;
    }

    @Override
    public PackBuilder call() throws Exception {
        if (clean) {
            List<File> failures = new ArrayList<File>();

            try {
                LauncherUtils.interruptibleDelete(outputDir, failures);
            } catch (IOException e) {
                Thread.sleep(1000);
                LauncherUtils.interruptibleDelete(outputDir, failures);
            }

            if (failures.size() > 0) {
                throw new LauncherException(failures.size() + " failed to delete", "There were " + failures.size() + " failures during cleaning.");
            }
        }

        //noinspection ResultOfMethodCallIgnored
        outputDir.mkdirs();

        System.setProperty("com.thrandos.azulejo.builder.ignoreURLOverrides", downloadUrls ? "false" : "true");
        String[] args = {
                "--version", version,
                "--manifest-dest", new File(outputDir, manifestFilename).getAbsolutePath(),
                "-i", pack.getDirectory().getAbsolutePath(),
                "-o", outputDir.getAbsolutePath()
        };
        PackageBuilder.main(args);

        return this;
    }

    @Override
    public double getProgress() {
        return -1;
    }

    @Override
    public String getStatus() {
        return "Building modpack...";
    }
}
