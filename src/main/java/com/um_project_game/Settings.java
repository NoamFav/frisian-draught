package com.um_project_game;

import com.um_project_game.util.SoundPlayer;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class Settings {

    private Stage settingsStage;
    private SoundPlayer soundPlayer;

    // Relative volumes
    private double mainVolume;
    private double backgroundRelativeVolume;
    private double moveRelativeVolume;
    private double captureRelativeVolume;

    public Settings(SoundPlayer soundPlayer) {
        this.soundPlayer = soundPlayer;
        initializeSettingsWindow();
    }

    private void initializeSettingsWindow() {
        // Store initial relative volumes
        mainVolume = soundPlayer.getMainVolume();
        backgroundRelativeVolume = soundPlayer.getBackgroundVolume();
        moveRelativeVolume = soundPlayer.getMoveVolume();
        captureRelativeVolume = soundPlayer.getCaptureVolume();

        // Create a GridPane for better layout
        GridPane grid = new GridPane();
        grid.setVgap(10);
        grid.setHgap(10);
        grid.setPadding(new Insets(20));

        // Main Volume
        Label mainVolumeLabel = new Label("Main Volume:");
        Slider mainVolumeSlider = new Slider(0, 1, mainVolume);
        setupSlider(mainVolumeSlider);

        // Background Volume
        Label backgroundVolumeLabel = new Label("Background Volume:");
        Slider backgroundVolumeSlider = new Slider(0, 1, backgroundRelativeVolume * mainVolume);
        setupSlider(backgroundVolumeSlider);

        // Move Volume
        Label moveVolumeLabel = new Label("Move Volume:");
        Slider moveVolumeSlider = new Slider(0, 1, moveRelativeVolume * mainVolume);
        setupSlider(moveVolumeSlider);

        // Capture Volume
        Label captureVolumeLabel = new Label("Capture Volume:");
        Slider captureVolumeSlider = new Slider(0, 1, captureRelativeVolume * mainVolume);
        setupSlider(captureVolumeSlider);

        // Add controls to the grid
        grid.add(mainVolumeLabel, 0, 0);
        grid.add(mainVolumeSlider, 1, 0);
        grid.add(backgroundVolumeLabel, 0, 1);
        grid.add(backgroundVolumeSlider, 1, 1);
        grid.add(moveVolumeLabel, 0, 2);
        grid.add(moveVolumeSlider, 1, 2);
        grid.add(captureVolumeLabel, 0, 3);
        grid.add(captureVolumeSlider, 1, 3);

        // Add Save and Cancel buttons
        Button saveButton = new Button("Save");
        Button cancelButton = new Button("Cancel");

        saveButton.setOnAction(e -> settingsStage.close());
        cancelButton.setOnAction(e -> {
            // Optionally revert changes if needed
            settingsStage.close();
        });

        // Add buttons to a horizontal box
        HBox buttonBox = new HBox(10, saveButton, cancelButton);
        buttonBox.setPadding(new Insets(10));

        // Create a VBox to hold all elements
        VBox content = new VBox(10, grid, buttonBox);

        // Create the scene and stage
        Scene scene = new Scene(content);
        settingsStage = new Stage();
        settingsStage.setTitle("Settings");
        settingsStage.setScene(scene);

        // Make the settings window modal
        settingsStage.initModality(Modality.APPLICATION_MODAL);

        // Set up listeners
        setupListeners(mainVolumeSlider, backgroundVolumeSlider, moveVolumeSlider, captureVolumeSlider);
    }

    private void setupSlider(Slider slider) {
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
        slider.setMajorTickUnit(0.1);
        slider.setBlockIncrement(0.01);
    }

    private void setupListeners(Slider mainVolumeSlider, Slider backgroundVolumeSlider, Slider moveVolumeSlider, Slider captureVolumeSlider) {
        // Listener for Main Volume Slider
        mainVolumeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            mainVolume = newValue.doubleValue();
            soundPlayer.setMainVolume(mainVolume);

            // Adjust other sliders
            backgroundVolumeSlider.setValue(backgroundRelativeVolume * mainVolume);
            moveVolumeSlider.setValue(moveRelativeVolume * mainVolume);
            captureVolumeSlider.setValue(captureRelativeVolume * mainVolume);
        });

        // Listener for Background Volume Slider
        backgroundVolumeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            double effectiveVolume = newValue.doubleValue();
            if (mainVolume != 0) {
                backgroundRelativeVolume = effectiveVolume / mainVolume;
                soundPlayer.setBackgroundVolume(backgroundRelativeVolume);
            }
            // Else, do nothing
        });

        // Listener for Move Volume Slider
        moveVolumeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            double effectiveVolume = newValue.doubleValue();
            if (mainVolume != 0) {
                moveRelativeVolume = effectiveVolume / mainVolume;
                soundPlayer.setMoveVolume(moveRelativeVolume);
            }
            // Else, do nothing
        });

        // Listener for Capture Volume Slider
        captureVolumeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            double effectiveVolume = newValue.doubleValue();
            if (mainVolume != 0) {
                captureRelativeVolume = effectiveVolume / mainVolume;
                soundPlayer.setCaptureVolume(captureRelativeVolume);
            }
            // Else, do nothing
        });
    }

    public void show() {
        settingsStage.showAndWait();
    }
}
