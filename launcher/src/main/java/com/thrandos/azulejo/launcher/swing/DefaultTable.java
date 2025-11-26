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
import java.awt.*;

/**
 * The default table style used throughout the launcher.
 */
public class DefaultTable extends JTable {

    public DefaultTable() {
        setShowGrid(false);
        setRowHeight((int) (Math.max(getRowHeight(), new JCheckBox().getPreferredSize().getHeight() - 2)));
        setIntercellSpacing(new Dimension(0, 0));
        setFillsViewportHeight(true);
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

}
