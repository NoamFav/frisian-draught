package com.um_project_game;

import com.um_project_game.Server.MainServer;
import com.um_project_game.board.MainBoard;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Launcher extends Application {
    private PauseTransition resizePause;
    public static SoundPlayer soundPlayer = new SoundPlayer();
    public static Settings settings;
    public static final int REF_WIDTH = 1366;
    public static final int REF_HEIGHT = 768;
    public static boolean DARK_MODE = true;
    public static boolean dqnbot = true;
    public static final ViewManager viewManager =
            new ViewManager(
                    new Pane(), new Launcher(), new Scene(new Pane(), REF_WIDTH, REF_HEIGHT));
    public static Scene menuScene;
    public static final boolean isRecentGameToggleReady = false;

    private static final List<Scene> scenes = new ArrayList<>();

    public static void registerScene(Scene scene) {
        scenes.add(scene);
        applyTheme(scene);
    }

    public static void switchTheme() {
        Launcher.DARK_MODE = !Launcher.DARK_MODE;
        scenes.forEach(Launcher::applyTheme); // Apply the new theme to all scenes
    }

    public static Stage menuStage;

    @Override
    public void start(Stage stage) {
        menuStage = stage;
        settings = new Settings(soundPlayer, viewManager.getRoot(), viewManager.getScene());
        setupMenuStage(menuStage);
    }

    private void setupMenuStage(Stage stage) {
        Pane root = new Pane();
        Scene scene = new Scene(root, REF_WIDTH, REF_HEIGHT);
        Launcher.registerScene(scene);
        menuScene = scene;
        stage.setTitle("Frisian Draughts - Menu");

        URL cssUrl = getClass().getResource(DARK_MODE ? "/dark-theme.css" : "/light-theme.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
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
        viewManager.setMenu(menu);
        resizePause = new PauseTransition(Duration.millis(50));
        resizePause.setOnFinished(_ -> menu.onResize(root, scene));
        // Add resize listeners
        scene.widthProperty().addListener((_, _, _) -> resizePause.playFromStart());
        scene.heightProperty().addListener((_, _, _) -> resizePause.playFromStart());

        // Handle close event
        stage.setOnCloseRequest(
                _ -> {
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
     * @param isOnline Is Online-Game?
     * @param againstBot Is Against Bot?
     */
    public void startNewGame(boolean isOnline, boolean againstBot, boolean isBotVsBot) {

        if (isOnline) {
            viewManager.gameStateSwitch(1);
        } else if (againstBot) {
            viewManager.gameStateSwitch(isBotVsBot ? 4 : 2);
        } else {
            viewManager.gameStateSwitch(3);
        }
    }

    public void startNewGame(MainBoard board) {
        viewManager.gameStateSwitch(
                board.boardState.isMultiplayer() ? 1 : board.boardState.isBotActive() ? 2 : 3,
                board);
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

    public static void applyTheme(Scene scene) {
        URL cssUrl =
                Launcher.DARK_MODE
                        ? Launcher.class.getResource("/dark-theme.css")
                        : Launcher.class.getResource("/light-theme.css");

        if (cssUrl != null) {
            scene.getStylesheets().clear();
            scene.getStylesheets().add(cssUrl.toExternalForm());
            System.out.println("Applied theme: " + (Launcher.DARK_MODE ? "Dark" : "Light"));
        } else {
            System.err.println("Stylesheet not found.");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
