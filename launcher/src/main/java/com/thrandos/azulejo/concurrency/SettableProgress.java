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

public class SettableProgress implements ProgressObservable {

    private ProgressObservable delegate;
    private String status = "";
    private double progress = -1;

    public SettableProgress(String status, double progress) {
        this.status = status;
        this.progress = progress;
    }

    public SettableProgress(ProgressObservable observable) {
        this.delegate = observable;
    }

    public synchronized void observe(ProgressObservable observable) {
        delegate = observable;
    }

    public synchronized void set(String status, double progress) {
        delegate = null;
        this.progress = progress;
        this.status = status;
    }

    @Override
    public double getProgress() {
        ProgressObservable delegate = this.delegate;
        return delegate != null ? delegate.getProgress() : progress;
    }

    @Override
    public String getStatus() {
        ProgressObservable delegate = this.delegate;
        return delegate != null ? delegate.getStatus() : status;
    }

}
