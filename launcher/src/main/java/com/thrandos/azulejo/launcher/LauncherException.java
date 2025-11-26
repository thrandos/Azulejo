/*
  ====================================================================
  AZULEJO

  Built for the Coastline server network
  Copyright (C) 2025
  Some base code copyright (C) 2010-2014 Albert Pham and contributors
  Please see LICENSE.txt for more information.
  ====================================================================
*/


package com.thrandos.azulejo.launcher;

/**
 * A human-readable error wrapper.
 */
public class LauncherException extends Exception {

    private final String localizedMessage;

    public LauncherException(String message, String localizedMessage) {
        super(message);
        this.localizedMessage = localizedMessage;
    }

    public LauncherException(Throwable cause, String localizedMessage) {
        super(cause.getMessage(), cause);
        this.localizedMessage = localizedMessage;
    }

    @Override
    public String getLocalizedMessage() {
        return localizedMessage;
    }
}
