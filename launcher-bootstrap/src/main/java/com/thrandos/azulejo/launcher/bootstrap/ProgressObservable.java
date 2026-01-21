/*
  ====================================================================
  AZULEJO

  Built for the Coastline server network
  Copyright (C) 2025-2026
  Some base code copyright (C) 2010-2014 Albert Pham and contributors
  Please see LICENSE.txt for more information.
  ====================================================================
*/
package com.thrandos.azulejo.launcher.bootstrap;

/**
 * Implementations of this interface can provide information on the progress
 * of a task.
 */
public interface ProgressObservable {

    /**
     * Get the progress value as a number between 0 and 1 (inclusive), or -1
     * if progress information is unavailable.
     *
     * @return the progress value
     */
    double getProgress();

}
