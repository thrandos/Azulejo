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
import lombok.Data;

import java.util.List;

@Data
public class CreatorConfig {

    private List<RecentEntry> recentEntries = Lists.newArrayList();
    private boolean offlineEnabled;

    public void setRecentEntries(List<RecentEntry> recentEntries) {
        this.recentEntries = recentEntries != null ? recentEntries : Lists.newArrayList();
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

}
