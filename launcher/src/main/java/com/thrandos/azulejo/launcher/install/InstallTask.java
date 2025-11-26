/*
  ====================================================================
  AZULEJO

  Built for the Coastline server network
  Copyright (C) 2025
  Some base code copyright (C) 2010-2014 Albert Pham and contributors
  Please see LICENSE.txt for more information.
  ====================================================================
*/


package com.thrandos.azulejo.launcher.install;

import com.thrandos.azulejo.concurrency.ProgressObservable;
import com.thrandos.azulejo.launcher.Launcher;

public interface InstallTask extends ProgressObservable {

    void execute(Launcher launcher) throws Exception;

}
