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

import com.thrandos.azulejo.launcher.model.modpack.Feature;
import com.thrandos.azulejo.launcher.util.SharedLocale;

import javax.swing.table.AbstractTableModel;
import java.util.List;

public class FeatureTableModel extends AbstractTableModel {

    private final List<Feature> features;

    public FeatureTableModel(List<Feature> features) {
        this.features = features;
    }

    @Override
    public String getColumnName(int columnIndex) {
        return switch (columnIndex) {
            case 1 -> SharedLocale.tr("features.nameColumn");
            default -> null;
        };
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return switch (columnIndex) {
            case 0 -> Boolean.class;
            case 1 -> String.class;
            default -> null;
        };
    }

    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        switch (columnIndex) {
            case 0:
                features.get(rowIndex).setSelected((boolean) (Boolean) value);
                break;
            case 1:
            default:
                break;
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return switch (columnIndex) {
            case 0 -> true;
            case 1 -> false;
            default -> false;
        };
    }

    @Override
    public int getRowCount() {
        return features.size();
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        switch (columnIndex) {
            case 0:
                return features.get(rowIndex).isSelected();
            case 1:
                Feature feature = features.get(rowIndex);
                return "<html>" + SwingHelper.htmlEscape(feature.getName()) + getAddendum(feature) + "</html>";
            default:
                return null;
        }
    }

    private String getAddendum(Feature feature) {
        if (feature.getRecommendation() == null) {
            return "";
        }
        return switch (feature.getRecommendation()) {
            case STARRED -> " <span style=\"color: #3758DB\">" + SharedLocale.tr("features.starred") + "</span>";
            case AVOID -> " <span style=\"color: red\">" + SharedLocale.tr("features.avoid") + "</span>";
            default -> "";
        };
    }

}
