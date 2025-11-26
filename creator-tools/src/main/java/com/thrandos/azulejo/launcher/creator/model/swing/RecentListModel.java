/*
  ====================================================================
  AZULEJO

  Built for the Coastline server network
  Copyright (C) 2025
  Some base code copyright (C) 2010-2014 Albert Pham and contributors
  Please see LICENSE.txt for more information.
  ====================================================================
*/


package com.thrandos.azulejo.launcher.creator.model.swing;

import com.thrandos.azulejo.launcher.creator.model.creator.RecentEntry;

import javax.swing.*;
import java.util.List;

public class RecentListModel extends AbstractListModel<RecentEntry> {

    private final List<RecentEntry> recentEntries;

    public RecentListModel(List<RecentEntry> recentEntries) {
        this.recentEntries = recentEntries;
    }

    @Override
    public int getSize() {
        return recentEntries.size();
    }

    @Override
    public RecentEntry getElementAt(int index) {
        return recentEntries.get(index);
    }

    public void fireUpdate() {
        fireContentsChanged(this, 0, Integer.MAX_VALUE);
    }
}
