package com.um_project_game;

import javafx.application.Platform;
import javafx.scene.layout.Pane;
import javafx.scene.Scene;

import java.util.ArrayList;
import java.util.List;

public class ViewManager {

    private Pane root;
    private Launcher launcher;
    private Menu menu;
    private Scene scene;
    private List<Game> activeGames = new ArrayList<>();
    private int GAME_STATE = 0; // 0 = Menu, 1 = Game, 2 = Multiplayer

    public ViewManager(Pane root, Launcher launcher, Scene scene) {
        this.root = root;
        this.launcher = launcher;
        this.scene = scene;
    }

    public void gameStateSwitch(int state) {
        GAME_STATE = state;
        switch (GAME_STATE) {
            case 0:
                // Show menu
                if (menu == null) {
                    menu = new Menu(root, scene, launcher);
                }
                root.getChildren().clear();
                root.getChildren().add(menu.getMenuRoot());
                break;
            case 1:
            case 2:
                // Start a new game
                boolean isMultiplayer = (GAME_STATE == 2);
                Game game = new Game(isMultiplayer, launcher);
                activeGames.add(game);
                root.getChildren().add(game.getGameRoot());
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
