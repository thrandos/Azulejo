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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
public class RequireAll implements Condition {

    private List<Feature> features = new ArrayList<Feature>();

    public RequireAll() {
    }

    public RequireAll(List<Feature> features) {
        this.features = features;
    }

    public RequireAll(Feature... feature) {
        features.addAll(Arrays.asList(feature));
    }

    @Override
    public boolean matches() {
        if (features == null) {
            return true;
        }

        for (Feature feature : features) {
            if (!feature.isSelected()) {
                return false;
            }
        }

        return true;
    }

}
