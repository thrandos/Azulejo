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
import com.thrandos.azulejo.launcher.creator.model.creator.Problem;
import com.thrandos.azulejo.launcher.swing.SwingHelper;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.util.List;

public class ProblemTableModel extends AbstractTableModel {

    private static final Icon WARNING_ICON;

    static {
        WARNING_ICON = SwingHelper.createIcon(Creator.class, "warning_icon.png");
    }

    private final List<Problem> problems;

    public ProblemTableModel(List<Problem> problems) {
        this.problems = problems;
    }

    @Override
    public String getColumnName(int columnIndex) {
        return switch (columnIndex) {
            case 0 -> "";
            case 1 -> "Problem";
            default -> null;
        };
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return switch (columnIndex) {
            case 0 -> Icon.class;
            case 1 -> Problem.class;
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
        return problems.size();
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return switch (columnIndex) {
            case 0 -> WARNING_ICON;
            case 1 -> problems.get(rowIndex);
            default -> null;
        };
    }

    public Problem getProblem(int index) {
        return problems.get(index);
    }

}
