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

import lombok.Data;
import lombok.NonNull;

import java.util.HashMap;
import java.util.Map;

@Data
public class UpdateCache {

    private Map<String, String> cache = new HashMap<String, String>();

    public synchronized boolean mark(@NonNull String key, @NonNull String version) {
        String current = cache.get(key);
        if (current != null && version.equals(current)) {
            return false;
        } else {
            cache.put(key, version);
            return true;
        }
    }
}
