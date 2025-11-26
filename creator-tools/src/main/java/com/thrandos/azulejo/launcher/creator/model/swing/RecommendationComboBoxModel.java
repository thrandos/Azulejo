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

import com.thrandos.azulejo.launcher.model.modpack.Feature.Recommendation;

import javax.swing.*;

public class RecommendationComboBoxModel extends AbstractListModel implements ComboBoxModel {

    private Recommendation selection;

    @Override
    public void setSelectedItem(Object anItem) {
        selection = (Recommendation) anItem;
    }

    @Override
    public Object getSelectedItem() {
        return selection;
    }

    @Override
    public int getSize() {
        return Recommendation.values().length + 1;
    }

    @Override
    public Object getElementAt(int index) {
        if (index == 0) {
            return null;
        } else {
            return Recommendation.values()[index - 1];
        }
    }
}
