/*
  ====================================================================
  AZULEJO

  Built for the Coastline server network
  Copyright (C) 2025
  Some base code copyright (C) 2010-2014 Albert Pham and contributors
  Please see LICENSE.txt for more information.
  ====================================================================
*/

package com.thrandos.azulejo.launcher.fx;

import com.thrandos.azulejo.launcher.Launcher;
import com.thrandos.azulejo.launcher.LauncherArguments;
import com.thrandos.azulejo.launcher.util.SharedLocale;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.extern.java.Log;

import java.io.IOException;
import java.util.logging.Level;

/**
 * JavaFX Application entry point for the launcher.
 */
@Log
public class LauncherApp extends Application {
    
    private Launcher launcher;
    private Stage primaryStage;
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        
        // Ensure JavaFX doesn't exit when last window closes
        Platform.setImplicitExit(false);
        
        // Initialize the launcher
        try {
            launcher = Launcher.createFromArguments(getParameters().getRaw().toArray(new String[0]));
        } catch (Exception e) {
            log.log(Level.SEVERE, "Failed to initialize launcher", e);
            throw new RuntimeException("Failed to initialize launcher", e);
        }
        
        // Load the main FXML
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/skcraft/launcher/fx/launcher.fxml"));
            Parent root = loader.load();
            
            // Get the controller and set the launcher instance
            LauncherController controller = loader.getController();
            controller.setLauncher(launcher);
            
            Scene scene = new Scene(root, 1000, 700);
            
            // Add CSS styling
            scene.getStylesheets().add(getClass().getResource("/com/skcraft/launcher/fx/launcher.css").toExternalForm());
            
            primaryStage.setTitle("Azulejo Launcher");
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(800);
            primaryStage.setMinHeight(500);
            
            // Set close behavior
            primaryStage.setOnCloseRequest(event -> {
                Platform.setImplicitExit(true);
                Platform.exit();
            });
            
            primaryStage.show();
            
            // Bring window to front
            primaryStage.toFront();
            primaryStage.requestFocus();
            
        } catch (IOException e) {
            log.log(Level.SEVERE, "Failed to load JavaFX FXML", e);
            throw new RuntimeException("Failed to load launcher UI", e);
        }
    }
    
    /**
     * Get the primary stage.
     * @return the primary stage
     */
    public Stage getPrimaryStage() {
        return primaryStage;
    }
    
    /**
     * Get the launcher instance.
     * @return the launcher
     */
    public Launcher getLauncher() {
        return launcher;
    }
    
    public static void main(String[] args) {
        // Set system properties for JavaFX
        System.setProperty("javafx.preloader", "com.thrandos.azulejo.launcher.fx.LauncherPreloader");
        
        // Initialize locale
        SharedLocale.loadBundle("com.thrandos.azulejo.launcher.lang.Launcher", java.util.Locale.getDefault());
        
        launch(args);
    }
}