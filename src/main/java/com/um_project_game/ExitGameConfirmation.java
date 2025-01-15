package com.um_project_game;

import com.um_project_game.util.ExitChoice;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.net.URL;

public class ExitGameConfirmation {

    // Method 1: Show Exit Confirmation
    public static boolean showExitConfirmation() {
        // Create a custom stage (borderless window)
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.UNDECORATED);

        // Create header
        Label headerLabel = new Label("Exit Confirmation");
        headerLabel.getStyleClass().add("header-label");

        // Create message
        Label messageLabel = new Label("Are you sure you want to quit?");
        messageLabel.getStyleClass().add("message-label");

        // Create buttons
        Button yesButton = new Button("Yes");
        Button noButton = new Button("No");
        yesButton.getStyleClass().add("button");
        noButton.getStyleClass().add("button");

        // Button actions
        final boolean[] result = {false};
        yesButton.setOnAction(
                _ -> {
                    result[0] = true;
                    stage.close();
                });
        noButton.setOnAction(_ -> stage.close());

        // Layout buttons
        HBox buttonBox = new HBox(10, noButton, yesButton);
        buttonBox.setAlignment(Pos.CENTER);

        // Layout root
        VBox root = new VBox(15, headerLabel, messageLabel, buttonBox);
        root.getStyleClass().add("alert-root");
        root.setAlignment(Pos.CENTER);

        // Create the scene
        Scene scene = new Scene(new StackPane(root));
        stage.setScene(scene);

        // Load the CSS theme
        URL cssUrl =
                ExitGameConfirmation.class.getResource(
                        Launcher.DARK_MODE ? "/dark-theme.css" : "/light-theme.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        }

        // Show the stage
        stage.showAndWait();

        return result[0];
    }

    // Method 2: Show Save Confirmation with Checkbox
    public static ExitChoice showSaveConfirmation(boolean canSave) {
        // Create a custom stage
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.UNDECORATED);

        // Create header
        Label headerLabel = new Label("Exit Game");
        headerLabel.getStyleClass().add("header-label");

        // Create message
        Label messageLabel = new Label("Are you sure you want to exit this game?");
        messageLabel.getStyleClass().add("message-label");

        // Create checkbox (conditionally)
        CheckBox saveCheckbox = null;
        if (canSave) {
            saveCheckbox = new CheckBox("Save the game before exiting");
            saveCheckbox.setSelected(true);
            saveCheckbox.getStyleClass().add("checkbox");
        }

        // Create buttons
        Button yesButton = new Button("Yes");
        Button noButton = new Button("No");
        yesButton.getStyleClass().add("button");
        noButton.getStyleClass().add("button");

        // Button actions
        final ExitChoice[] result = {ExitChoice.NOT_EXIT};
        CheckBox finalSaveCheckbox = saveCheckbox;
        yesButton.setOnAction(
                _ -> {
                    result[0] = (finalSaveCheckbox != null && finalSaveCheckbox.isSelected())
                            ? ExitChoice.EXIT_WITH_SAVE
                            : ExitChoice.EXIT_WITHOUT_SAVE;
                    stage.close();
                });
        noButton.setOnAction(
                _ -> {
                    stage.close();
                });

        // Layout buttons
        HBox buttonBox = new HBox(10, noButton, yesButton);
        buttonBox.setAlignment(Pos.CENTER);

        // Layout root
        VBox root;
        if (canSave) {
            root = new VBox(15, headerLabel, messageLabel, saveCheckbox, buttonBox);
        } else {
            root = new VBox(15, headerLabel, messageLabel, buttonBox);
        }
        root.getStyleClass().add("alert-root");
        root.setAlignment(Pos.CENTER);

        // Create the scene
        Scene scene = new Scene(new StackPane(root));
        stage.setScene(scene);

        // Load the CSS theme
        URL cssUrl =
                ExitGameConfirmation.class.getResource(
                        Launcher.DARK_MODE ? "/dark-theme.css" : "/light-theme.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        }

        // Show the stage
        stage.showAndWait();

        return result[0];
    }

}
