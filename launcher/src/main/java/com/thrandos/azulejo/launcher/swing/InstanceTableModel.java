/*
  ====================================================================
  AZULEJO

  Built for the Coastline server network
  Copyright (C) 2025-2026
  Some base code copyright (C) 2010-2014 Albert Pham and contributors
  Please see LICENSE.txt for more information.
  ====================================================================
*/


package com.thrandos.azulejo.launcher.swing;

import com.thrandos.azulejo.launcher.Instance;
import com.thrandos.azulejo.launcher.InstanceList;
import com.thrandos.azulejo.launcher.Launcher;
import com.thrandos.azulejo.launcher.util.SharedLocale;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;

public class InstanceTableModel extends AbstractTableModel {

    private final InstanceList instances;
    private final Icon instanceIcon;
    private final Icon customInstanceIcon;
    private final Icon downloadIcon;

    public InstanceTableModel(InstanceList instances) {
        this.instances = instances;
        instanceIcon = SwingHelper.createIcon(Launcher.class, "instance_icon.png", 16, 16);
        customInstanceIcon = SwingHelper.createIcon(Launcher.class, "custom_instance_icon.png", 16, 16);
        downloadIcon = SwingHelper.createIcon(Launcher.class, "download_icon.png", 14, 14);
    }

    public void update() {
        instances.sort();
        fireTableDataChanged();
    }

    @Override
    public String getColumnName(int columnIndex) {
        return switch (columnIndex) {
            case 0 -> "";
            case 1 -> SharedLocale.tr("launcher.modpackColumn");
            default -> null;
        };
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return switch (columnIndex) {
            case 0 -> ImageIcon.class;
            case 1 -> String.class;
            default -> null;
        };
    }

    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        switch (columnIndex) {
            case 0:
                instances.get(rowIndex).setSelected((boolean) (Boolean) value);
                break;
            case 1:
            default:
                break;
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return switch (columnIndex) {
            case 0 -> false;
            case 1 -> false;
            default -> false;
        };
    }

    @Override
    public int getRowCount() {
        return instances.size();
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Instance instance;
        switch (columnIndex) {
            case 0:
                instance = instances.get(rowIndex);
                if (!instance.isLocal()) {
                    return downloadIcon;
                } else if (instance.getManifestURL() != null) {
                    return instanceIcon;
                } else {
                    return customInstanceIcon;
                }
            case 1:
                instance = instances.get(rowIndex);
                return instance.getTitle();
            default:
                return null;
        }
    }

}
