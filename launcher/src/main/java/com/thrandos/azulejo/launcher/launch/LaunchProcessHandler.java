/*
  ====================================================================
  AZULEJO

  Built for the Coastline server network
  Copyright (C) 2025-2026
  Some base code copyright (C) 2010-2014 Albert Pham and contributors
  Please see LICENSE.txt for more information.
  ====================================================================
*/


package com.thrandos.azulejo.launcher.launch;

import com.google.common.base.Function;
import com.thrandos.azulejo.launcher.Launcher;
import com.thrandos.azulejo.launcher.dialog.LauncherFrame;
import com.thrandos.azulejo.launcher.dialog.ProcessConsoleFrame;
import com.thrandos.azulejo.launcher.swing.MessageLog;
import lombok.NonNull;
import lombok.extern.java.Log;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;

/**
 * Handles post-process creation during launch.
 */
@Log
public class LaunchProcessHandler implements Function<Process, ProcessConsoleFrame> {

    private static final int CONSOLE_NUM_LINES = 10000;

    private final Launcher launcher;
    private ProcessConsoleFrame consoleFrame;

    public LaunchProcessHandler(@NonNull Launcher launcher) {
        this.launcher = launcher;
    }

    @Override
    public ProcessConsoleFrame apply(final Process process) {
        log.info("Watching process " + process);

        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    consoleFrame = new ProcessConsoleFrame(CONSOLE_NUM_LINES, true);
                    consoleFrame.setProcess(process);
                    // consoleFrame.setVisible(true); yeah I don't want this
                    MessageLog messageLog = consoleFrame.getMessageLog();
                    messageLog.consume(process.getInputStream());
                    messageLog.consume(process.getErrorStream());
                }
            });

            // Wait for the process to end
            process.waitFor();
        } catch (InterruptedException e) {
            // Orphan process
        } catch (InvocationTargetException e) {
            log.log(Level.WARNING, "Unexpected failure", e);
        }

        log.info("Process ended, re-showing launcher...");

        // Restore the launcher
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (consoleFrame != null) {
                    consoleFrame.setProcess(null);
                    //consoleFrame.requestFocus();
                }
            }
        });

        return consoleFrame;
    }

}
