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

import com.thrandos.azulejo.launcher.LauncherUtils;
import com.thrandos.azulejo.launcher.creator.Creator;
import com.thrandos.azulejo.launcher.swing.SwingHelper;
import lombok.extern.java.Log;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;

@Log
public class AboutDialog extends JDialog {

    private String version;

    public AboutDialog(Window parent) {
        super(parent, "About", ModalityType.DOCUMENT_MODAL);

        try {
            Properties properties = LauncherUtils.loadProperties(Creator.class, "creator.properties", "com.thrandos.azulejo.creator.propertiesFile");
            version = properties.getProperty("version", "????");
        } catch (IOException e) {
            log.log(Level.WARNING, "Failed to get version", e);
            version = "????";
        }

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        initComponents();
        setResizable(false);
        pack();
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        JPanel container = new JPanel();
        container.setLayout(new MigLayout("insets dialog"));

        container.add(new JLabel(SwingHelper.createIcon(Creator.class, "about_header.png")), "dock north");
        container.add(new JLabel("<html>Version " + version), "wrap");
        container.add(new JLabel("<html>Licensed under GNU LGPL v3."), "wrap, gapbottom unrel");
        container.add(new JLabel("<html>Created for the Coastline server network. Visit our website!"), "wrap, gapbottom unrel");

        JButton okButton = new JButton("OK");
        JButton sourceCodeButton = new JButton("Source Code");
        JButton skCraftButton = new JButton("Website");

        container.add(sourceCodeButton, "span, split 3, sizegroup bttn");
        container.add(skCraftButton, "sizegroup bttn");
        container.add(okButton, "tag ok, sizegroup bttn");

        add(container, BorderLayout.CENTER);

        getRootPane().setDefaultButton(okButton);
        getRootPane().registerKeyboardAction(e -> okButton.doClick(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        okButton.addActionListener(e -> dispose());
        sourceCodeButton.addActionListener(e -> SwingHelper.openURL("https://coastline.thrandos.com", this));
        skCraftButton.addActionListener(e -> SwingHelper.openURL("https://coastline.thrandos.com", this));
    }

    public static void showAboutDialog(Window parent) {
        AboutDialog dialog = new AboutDialog(parent);
        dialog.setVisible(true);
    }
}

