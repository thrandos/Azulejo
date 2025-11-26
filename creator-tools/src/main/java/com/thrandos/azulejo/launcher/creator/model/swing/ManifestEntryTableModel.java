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

import com.google.common.base.Joiner;
import com.thrandos.azulejo.launcher.creator.model.creator.ManifestEntry;

import javax.swing.table.AbstractTableModel;
import java.util.List;

public class ManifestEntryTableModel extends AbstractTableModel {

    private final Joiner GAME_KEY_JOINER = Joiner.on(", ");
    private final List<ManifestEntry> entries;

    public ManifestEntryTableModel(List<ManifestEntry> entries) {
        this.entries = entries;
    }

    @Override
    public String getColumnName(int columnIndex) {
        return switch (columnIndex) {
            case 0 -> "";
            case 1 -> "Modpack";
            case 2 -> "Version";
            case 3 -> "Priority";
            case 4 -> "Location";
            case 5 -> "Game Keys";
            default -> null;
        };
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return switch (columnIndex) {
            case 0 -> Boolean.class;
            case 1 -> String.class;
            case 2 -> String.class;
            case 3 -> Integer.class;
            case 4 -> String.class;
            case 5 -> String.class;
            default -> null;
        };
    }
    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        switch (columnIndex) {
            case 0:
                entries.get(rowIndex).setSelected((Boolean) value);
                break;
            default:
                break;
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return switch (columnIndex) {
            case 0 -> true;
            default -> false;
        };
    }

    @Override
    public int getRowCount() {
        return entries.size();
    }

    @Override
    public int getColumnCount() {
        return 6;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        ManifestEntry entry = entries.get(rowIndex);

        switch (columnIndex) {
            case 0:
                return entry.isSelected();
            case 1:
                String title = entry.getManifestInfo().getTitle();
                if (title != null) {
                    return title;
                } else {
                    return entry.getManifestInfo().getName();
                }
            case 2:
                return entry.getManifestInfo().getVersion();
            case 3:
                return entry.getManifestInfo().getPriority();
            case 4:
                return entry.getManifestInfo().getLocation();
            case 5:
                List<String> gameKeys = entry.getGameKeys();
                return gameKeys != null ? GAME_KEY_JOINER.join(gameKeys) : "";
            default:
                return null;
        }
    }

}
