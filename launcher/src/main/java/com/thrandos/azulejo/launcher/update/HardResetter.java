/*
  ====================================================================
  AZULEJO

  Built for the Coastline server network
  Copyright (C) 2025
  Some base code copyright (C) 2010-2014 Albert Pham and contributors
  Please see LICENSE.txt for more information.
  ====================================================================
*/


package com.thrandos.azulejo.launcher.update;

import com.thrandos.azulejo.concurrency.ProgressObservable;
import com.thrandos.azulejo.launcher.Instance;
import com.thrandos.azulejo.launcher.LauncherUtils;
import com.thrandos.azulejo.launcher.persistence.Persistence;
import lombok.NonNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Callable;

import static com.thrandos.azulejo.launcher.util.SharedLocale.tr;

public class HardResetter implements Callable<Instance>, ProgressObservable {

    private final Instance instance;
    private File currentDir;

    public HardResetter(@NonNull Instance instance) {
        this.instance = instance;
    }

    @Override
    public double getProgress() {
        return -1;
    }

    @Override
    public String getStatus() {
        return tr("instanceResetter.resetting", instance.getTitle());
    }

    @Override
    public Instance call() throws Exception {
        instance.setInstalled(false);
        instance.setUpdatePending(true);
        Persistence.commitAndForget(instance);

        new File(instance.getDir(), "update_cache.json").delete();

        removeDir(new File(instance.getContentDir(), "config"));
        removeDir(new File(instance.getContentDir(), "mods"));
        
        return instance;
    }

    private void removeDir(File dir) throws IOException, InterruptedException {
        try {
            if (dir.isDirectory()) {
                currentDir = dir;
                LauncherUtils.interruptibleDelete(dir, new ArrayList<File>());
            }
        } finally {
            currentDir = null;
        }
    }

    public String toString() {
        File dir = currentDir;
        if (dir != null) {
            return "Removing " + dir.getAbsolutePath();
        } else {
            return "Working...";
        }
    }

}
