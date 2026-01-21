/*
  ====================================================================
  AZULEJO

  Built for the Coastline server network
  Copyright (C) 2025-2026
  Some base code copyright (C) 2010-2014 Albert Pham and contributors
  Please see LICENSE.txt for more information.
  ====================================================================
*/


package com.thrandos.azulejo.launcher.install;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class FeatureCache {

    private Map<String, Boolean> selected = new HashMap<String, Boolean>();

}
