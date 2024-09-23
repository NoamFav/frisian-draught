package com.um_project_game;

import java.net.URL;
import javafx.util.Duration;

import org.jetbrains.annotations.NotNull;

import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

/**
 * Launcher for the app
 */
public class Launcher extends Application {

    private PauseTransition resizePause; 

    @Override
    public void start(@NotNull Stage stage) {
        Pane root = new Pane();
        Scene scene = new Scene(root, 1366, 768);
        stage.setTitle("Hello!");

        URL cssUrl = getClass().getResource("/stylesheet.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.err.println("Stylesheet not found");
        }

        Menu menu = new Menu(root, scene);

        resizePause = new PauseTransition(Duration.millis(50));
        resizePause.setOnFinished(event -> {
            menu.onResize(scene, root, (int) scene.getWidth(), (int) scene.getHeight());
        });

        // Add resize listeners
        scene.widthProperty().addListener((observable, oldValue, newValue) -> {
            resizePause.playFromStart(); // Restart the pause every time the size changes
        });

        scene.heightProperty().addListener((observable, oldValue, newValue) -> {
            resizePause.playFromStart(); // Restart the pause every time the size changes
        });

        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
