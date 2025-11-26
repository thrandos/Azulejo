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

import com.thrandos.azulejo.launcher.creator.Creator;
import com.thrandos.azulejo.launcher.creator.model.creator.ModFile;
import com.thrandos.azulejo.launcher.swing.SwingHelper;

import javax.annotation.Nullable;
import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public class ModFileTableModel extends AbstractTableModel {

    private static final Icon WWW_ICON;
    private final List<ModFile> mods;

    static {
        WWW_ICON = SwingHelper.createIcon(Creator.class, "www_icon.png");
    }

    public ModFileTableModel(List<ModFile> mods) {
        checkNotNull(mods, "mods");
        this.mods = mods;
    }

    @Override
    public String getColumnName(int columnIndex) {
        return switch (columnIndex) {
            case 0 -> "";
            case 1 -> "Mod";
            case 2 -> "Version";
            case 3 -> "Latest Release";
            case 4 -> "Latest Dev";
            case 5 -> "Mod ID";
            case 6 -> "Filename";
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
        return mods.size();
    }

    @Override
    public int getColumnCount() {
        return 7;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        @Nullable ModFile mod = mods.get(rowIndex);

        if (mod == null) {
            return null;
        }

        return switch (columnIndex) {
            case 0 -> mod.getUrl() != null ? WWW_ICON : null;
            case 1 -> mod.getName() != null ? mod.getName() : mod.getFile().getName();
            case 2 -> mod.getCleanVersion();
            case 3 -> mod.getLatestVersion();
            case 4 -> mod.getLatestDevVersion();
            case 5 -> mod.getModId();
            case 6 -> mod.getFile().getName();
            default -> null;
        };
    }

    public ModFile getMod(int index) {
        return mods.get(index);
    }

}
