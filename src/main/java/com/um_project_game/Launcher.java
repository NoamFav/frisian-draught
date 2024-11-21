package com.um_project_game;

import com.um_project_game.Server.MainServer;
import com.um_project_game.util.SoundPlayer;

import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URL;
import java.util.Optional;

public class Launcher extends Application {
    private PauseTransition resizePause;
    public static SoundPlayer soundPlayer = new SoundPlayer();
    public static Settings settings;
    public static final int REF_WIDTH = 1366;
    public static final int REF_HEIGHT = 768;
    public static final ViewManager viewManager =
            new ViewManager(
                    new Pane(), new Launcher(), new Scene(new Pane(), REF_WIDTH, REF_HEIGHT));

    public static Stage menuStage;

    @Override
    public void start(Stage stage) {
        menuStage = stage;
        settings = new Settings(soundPlayer, viewManager.getRoot());
        setupMenuStage(menuStage);
    }

    private void setupMenuStage(Stage stage) {
        Pane root = new Pane();
        Scene scene = new Scene(root, REF_WIDTH, REF_HEIGHT);
        stage.setTitle("Frisian Draughts - Menu");

        URL cssUrl = getClass().getResource("/stylesheet.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.err.println("Stylesheet not found");
        }

        scene.setOnKeyPressed(
                event -> {
                    switch (event.getCode()) {
                        case ESCAPE:
                            showExitConfirmation();
                            break;
                        default:
                            break;
                    }
                });

        stage.setScene(scene);

        // Create and display the menu
        Menu menu = new Menu(root, scene, this);

        resizePause = new PauseTransition(Duration.millis(50));
        resizePause.setOnFinished(event -> menu.onResize(root, scene));

        // Add resize listeners
        scene.widthProperty()
                .addListener((observable, oldValue, newValue) -> resizePause.playFromStart());
        scene.heightProperty()
                .addListener((observable, oldValue, newValue) -> resizePause.playFromStart());

        // Handle close event
        stage.setOnCloseRequest(
                e -> {
                    // If there are no other windows open, exit the application
                    if (Stage.getWindows().size() <= 1) {
                        // This is the last window, so exit
                        Platform.exit();
                    } else {
                        // Just hide the menu window
                        menuStage = null;
                        stage.hide();
                    }
                });

        stage.show();
    }

    /**
     *
     * @param isOnline Is Online-Game?
     * @param againstBot Is Against Bot?
     */
    public void startNewGame(boolean isOnline, boolean againstBot) {
        viewManager.gameStateSwitch(isOnline ? 1 : againstBot ? 2 : 3);
    }

    public void closeMenu() {
        if (menuStage != null) {
            menuStage.close();
            menuStage = null;
        }
    }

    public void showMenu() {
        viewManager.gameStateSwitch(0);
        if (menuStage == null) {
            Stage newMenuStage = new Stage();
            menuStage = newMenuStage;
            setupMenuStage(menuStage);
        } else {
            menuStage.show();
        }
    }

    public void foolproofExit() {
        showExitConfirmation();
    }

    private void showExitConfirmation() {
        // Create a confirmation dialog
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Exit Confirmation");
        alert.setHeaderText("Are you sure you want to quit?");
        alert.setContentText("Do you really want to close the application?");

        // Customize the button labels
        ButtonType yesButton = new ButtonType("Yes, I'm sure", ButtonData.OK_DONE);
        ButtonType noButton = new ButtonType("Nah, I'm good", ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(yesButton, noButton);

        // Show the dialog and wait for the user's response
        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == yesButton) {
            MainServer server = viewManager.getServer();
            if (server.isRunning()) {
                server.close();
                System.out.println("Server closed");
            }
            Platform.exit(); // Close the application
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
