/*
  ====================================================================
  AZULEJO

  Built for the Coastline server network
  Copyright (C) 2025
  Some base code copyright (C) 2010-2014 Albert Pham and contributors
  Please see LICENSE.txt for more information.
  ====================================================================
*/


package com.thrandos.azulejo.launcher.auth;

import com.thrandos.azulejo.launcher.LauncherException;
import lombok.Getter;

/**
 * Thrown on authentication error.
 */
public class AuthenticationException extends LauncherException {
    @Getter
    private boolean invalidatedSession = false;

    public AuthenticationException(String message, String localizedMessage) {
        super(message, localizedMessage);
    }

    public AuthenticationException(String message) {
        super(message, message);
    }

    public AuthenticationException(String message, boolean invalidatedSession) {
        super(message, message);
        this.invalidatedSession = invalidatedSession;
    }

    public AuthenticationException(Throwable cause, String localizedMessage) {
        super(cause, localizedMessage);
    }
}
