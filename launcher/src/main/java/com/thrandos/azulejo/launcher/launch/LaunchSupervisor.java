/*
  ====================================================================
  AZULEJO

  Built for the Coastline server network
  Copyright (C) 2025
  Some base code copyright (C) 2010-2014 Albert Pham and contributors
  Please see LICENSE.txt for more information.
  ====================================================================
*/


package com.thrandos.azulejo.launcher.launch;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.thrandos.azulejo.concurrency.ObservableFuture;
import com.thrandos.azulejo.launcher.Instance;
import com.thrandos.azulejo.launcher.Launcher;
import com.thrandos.azulejo.launcher.auth.Session;
import com.thrandos.azulejo.launcher.dialog.AccountSelectDialog;
import com.thrandos.azulejo.launcher.dialog.ProcessConsoleFrame;
import com.thrandos.azulejo.launcher.dialog.ProgressDialog;
import com.thrandos.azulejo.launcher.launch.LaunchOptions.UpdatePolicy;
import com.thrandos.azulejo.launcher.launch.runtime.JavaRuntime;
import com.thrandos.azulejo.launcher.model.minecraft.JavaVersion;
import com.thrandos.azulejo.launcher.persistence.Persistence;
import com.thrandos.azulejo.launcher.swing.SwingHelper;
import com.thrandos.azulejo.launcher.update.Updater;
import com.thrandos.azulejo.launcher.util.SharedLocale;
import com.thrandos.azulejo.launcher.util.SwingExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.apache.commons.io.FileUtils;

import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.function.BiPredicate;
import java.util.logging.Level;

import static com.google.common.util.concurrent.MoreExecutors.directExecutor;
import static com.thrandos.azulejo.launcher.util.SharedLocale.tr;

@Log
public class LaunchSupervisor {

    private final Launcher launcher;

    public LaunchSupervisor(Launcher launcher) {
        this.launcher = launcher;
    }

    public void launch(LaunchOptions options) {
        final Window window = options.getWindow();
        final Instance instance = options.getInstance();
        final LaunchListener listener = options.getListener();

        try {
            boolean update = options.getUpdatePolicy().isUpdateEnabled() && instance.isUpdatePending();

            // Store last access date
            Date now = new Date();
            instance.setLastAccessed(now);
            Persistence.commitAndForget(instance);

            // Perform login
            final Session session;
            if (options.getSession() != null) {
                session = options.getSession();
            } else {
                session = AccountSelectDialog.showAccountRequest(window, launcher);
                if (session == null) {
                    return;
                }
            }

            // If we have to update, we have to update (unless NO_UPDATE policy is set)
            if (!instance.isInstalled() && options.getUpdatePolicy() != UpdatePolicy.NO_UPDATE) {
                update = true;
            }

            if (update) {
                // Execute the updater
                Updater updater = new Updater(launcher, instance);
                updater.setOnline(options.getUpdatePolicy() == UpdatePolicy.ALWAYS_UPDATE || session.isOnline());
                ObservableFuture<Instance> future = new ObservableFuture<Instance>(
                        launcher.getExecutor().submit(updater), updater);

                // Show progress
                ProgressDialog.showProgress(window, future, SharedLocale.tr("launcher.updatingTitle"), tr("launcher.updatingStatus", instance.getTitle()));
                SwingHelper.addErrorDialogCallback(window, future);

                // Update the list of instances after updating
                future.addListener(new Runnable() {
                    @Override
                    public void run() {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                listener.instancesUpdated();
                            }
                        });
                    }
                }, SwingExecutor.INSTANCE);

                // On success, launch also
                Futures.addCallback(future, new FutureCallback<Instance>() {
                    @Override
                    public void onSuccess(Instance result) {
                        launch(window, instance, session, listener, options);
                    }

                    @Override
                    public void onFailure(Throwable t) {
                    }
                }, SwingExecutor.INSTANCE);
            } else {
                launch(window, instance, session, listener, options);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            SwingHelper.showErrorDialog(window, SharedLocale.tr("launcher.noInstanceError"), SharedLocale.tr("launcher.noInstanceTitle"));
        }
    }

    private void launch(Window window, Instance instance, Session session, final LaunchListener listener, LaunchOptions options) {
        final File extractDir = launcher.createExtractDir();

        // Get the process
        Runner task = new Runner(launcher, instance, session, extractDir, new RuntimeVerifier(instance), options);
        ObservableFuture<Process> processFuture = new ObservableFuture<Process>(
                launcher.getExecutor().submit(task), task);

        // Show process for the process retrieval
        ProgressDialog.showProgress(
                window, processFuture, SharedLocale.tr("launcher.launchingTItle"), tr("launcher.launchingStatus", instance.getTitle()));

        // If the process is started, get rid of this window
        Futures.addCallback(processFuture, new FutureCallback<Process>() {
            @Override
            public void onSuccess(Process result) {
                SwingUtilities.invokeLater(listener::gameStarted);
            }

            @Override
            public void onFailure(Throwable t) {
            }
        }, SwingExecutor.INSTANCE);

        // Watch the created process
        ListenableFuture<ProcessConsoleFrame> future = Futures.transform(
                processFuture, new LaunchProcessHandler(launcher), launcher.getExecutor());
        SwingHelper.addErrorDialogCallback(null, future);

        // Clean up at the very end
        future.addListener(() -> {
            try {
                log.info("Process ended; cleaning up " + extractDir.getAbsolutePath());
                FileUtils.deleteDirectory(extractDir);
            } catch (IOException e) {
                log.log(Level.WARNING, "Failed to clean up " + extractDir.getAbsolutePath(), e);
            }
        }, directExecutor());

        // Hook up launch listener
        Futures.addCallback(future, new FutureCallback<ProcessConsoleFrame>() {
            @Override
            public void onSuccess(@Nullable ProcessConsoleFrame result) {
                // gameStarted was only invoked on success above, so only call gameClosed on success
                listener.gameClosed();
            }

            @Override
            public void onFailure(Throwable t) {
                // likely user cancellation
                if (!(t instanceof CancellationException)) {
                    log.info("Process failure: " + t.getLocalizedMessage());
                }
            }
        }, SwingExecutor.INSTANCE);
    }

    @RequiredArgsConstructor
    static class RuntimeVerifier implements BiPredicate<JavaRuntime, JavaVersion> {
        private final Instance instance;

        @Override
        public boolean test(JavaRuntime javaRuntime, JavaVersion javaVersion) {
            ListenableFuture<Boolean> fut = SwingExecutor.INSTANCE.submit(() -> {
                Object[] options = new Object[]{
                        tr("button.cancel"),
                        tr("button.launchAnyway"),
                };

                String message = tr("runner.wrongJavaVersion",
                        instance.getTitle(), javaVersion.getMajorVersion(), javaRuntime.getVersion());
                int picked = JOptionPane.showOptionDialog(null,
                        SwingHelper.htmlWrap(message),
                        tr("launcher.javaMismatchTitle"),
                        JOptionPane.DEFAULT_OPTION,
                        JOptionPane.WARNING_MESSAGE,
                        null,
                        options,
                        null);

                return picked == 1;
            });

            try {
                return fut.get();
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
