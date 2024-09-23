package com.um_project_game;

import java.net.URL;
import java.util.Optional;

import javafx.util.Duration;

import org.jetbrains.annotations.NotNull;

import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
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

        scene.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case ESCAPE:
                    showExitConfirmation();
                    break;
                default:
                    break;
            }
        });
        
        stage.setScene(scene);
        stage.show();
    }

    private void showExitConfirmation() {
        // Create a confirmation dialog
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Exit Confirmation");
        alert.setHeaderText("Are you sure you want to quit?");
        alert.setContentText("Do you really want to close the application?");

        // Customize the button labels
        ButtonType yesButton = new ButtonType("Yes, I'm sure", ButtonData.OK_DONE);
        ButtonType noButton = new ButtonType("Nah", ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(yesButton, noButton);

        // Show the dialog and wait for the user's response
        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == yesButton) {
            Platform.exit(); // Close the application
        }
    }

    public static void main(String[] args) {
        launch();
    }
}
