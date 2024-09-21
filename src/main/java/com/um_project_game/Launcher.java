package com.um_project_game;

import java.net.URL;

import org.jetbrains.annotations.NotNull;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

/**
 * Launcher for the app
 */
public class Launcher extends Application {
    @Override
    public void start(@NotNull Stage stage) {
        Pane root = new Pane();
        Scene scene = new Scene(root, 1920, 1080);
        stage.setTitle("Hello!");

        URL cssUrl = getClass().getResource("/stylesheet.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.err.println("Stylesheet not found");
        }

        new Menu(root, scene);

        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
