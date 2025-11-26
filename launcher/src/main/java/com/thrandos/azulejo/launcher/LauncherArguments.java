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

import com.beust.jcommander.Parameter;
import lombok.Data;

import java.io.File;

/**
 * The command line arguments that the launcher accepts.
 */
@Data
public class LauncherArguments {

    @Parameter(names = "--dir")
    private File dir;

    @Parameter(names = "--bootstrap-version")
    private Integer bootstrapVersion;

    @Parameter(names = "--portable")
    private boolean portable;
    
    @Parameter(names = "--javafx")
    private boolean javafx;
    
    @Parameter(names = "--swing")
    private boolean swing;

}
