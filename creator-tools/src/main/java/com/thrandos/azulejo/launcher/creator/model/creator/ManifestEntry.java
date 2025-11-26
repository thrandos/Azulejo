/*
  ====================================================================
  AZULEJO

  Built for the Coastline server network
  Copyright (C) 2025
  Some base code copyright (C) 2010-2014 Albert Pham and contributors
  Please see LICENSE.txt for more information.
  ====================================================================
*/


package com.thrandos.azulejo.launcher.creator.model.creator;

import com.google.common.collect.Lists;
import com.thrandos.azulejo.launcher.model.modpack.ManifestInfo;
import lombok.Data;

import java.util.List;

@Data
public class ManifestEntry implements Comparable<ManifestEntry> {

    private boolean selected = false;
    private ManifestInfo manifestInfo;
    private List<String> gameKeys = Lists.newArrayList();

    @Override
    public int compareTo(ManifestEntry o) {
        return manifestInfo.compareTo(o.getManifestInfo());
    }

}
