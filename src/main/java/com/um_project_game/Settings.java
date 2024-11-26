package com.um_project_game;

import com.um_project_game.util.Buttons;
import com.um_project_game.util.SoundPlayer;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;

public class Settings {

    private Stage settingsStage;
    private SoundPlayer soundPlayer;

    // Relative volumes
    private double mainVolume;
    private double backgroundRelativeVolume;
    private double moveRelativeVolume;
    private double captureRelativeVolume;

    public Settings(SoundPlayer soundPlayer, Pane root, Scene scene) {
        this.soundPlayer = soundPlayer;
        initializeSettingsWindow(root, scene);
    }

    private void initializeSettingsWindow(Pane root, Scene scene) {
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

        // DQN Bot Setting
        Label dqnbotLabel = new Label("Enable DQN Bot:");
        dqnbotLabel.getStyleClass().add("settings-label");

        final Buttons[] dqnbotToggleButton = new Buttons[1];

        dqnbotToggleButton[0] =
                new Buttons(
                        Launcher.dqnbot ? "Disable" : "Enable",
                        100,
                        30,
                        () -> {
                            Launcher.dqnbot = !Launcher.dqnbot;
                            dqnbotToggleButton[0]
                                    .getButton()
                                    .setText(Launcher.dqnbot ? "Disable" : "Enable");
                            System.out.println(
                                    "DQN Bot is now: "
                                            + (Launcher.dqnbot ? "Enabled" : "Disabled"));
                        });

        // Dark mode
        Label darkModeLabel = new Label("Dark Mode:");
        darkModeLabel.getStyleClass().add("settings-label");

        Buttons darkModeButton =
                new Buttons(
                        "Toggle",
                        100,
                        30,
                        () -> {
                            Launcher.switchTheme();
                            Platform.runLater(
                                    () -> {
                                        Launcher.viewManager.getMenu().onResize(root, scene);
                                        Launcher.viewManager
                                                .getActiveGames()
                                                .forEach(game -> game.onResize(root, scene));
                                        Launcher.viewManager
                                                .getActiveGames()
                                                .forEach(
                                                        game -> game.getMainBoard().refreshBoard());
                                    });
                        });

        mainVolumeLabel.getStyleClass().add("settings-label");
        backgroundVolumeLabel.getStyleClass().add("settings-label");
        moveVolumeLabel.getStyleClass().add("settings-label");
        captureVolumeLabel.getStyleClass().add("settings-label");

        // Adding style classes to sliders
        mainVolumeSlider.getStyleClass().add("settings-slider");
        backgroundVolumeSlider.getStyleClass().add("settings-slider");
        moveVolumeSlider.getStyleClass().add("settings-slider");
        captureVolumeSlider.getStyleClass().add("settings-slider");

        // Add controls to the grid
        grid.add(mainVolumeLabel, 0, 0);
        grid.add(mainVolumeSlider, 1, 0);
        grid.add(backgroundVolumeLabel, 0, 1);
        grid.add(backgroundVolumeSlider, 1, 1);
        grid.add(moveVolumeLabel, 0, 2);
        grid.add(moveVolumeSlider, 1, 2);
        grid.add(captureVolumeLabel, 0, 3);
        grid.add(captureVolumeSlider, 1, 3);
        grid.add(dqnbotLabel, 0, 4);
        grid.add(dqnbotToggleButton[0].getButton(), 1, 4);
        grid.add(darkModeLabel, 0, 5);
        grid.add(darkModeButton.getButton(), 1, 5);

        // Add Save and Cancel buttons
        Buttons doneButtons = new Buttons("Done", 100, 30, () -> settingsStage.close());

        // Add buttons to a horizontal box
        HBox buttonBox = new HBox(10, doneButtons.getButton());
        buttonBox.setPadding(new Insets(10));

        // Create a VBox to hold all elements
        VBox content = new VBox(10, grid, buttonBox);

        // Create the scene and stage
        Scene settingScene = new Scene(content);
        URL cssUrl =
                getClass().getResource(Launcher.DARK_MODE ? "/dark-theme.css" : "/light-theme.css");
        if (cssUrl != null) {
            settingScene.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.err.println("Stylesheet not found");
        }

        settingsStage = new Stage();
        settingsStage.setTitle("Settings");
        Launcher.registerScene(settingScene); // Register the scene
        settingsStage.setScene(settingScene);

        // Make the settings window modal
        settingsStage.initModality(Modality.APPLICATION_MODAL);

        // Set up listeners
        setupListeners(
                mainVolumeSlider, backgroundVolumeSlider, moveVolumeSlider, captureVolumeSlider);
    }

    private void setupSlider(Slider slider) {
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
        slider.setMajorTickUnit(0.1);
        slider.setBlockIncrement(0.01);
    }

    private void setupListeners(
            Slider mainVolumeSlider,
            Slider backgroundVolumeSlider,
            Slider moveVolumeSlider,
            Slider captureVolumeSlider) {
        // Listener for Main Volume Slider
        mainVolumeSlider
                .valueProperty()
                .addListener(
                        (_, _, newValue) -> {
                            mainVolume = newValue.doubleValue();
                            soundPlayer.setMainVolume(mainVolume);

                            // Adjust other sliders
                            backgroundVolumeSlider.setValue(mainVolume);
                            moveVolumeSlider.setValue(mainVolume);
                            captureVolumeSlider.setValue(mainVolume);
                        });

        // Listener for Background Volume Slider
        backgroundVolumeSlider
                .valueProperty()
                .addListener(
                        (_, _, newValue) -> {
                            double effectiveVolume = newValue.doubleValue();
                            if (mainVolume != 0) {
                                backgroundRelativeVolume = effectiveVolume / mainVolume;
                                soundPlayer.setBackgroundVolume(backgroundRelativeVolume);
                            }
                            // Else, do nothing
                        });

        // Listener for Move Volume Slider
        moveVolumeSlider
                .valueProperty()
                .addListener(
                        (_, _, newValue) -> {
                            double effectiveVolume = newValue.doubleValue();
                            if (mainVolume != 0) {
                                moveRelativeVolume = effectiveVolume / mainVolume;
                                soundPlayer.setMoveVolume(moveRelativeVolume);
                            }
                            // Else, do nothing
                        });

        // Listener for Capture Volume Slider
        captureVolumeSlider
                .valueProperty()
                .addListener(
                        (_, _, newValue) -> {
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
