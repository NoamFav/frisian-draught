package com.um_project_game;

import com.um_project_game.util.Buttons;
import com.um_project_game.util.SoundPlayer;

import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URL;

/**
 * The Settings class is responsible for managing the settings of the application. Handles the
 * volume control and toggling of dark mode.
 */
public class Settings {

    private Stage settingsStage;
    private SoundPlayer soundPlayer;

    // Relative volumes
    private double mainVolume;
    private double backgroundRelativeVolume;
    private double moveRelativeVolume;
    private double captureRelativeVolume;

    /**
     * Constructor for the Settings class
     *
     * @param soundPlayer The SoundPlayer object
     * @param root The root pane of the application
     * @param scene The scene object
     */
    public Settings(SoundPlayer soundPlayer, Pane root, Scene scene) {
        this.soundPlayer = soundPlayer;
        initializeSettingsWindow(root, scene);
    }

    /**
     * Initializes the settings window with sliders for volume control and buttons for toggling DQN
     * bot and dark mode.
     *
     * @param root The root pane of the application
     * @param scene The scene object
     */
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
                                                        game ->
                                                                game.getMainBoard()
                                                                        .boardRendered
                                                                        .refreshBoard());
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

        grid.add(darkModeLabel, 0, 5);
        grid.add(darkModeButton.getButton(), 1, 5);

        // Add "Done" button
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
        Launcher.registerScene(settingScene);
        settingsStage.setScene(settingScene);

        // Make the settings window modal
        settingsStage.initModality(Modality.APPLICATION_MODAL);

        animateHoverScale(doneButtons.getButton(), 1.05);

        animateHoverScale(darkModeButton.getButton(), 1.05);

        // Set up listeners
        setupListeners(
                mainVolumeSlider, backgroundVolumeSlider, moveVolumeSlider, captureVolumeSlider);
    }

    /**
     * Sets up the sliders for volume control.
     *
     * @param slider The slider object
     */
    private void setupSlider(Slider slider) {
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
        slider.setMajorTickUnit(0.1);
        slider.setBlockIncrement(0.01);
    }

    /**
     * Sets up listeners for the volume sliders.
     *
     * @param mainVolumeSlider The main volume slider
     * @param backgroundVolumeSlider The background volume slider
     * @param moveVolumeSlider The move volume slider
     * @param captureVolumeSlider The capture volume slider
     */
    private void setupListeners(
            Slider mainVolumeSlider,
            Slider backgroundVolumeSlider,
            Slider moveVolumeSlider,
            Slider captureVolumeSlider) {
        // Listener for Main Volume Slider
        mainVolumeSlider
                .valueProperty()
                .addListener(
                        (_, _, newVal) -> {
                            mainVolume = newVal.doubleValue();
                            soundPlayer.setMainVolume(mainVolume);

                            // Keep background, move, and capture sliders in sync proportionally
                            backgroundVolumeSlider.setValue(mainVolume);
                            moveVolumeSlider.setValue(mainVolume);
                            captureVolumeSlider.setValue(mainVolume);
                        });

        // Listener for Background Volume Slider
        backgroundVolumeSlider
                .valueProperty()
                .addListener(
                        (_, _, newVal) -> {
                            double effectiveVolume = newVal.doubleValue();
                            if (mainVolume != 0) {
                                backgroundRelativeVolume = effectiveVolume / mainVolume;
                                soundPlayer.setBackgroundVolume(backgroundRelativeVolume);
                            }
                        });

        // Listener for Move Volume Slider
        moveVolumeSlider
                .valueProperty()
                .addListener(
                        (_, _, newVal) -> {
                            double effectiveVolume = newVal.doubleValue();
                            if (mainVolume != 0) {
                                moveRelativeVolume = effectiveVolume / mainVolume;
                                soundPlayer.setMoveVolume(moveRelativeVolume);
                            }
                        });

        // Listener for Capture Volume Slider
        captureVolumeSlider
                .valueProperty()
                .addListener(
                        (_, _, newVal) -> {
                            double effectiveVolume = newVal.doubleValue();
                            if (mainVolume != 0) {
                                captureRelativeVolume = effectiveVolume / mainVolume;
                                soundPlayer.setCaptureVolume(captureRelativeVolume);
                            }
                        });
    }

    /** Shows the settings window. Also includes a quick fade-in for a smoother appearance. */
    public void show() {
        settingsStage.show();

        // OPTIONAL: fade in the settings window
        Platform.runLater(
                () -> {
                    animateFadeIn(settingsStage.getScene().getRoot(), 250);
                });
    }

    /* --------------------------------------------------------------------------------
     *                          ANIMATION HELPERS
     * -------------------------------------------------------------------------------- */

    /** Animate fade-in of a Node over a given duration (ms). */
    private void animateFadeIn(Node node, int durationMs) {
        node.setOpacity(0);
        javafx.animation.FadeTransition ft =
                new javafx.animation.FadeTransition(Duration.millis(durationMs), node);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
    }

    /** Slight hover scale transition. Node scales up on mouse enter, returns on mouse exit. */
    private void animateHoverScale(Node node, double scaleFactor) {
        node.setOnMouseEntered(
                _ -> {
                    ScaleTransition st = new ScaleTransition(Duration.millis(150), node);
                    st.setToX(scaleFactor);
                    st.setToY(scaleFactor);
                    st.play();
                });
        node.setOnMouseExited(
                _ -> {
                    ScaleTransition st = new ScaleTransition(Duration.millis(150), node);
                    st.setToX(1.0);
                    st.setToY(1.0);
                    st.play();
                });
    }
}
