package com.thrandos.azulejo.launcher.fx;

import com.thrandos.azulejo.launcher.Launcher;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import java.net.URL;

public class LauncherFXApp extends Application {

    private static Launcher launcher;

    public static void setLauncher(Launcher l) {
        launcher = l;
    }

    @Override
public void start(Stage stage) {
    WebView webView = new WebView();

    URL ui = getClass().getResource("/launcher/ui/index.html");
    if (ui == null) {
        throw new IllegalStateException("Missing resource: /launcher/ui/index.html you're cooked buddy"); // slightly less professional exception message
    }

    webView.getEngine().load(ui.toExternalForm());

    stage.setTitle("Azulejo");
    stage.setScene(new Scene(webView, 1280, 800));
    stage.show();
}

}
