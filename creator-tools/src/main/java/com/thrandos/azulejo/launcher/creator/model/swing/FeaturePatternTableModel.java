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

import com.thrandos.azulejo.launcher.builder.FeaturePattern;

import javax.swing.table.AbstractTableModel;
import java.util.List;

public class FeaturePatternTableModel extends AbstractTableModel {

    private final List<FeaturePattern> features;

    public FeaturePatternTableModel(List<FeaturePattern> features) {
        this.features = features;
    }

    @Override
    public String getColumnName(int columnIndex) {
        return switch (columnIndex) {
            case 0 -> "Feature";
            case 1 -> "Recommendation";
            case 2 -> "Default?";
            default -> null;
        };
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return switch (columnIndex) {
            case 0 -> String.class;
            case 1 -> String.class;
            case 2 -> String.class;
            default -> null;
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
        return features.size();
    }

    @Override
    public int getColumnCount() {
        return 3;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return switch (columnIndex) {
            case 0 -> features.get(rowIndex).getFeature().getName();
            case 1 -> features.get(rowIndex).getFeature().getRecommendation();
            case 2 -> features.get(rowIndex).getFeature().isSelected() ? "Yes" : "";
            default -> null;
        };
    }

    public FeaturePattern getFeature(int index) {
        return features.get(index);
    }

    public void addFeature(FeaturePattern pattern) {
        features.add(pattern);
        fireTableDataChanged();
    }

    public void removeFeature(int index) {
        features.remove(index);
        fireTableDataChanged();
    }

}
