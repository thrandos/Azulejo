/*
  ====================================================================
  AZULEJO

  Built for the Coastline server network
  Copyright (C) 2025-2026
  Some base code copyright (C) 2010-2014 Albert Pham and contributors
  Please see LICENSE.txt for more information.
  ====================================================================
*/


package com.thrandos.azulejo.launcher.builder;

import com.beust.jcommander.Parameter;
import lombok.Data;

import java.io.File;

@Data
public class ServerExportOptions {

    @Parameter(names = "--source", required = true)
    private File sourceDir;
    @Parameter(names = "--dest", required = true)
    private File destDir;

}
