package com.thrandos.azulejo.launcher.ui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

public class LauncherApp extends Application {

    @Override
    public void start(Stage stage) {
        WebView webView = new WebView();
        WebEngine engine = webView.getEngine();

        engine.load(
            getClass().getResource("/ui/index.html").toExternalForm()
        );

        Scene scene = new Scene(new StackPane(webView), 900, 600);

        stage.setTitle("Azulejo Launcher");
        stage.setScene(scene);
        stage.show();
    }
}
