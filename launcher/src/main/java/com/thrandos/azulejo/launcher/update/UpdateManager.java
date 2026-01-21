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

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.thrandos.azulejo.concurrency.ObservableFuture;
import com.thrandos.azulejo.launcher.Launcher;
import com.thrandos.azulejo.launcher.dialog.ProgressDialog;
import com.thrandos.azulejo.launcher.selfupdate.LatestVersionInfo;
import com.thrandos.azulejo.launcher.selfupdate.SelfUpdater;
import com.thrandos.azulejo.launcher.selfupdate.UpdateChecker;
import com.thrandos.azulejo.launcher.swing.SwingHelper;
import com.thrandos.azulejo.launcher.util.SharedLocale;
import com.thrandos.azulejo.launcher.util.SwingExecutor;
import lombok.Getter;

import javax.swing.*;
import javax.swing.event.SwingPropertyChangeSupport;
import java.awt.*;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.URL;

public class UpdateManager {

    @Getter
    private final SwingPropertyChangeSupport propertySupport = new SwingPropertyChangeSupport(this);
    private final Launcher launcher;
    private LatestVersionInfo pendingUpdate;

    public UpdateManager(Launcher launcher) {
        this.launcher = launcher;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertySupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertySupport.removePropertyChangeListener(listener);
    }

    public boolean getPendingUpdate() {
        return pendingUpdate != null;
    }

    public void checkForUpdate(final Window window) {
        ListenableFuture<LatestVersionInfo> future = launcher.getExecutor().submit(new UpdateChecker(launcher));

        Futures.addCallback(future, new FutureCallback<LatestVersionInfo>() {
            @Override
            public void onSuccess(LatestVersionInfo result) {
                if (result != null) {
                    requestUpdate(result);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                // Error handler attached below.
            }
        }, SwingExecutor.INSTANCE);

        SwingHelper.addErrorDialogCallback(window, future);
    }

    public void performUpdate(final Window window) {
        final URL url = pendingUpdate.getUrl();

        if (url != null) {
            SelfUpdater downloader = new SelfUpdater(launcher, url);
            ObservableFuture<File> future = new ObservableFuture<File>(
                    launcher.getExecutor().submit(downloader), downloader);

            Futures.addCallback(future, new FutureCallback<File>() {
                @Override
                public void onSuccess(File result) {
                    propertySupport.firePropertyChange("pendingUpdate", true, false);
                    UpdateManager.this.pendingUpdate = null;

                    SwingHelper.showMessageDialog(
                            window,
                            SharedLocale.tr("launcher.selfUpdateComplete"),
                            SharedLocale.tr("launcher.selfUpdateCompleteTitle"),
                            null,
                            JOptionPane.INFORMATION_MESSAGE);
                }

                @Override
                public void onFailure(Throwable t) {
                }
            }, SwingExecutor.INSTANCE);

            ProgressDialog.showProgress(window, future, SharedLocale.tr("launcher.selfUpdatingTitle"), SharedLocale.tr("launcher.selfUpdatingStatus"));
            SwingHelper.addErrorDialogCallback(window, future);
        } else {
            propertySupport.firePropertyChange("pendingUpdate", false, false);
        }
    }

    private void requestUpdate(LatestVersionInfo url) {
        propertySupport.firePropertyChange("pendingUpdate", getPendingUpdate(), url != null);
        this.pendingUpdate = url;
    }


}
