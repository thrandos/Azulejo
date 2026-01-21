/*
  ====================================================================
  AZULEJO

  Built for the Coastline server network
  Copyright (C) 2025-2026
  Some base code copyright (C) 2010-2014 Albert Pham and contributors
  Please see LICENSE.txt for more information.
  ====================================================================
*/


package com.thrandos.azulejo.launcher.model.modpack;

import lombok.Data;

import java.util.List;

@Data
public class PackageList {

    public static final int MIN_VERSION = 1;

    private int minimumVersion;
    private List<ManifestInfo> packages;

}
