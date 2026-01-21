/*
  ====================================================================
  AZULEJO

  Built for the Coastline server network
  Copyright (C) 2025-2026
  Some base code copyright (C) 2010-2014 Albert Pham and contributors
  Please see LICENSE.txt for more information.
  ====================================================================
*/


package com.thrandos.azulejo.launcher.update;

import com.thrandos.azulejo.concurrency.ProgressObservable;
import com.thrandos.azulejo.launcher.Instance;
import com.thrandos.azulejo.launcher.LauncherException;
import com.thrandos.azulejo.launcher.LauncherUtils;
import com.thrandos.azulejo.launcher.persistence.Persistence;
import lombok.NonNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import static com.thrandos.azulejo.launcher.LauncherUtils.checkInterrupted;
import static com.thrandos.azulejo.launcher.util.SharedLocale.tr;

public class Remover implements Callable<Instance>, ProgressObservable {

    private final Instance instance;

    public Remover(@NonNull Instance instance) {
        this.instance = instance;
    }

    @Override
    public double getProgress() {
        return -1;
    }

    @Override
    public String getStatus() {
        return tr("instanceDeleter.deleting", instance.getDir());
    }

    @Override
    public Instance call() throws Exception {
        instance.setInstalled(false);
        instance.setUpdatePending(true);
        Persistence.commitAndForget(instance);

        checkInterrupted();

        Thread.sleep(2000);

        List<File> failures = new ArrayList<File>();

        try {
            LauncherUtils.interruptibleDelete(instance.getDir(), failures);
        } catch (IOException e) {
            Thread.sleep(1000);
            LauncherUtils.interruptibleDelete(instance.getDir(), failures);
        }

        if (failures.size() > 0) {
            throw new LauncherException(failures.size() + " failed to delete",
                     tr("instanceDeleter.failures", failures.size()));
        }

        return instance;
    }

}
