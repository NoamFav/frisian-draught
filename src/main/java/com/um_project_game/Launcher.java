package com.um_project_game;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;

import com.um_project_game.util.SoundPlayer;
import com.um_project_game.Server.MainServer;

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

/**
 * Launcher for the app
 */
public class Launcher extends Application {

    static SoundPlayer soundPlayer = new SoundPlayer();

    private PauseTransition resizePause;
    public static final int REF_WIDTH = 1366;
    public static final int REF_HEIGHT = 768;
    public static final MainServer server = new MainServer();
    public static int GAME_STATE = 0; // 0 = Menu, 1 = Game, 2 = Multiplayer
    private Stage primaryStage;
    private Menu menu;
    private List<Game> activeGames = new ArrayList<>();

    @Override
    public void start(@NotNull Stage stage) {
        this.primaryStage = stage;
        setupStage();
    }

    private void setupStage() {
        Pane root = new Pane();
        Scene scene = new Scene(root, 1366, 768);
        primaryStage.setTitle("Hello!");

        gameStateSwitch(root, scene);

        URL cssUrl = getClass().getResource("/stylesheet.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.err.println("Stylesheet not found");
        }

        scene.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case ESCAPE:
                    showExitConfirmation();
                    break;
                default:
                    break;
            }
        });

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void gameStateSwitch(Pane root, Scene scene) {
        switch (GAME_STATE) {
            case 0:
                menu = new Menu(root, scene);


                if (server.isRunning()) {
                    server.close();
                }

                resizePause = new PauseTransition(Duration.millis(50));
                resizePause.setOnFinished(event -> {
                    menu.onResize(root, scene);
                });

                // Add resize listeners
                scene.widthProperty().addListener((observable, oldValue, newValue) -> {
                    resizePause.playFromStart(); // Restart the pause every time the size changes
                });

                scene.heightProperty().addListener((observable, oldValue, newValue) -> {
                    resizePause.playFromStart(); // Restart the pause every time the size changes
                });

                break;
            case 1:
                if (server.isRunning()) {
                    server.close();
                }

                Game game = new Game(root, scene, false);
                activeGames.add(game);
                resizePause = new PauseTransition(Duration.millis(50));
                resizePause.setOnFinished(event -> {
                    game.onResize(root, scene);
                });

                // Add resize listeners
                scene.widthProperty().addListener((observable, oldValue, newValue) -> resizePause.playFromStart());
                scene.heightProperty().addListener((observable, oldValue, newValue) -> resizePause.playFromStart());

                break;
            case 2:
                Game gameMultiplayer = new Game(root, scene, true);
                activeGames.add(gameMultiplayer);

                if (server.isRunning()) {
                    server.close();
                }

                Thread serverThread = new Thread(server);
                serverThread.setDaemon(true);
                serverThread.start();

                resizePause = new PauseTransition(Duration.millis(50));
                resizePause.setOnFinished(event -> {
                    gameMultiplayer.onResize(root, scene);
                });

                // Add resize listeners
                scene.widthProperty().addListener((observable, oldValue, newValue) -> resizePause.playFromStart());
                scene.heightProperty().addListener((observable, oldValue, newValue) -> resizePause.playFromStart());
                break;
            default:
                break;
        }
    }

    public void closeGame(Game gameToClose) {
        activeGames.remove(gameToClose); // Remove the game from the list
        Platform.runLater(() -> {
            changeState(0); // Switch back to the menu
            setupStage(); // Refresh the stage with the menu
        });
    }

    public static void changeState(int newState) {
        GAME_STATE = newState;

        Platform.runLater(() -> {
            Launcher instance = new Launcher();
            Stage newStage = new Stage();
            instance.start(newStage);
        });
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
            if (server.isRunning()) {
                server.close();
            }
            Platform.exit(); // Close the application
        }
    }

    public static void main(String[] args) {
        soundPlayer.playBackgroundMusic();
        launch();
    }
}
