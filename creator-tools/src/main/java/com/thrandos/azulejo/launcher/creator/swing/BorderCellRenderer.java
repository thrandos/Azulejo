/*
  ====================================================================
  AZULEJO

  Built for the Coastline server network
  Copyright (C) 2025
  Some base code copyright (C) 2010-2014 Albert Pham and contributors
  Please see LICENSE.txt for more information.
  ====================================================================
*/


package com.thrandos.azulejo.launcher.creator.swing;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

public class BorderCellRenderer implements ListCellRenderer {

    private final Border border;
    private final DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();

    public BorderCellRenderer(Border border) {
        this.border = border;
    }

    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        JLabel renderer = (JLabel) defaultRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        renderer.setBorder(border);
        return renderer;
    }

}
