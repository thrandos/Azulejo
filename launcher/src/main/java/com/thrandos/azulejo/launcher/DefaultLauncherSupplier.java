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

import com.google.common.base.Supplier;
import com.thrandos.azulejo.launcher.dialog.LauncherFrame;

import java.awt.*;

public class DefaultLauncherSupplier implements Supplier<Window> {

    private final Launcher launcher;

    public DefaultLauncherSupplier(Launcher launcher) {
        this.launcher = launcher;
    }

    @Override
    public Window get() {
        return new LauncherFrame(launcher);
    }

}
