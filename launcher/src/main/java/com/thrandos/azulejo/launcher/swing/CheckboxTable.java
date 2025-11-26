/*
  ====================================================================
  AZULEJO

  Built for the Coastline server network
  Copyright (C) 2025
  Some base code copyright (C) 2010-2014 Albert Pham and contributors
  Please see LICENSE.txt for more information.
  ====================================================================
*/


package com.thrandos.azulejo.launcher.swing;

import javax.swing.*;
import javax.swing.table.TableModel;

public class CheckboxTable extends DefaultTable {

    @Override
    public void setModel(TableModel dataModel) {
        super.setModel(dataModel);
        try {
            getColumnModel().getColumn(0).setMaxWidth((int) new JCheckBox().getPreferredSize().getWidth());
        } catch (ArrayIndexOutOfBoundsException e) {
        }
    }

}
