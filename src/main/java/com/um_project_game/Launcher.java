package com.um_project_game;

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
        Scene scene = new Scene(root, 320, 240);
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

    public void randomMethod() {
        System.out.println("Random method");
    }
}
