/*
  ====================================================================
  AZULEJO

  Built for the Coastline server network
  Copyright (C) 2025
  Some base code copyright (C) 2010-2014 Albert Pham and contributors
  Please see LICENSE.txt for more information.
  ====================================================================
*/


package com.thrandos.azulejo.launcher.swing;

import java.awt.*;
import java.util.concurrent.Executor;

public class EventQueueExecutor implements Executor {

    public static final EventQueueExecutor INSTANCE = new EventQueueExecutor();

    private EventQueueExecutor() {
    }

    @Override
    public void execute(Runnable runnable) {
        EventQueue.invokeLater(runnable);
    }

}
