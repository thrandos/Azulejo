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

import com.thrandos.azulejo.launcher.builder.BuilderConfig;
import com.thrandos.azulejo.launcher.creator.Creator;
import com.thrandos.azulejo.launcher.creator.model.creator.Pack;
import com.thrandos.azulejo.launcher.swing.SwingHelper;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.util.List;

public class PackTableModel extends AbstractTableModel {

    private final Icon instanceIcon;
    private final Icon warningIcon;
    private final List<Pack> packs;

    public PackTableModel(List<Pack> packs) {
        this.packs = packs;

        instanceIcon = SwingHelper.createIcon(Creator.class, "pack_icon.png");
        warningIcon = SwingHelper.createIcon(Creator.class, "warning_icon.png");
    }

    @Override
    public String getColumnName(int columnIndex) {
        return switch (columnIndex) {
            case 0 -> "";
            case 1 -> "Name";
            case 2 -> "Title";
            case 3 -> "Game Version";
            case 4 -> "Location";
            default -> null;
        };
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return switch (columnIndex) {
            case 0 -> Icon.class;
            default -> String.class;
        };
    }

    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    @Override
    public int getRowCount() {
        return packs.size();
    }

    @Override
    public int getColumnCount() {
        return 5;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Pack pack = packs.get(rowIndex);

        BuilderConfig config = pack.getCachedConfig();

        return switch (columnIndex) {
            case 0 -> config != null ? instanceIcon : warningIcon;
            case 1 -> config != null ? config.getName() : "<Moved or Deleted>";
            case 2 -> config != null ? config.getTitle() : "?";
            case 3 -> config != null ? config.getGameVersion() : "?";
            case 4 -> pack.getLocation();
            default -> null;
        };
    }

}
