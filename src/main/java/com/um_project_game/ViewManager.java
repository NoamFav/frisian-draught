 package com.um_project_game;

import java.util.ArrayList;
import java.util.List;

import com.um_project_game.Server.MainServer;

import javafx.animation.PauseTransition;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

public class ViewManager {

    private Pane root;
    private Launcher launcher;
    private Menu menu;
    private Scene scene;
    private MainServer server;
    private List<Game> activeGames = new ArrayList<>();
    private int GAME_STATE = 0; // 0 = Menu, 1 = Game, 2 = Multiplayer

    public ViewManager(Pane root, Launcher launcher, Scene scene) {
        this.server = new MainServer();
        this.root = root;
        this.launcher = launcher;
        this.scene = scene;
    }

    public MainServer getServer() {
        return server;
    }

    public void gameStateSwitch(int state) {
        GAME_STATE = state;
        switch (GAME_STATE) {
            case 0:
                if (server.isRunning()) {
                    server.close();
                    System.out.println("Server closed");
                }

                root.getChildren().clear();
                root.getChildren().add(menu.getMenuRoot());
                break;
            case 1:
            case 2:
                // Start a new game
                boolean isMultiplayer = (GAME_STATE == 2);

                if (isMultiplayer) {
                    System.out.println("Starting multiplayer game");
                    if (server.isRunning()) {
                        server.close();
                        System.out.println("Server closed");
                    }
                    server = new MainServer(); // Create a new server instance
                    System.out.println("Starting server");
                    Thread serverThread = new Thread(server);
                    serverThread.setDaemon(true);
                    serverThread.start();
                    System.out.println("Server started");
                } else {
                    if (server != null && server.isRunning()) {
                        server.close();
                        System.out.println("Server closed");
                    }
                }

                Game game = new Game(isMultiplayer, launcher);

                activeGames.add(game);
                game.showGameWindow();
                break;
            default:
                break;
        }
    }

    public void closeGame(Game gameToClose) {
        activeGames.remove(gameToClose);
        root.getChildren().remove(gameToClose.getGameRoot());
        if (activeGames.isEmpty()) {
            gameStateSwitch(0); // Go back to menu
        }
    }
}
