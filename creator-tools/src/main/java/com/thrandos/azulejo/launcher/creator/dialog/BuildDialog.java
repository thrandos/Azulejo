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

import com.thrandos.azulejo.launcher.swing.DirectoryField;
import com.thrandos.azulejo.launcher.swing.SwingHelper;
import com.thrandos.azulejo.launcher.swing.TextFieldPopupMenu;
import lombok.Data;
import lombok.Getter;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.File;

public class BuildDialog extends JDialog {

    private final DirectoryField destDirField = new DirectoryField();
    private final JTextField versionText = new JTextField(20);
    private final JTextField manifestFilenameText = new JTextField(30);
    @Getter
    private BuildOptions options;

    public BuildDialog(Window parent) {
        super(parent, "Build Release", ModalityType.DOCUMENT_MODAL);

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        initComponents();
        setResizable(false);
        pack();
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        versionText.setComponentPopupMenu(TextFieldPopupMenu.INSTANCE);
        manifestFilenameText.setComponentPopupMenu(TextFieldPopupMenu.INSTANCE);

        JPanel container = new JPanel();
        container.setLayout(new MigLayout("insets dialog"));

        container.add(new JLabel("Version:"));
        container.add(versionText, "span");

        container.add(new JLabel("Manifest Filename:"));
        container.add(manifestFilenameText, "span");

        container.add(new JLabel("Output Directory:"));
        container.add(destDirField, "span");

        JButton buildButton = new JButton("Build");
        JButton cancelButton = new JButton("Cancel");

        container.add(buildButton, "tag ok, span, split 2, sizegroup bttn");
        container.add(cancelButton, "tag cancel, sizegroup bttn");

        add(container, BorderLayout.CENTER);

        getRootPane().setDefaultButton(buildButton);
        getRootPane().registerKeyboardAction(e -> cancelButton.doClick(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        buildButton.addActionListener(e -> returnValue());
        cancelButton.addActionListener(e -> dispose());
    }

    private void returnValue() {
        String version = versionText.getText().trim();
        String manifestFilename = manifestFilenameText.getText().trim();

        if (version.isEmpty()) {
            SwingHelper.showErrorDialog(this, "A version string must be entered.", "Error");
            return;
        }

        if (manifestFilename.isEmpty()) {
            SwingHelper.showErrorDialog(this, "A manifest filename must be entered.", "Error");
            return;
        }

        if (destDirField.getPath().isEmpty()) {
            SwingHelper.showErrorDialog(this, "A destination directory must be entered.", "Error");
            return;
        }

        options = new BuildOptions(version, manifestFilename, new File(destDirField.getPath()));
        dispose();
    }

    public static BuildOptions showBuildDialog(Window parent, String version, String manifestName, File destDir) {
        BuildDialog dialog = new BuildDialog(parent);
        dialog.versionText.setText(version);
        dialog.manifestFilenameText.setText(manifestName);
        dialog.destDirField.setPath(destDir.getAbsolutePath());
        dialog.setVisible(true);
        return dialog.getOptions();
    }

    @Data
    public static class BuildOptions {
        private final String version;
        private final String manifestFilename;
        private final File destDir;
    }

}
