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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class DirectoryDeleter implements Callable<File>, ProgressObservable {

    private final File dir;

    public DirectoryDeleter(File dir) {
        this.dir = dir;
    }

    @Override
    public File call() throws Exception {
        Thread.sleep(2000);

        List<File> failures = new ArrayList<File>();

        try {
            LauncherUtils.interruptibleDelete(dir, failures);
        } catch (IOException e) {
            Thread.sleep(1000);
            LauncherUtils.interruptibleDelete(dir, failures);
        }

        if (failures.size() > 0) {
            throw new LauncherException(failures.size() + " failed to delete", failures.size() + " file(s) could not be deleted");
        }

        return dir;
    }

    @Override
    public double getProgress() {
        return -1;
    }

    @Override
    public String getStatus() {
        return "Deleting files...";
    }

}
