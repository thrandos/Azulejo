/*
  ====================================================================
  AZULEJO

  Built for the Coastline server network
  Copyright (C) 2025-2026
  Some base code copyright (C) 2010-2014 Albert Pham and contributors
  Please see LICENSE.txt for more information.
  ====================================================================
*/

package com.thrandos.azulejo.launcher.fx;

import javafx.application.Preloader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Preloader for the JavaFX launcher.
 */
public class LauncherPreloader extends Preloader {
    
    private Stage preloaderStage;
    private ProgressBar progressBar;
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        this.preloaderStage = primaryStage;
        
        Label loadingLabel = new Label("Loading Azulejo Launcher...");
        progressBar = new ProgressBar();
        progressBar.setPrefWidth(300);
        
        VBox root = new VBox(10);
        root.getChildren().addAll(loadingLabel, progressBar);
        root.setStyle("-fx-padding: 20; -fx-alignment: center;");
        
        Scene scene = new Scene(root, 350, 100);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Azulejo Launcher");
        primaryStage.show();
    }
    
    @Override
    public void handleProgressNotification(ProgressNotification pn) {
        if (progressBar != null) {
            progressBar.setProgress(pn.getProgress());
        }
    }
    
    @Override
    public void handleStateChangeNotification(StateChangeNotification evt) {
        if (evt.getType() == StateChangeNotification.Type.BEFORE_START) {
            if (preloaderStage != null) {
                preloaderStage.hide();
            }
        }
    }
}