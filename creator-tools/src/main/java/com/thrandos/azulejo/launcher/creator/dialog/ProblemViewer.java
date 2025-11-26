/*
  ====================================================================
  AZULEJO

  Built for the Coastline server network
  Copyright (C) 2025
  Some base code copyright (C) 2010-2014 Albert Pham and contributors
  Please see LICENSE.txt for more information.
  ====================================================================
*/


package com.thrandos.azulejo.launcher.creator.dialog;

import com.thrandos.azulejo.launcher.creator.model.creator.Problem;
import com.thrandos.azulejo.launcher.creator.model.swing.ProblemTableModel;
import com.thrandos.azulejo.launcher.swing.SwingHelper;
import com.thrandos.azulejo.launcher.swing.TextFieldPopupMenu;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.List;

public class ProblemViewer extends JDialog {

    private static final String DEFAULT_EXPLANATION = "Select a problem on the left to see the explanation here.";
    private final ProblemTable problemTable = new ProblemTable();
    private final ProblemTableModel problemTableModel;
    private final JTextArea explanationText = new JTextArea(DEFAULT_EXPLANATION);

    public ProblemViewer(Window parent, List<Problem> problems) {
        super(parent, "Potential Problems", ModalityType.DOCUMENT_MODAL);

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        initComponents();
        pack();
        setLocationRelativeTo(parent);

        problemTableModel = new ProblemTableModel(problems);
        problemTable.setModel(problemTableModel);
    }

    private void initComponents() {
        explanationText.setComponentPopupMenu(TextFieldPopupMenu.INSTANCE);

        explanationText.setFont(new JTextField().getFont());
        explanationText.setEditable(false);
        explanationText.setLineWrap(true);
        explanationText.setWrapStyleWord(true);

        JPanel container = new JPanel();
        container.setLayout(new MigLayout("fill, insets dialog"));

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                SwingHelper.wrapScrollPane(problemTable), SwingHelper.
                wrapScrollPane(explanationText));
        splitPane.setDividerLocation(180);
        SwingHelper.flattenJSplitPane(splitPane);

        container.add(splitPane, "grow, w 220:500, h 240, wrap");

        JButton closeButton = new JButton("Close");
        container.add(closeButton, "tag cancel, gaptop unrel");

        add(container, BorderLayout.CENTER);

        getRootPane().setDefaultButton(closeButton);
        getRootPane().registerKeyboardAction(e -> closeButton.doClick(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        problemTable.getSelectionModel().addListSelectionListener(e -> {
            Problem selected = problemTableModel.getProblem(problemTable.getSelectedRow());
            if (selected != null) {
                SwingHelper.setTextAndResetCaret(explanationText, selected.getExplanation());
            }
        });

        closeButton.addActionListener(e -> dispose());
    }
}
