/*
  ====================================================================
  AZULEJO

  Built for the Coastline server network
  Copyright (C) 2025
  Some base code copyright (C) 2010-2014 Albert Pham and contributors
  Please see LICENSE.txt for more information.
  ====================================================================
*/


package com.thrandos.azulejo.launcher.creator.dialog;

import com.thrandos.azulejo.launcher.swing.DefaultTable;

import javax.swing.table.TableModel;

class FeaturePatternTable extends DefaultTable {

    @Override
    public void setModel(TableModel dataModel) {
        super.setModel(dataModel);
        try {
            getColumnModel().getColumn(1).setMaxWidth(80);
            getColumnModel().getColumn(2).setMaxWidth(80);
        } catch (ArrayIndexOutOfBoundsException ignored) {
        }
    }
}
