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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.thrandos.azulejo.launcher.model.modpack.Feature;
import lombok.Data;

@Data
public class FeaturePattern {

    @JsonProperty("properties")
    private Feature feature;
    @JsonProperty("files")
    private FnPatternList filePatterns = new FnPatternList();

    public boolean matches(String path) {
        return filePatterns != null && filePatterns.matches(path);
    }
}
