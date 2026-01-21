/*
  ====================================================================
  AZULEJO

  Built for the Coastline server network
  Copyright (C) 2025-2026
  Some base code copyright (C) 2010-2014 Albert Pham and contributors
  Please see LICENSE.txt for more information.
  ====================================================================
*/

package com.thrandos.azulejo.concurrency;

import lombok.Data;

/**
 * A simple default implementation of {@link com.thrandos.azulejo.concurrency.ProgressObservable}
 * with settable properties.
 */
@Data
public class DefaultProgress implements ProgressObservable {

    private String status;
    private double progress = -1;

    public DefaultProgress() {
    }

    public DefaultProgress(double progress, String status) {
        this.progress = progress;
        this.status = status;
    }
}
