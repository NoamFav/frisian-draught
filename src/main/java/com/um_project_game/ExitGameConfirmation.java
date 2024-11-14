package com.um_project_game;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.util.Optional;

public class ExitGameConfirmation {

    private boolean saveOnExit = false;

    public boolean showAndWait() {
        Alert exitAlert = new Alert(AlertType.CONFIRMATION);
        exitAlert.setTitle("Exit Confirmation");
        exitAlert.setHeaderText("Are you sure you want to exit the game?");
        exitAlert.setContentText("Choose 'Yes' to exit, or 'No' to stay in the game.");

        CheckBox saveCheckbox = new CheckBox("Save the game before exiting");

        VBox content = new VBox();
        content.getChildren().add(saveCheckbox);
        exitAlert.getDialogPane().setContent(content);

        ButtonType yesButton = ButtonType.OK;
        ButtonType noButton = ButtonType.CANCEL;
        exitAlert.getButtonTypes().setAll(yesButton, noButton);

        Optional<ButtonType> result = exitAlert.showAndWait();
        saveOnExit = saveCheckbox.isSelected();

        return result.isPresent() && result.get() == yesButton;
    }

    public boolean shouldSaveOnExit() {
        return saveOnExit;
    }
}
