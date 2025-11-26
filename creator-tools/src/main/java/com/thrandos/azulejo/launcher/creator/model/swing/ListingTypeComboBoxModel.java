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

import javax.swing.*;

public class ListingTypeComboBoxModel extends AbstractListModel<ListingType> implements ComboBoxModel<ListingType> {

    private ListingType selection = ListingType.STATIC;

    @Override
    public void setSelectedItem(Object anItem) {
        selection = (ListingType) anItem;
    }

    @Override
    public ListingType getSelectedItem() {
        return selection;
    }

    @Override
    public int getSize() {
        return ListingType.values().length;
    }

    @Override
    public ListingType getElementAt(int index) {
        return ListingType.values()[index];
    }

}
