package com.um_project_game;

import com.um_project_game.util.Buttons;
import com.um_project_game.util.Colors;

import javafx.scene.Scene;
import javafx.scene.layout.Pane;

public class Menu {

    public Menu(Pane root) {
        Buttons startGameButton = new Buttons("Start Game", 200, 100, 200, 50);
        Buttons exitButton = new Buttons("Exit", 200, 200, 200, 50);
        
        exitButton.getButton().setOnAction(e -> {
            System.exit(0);
        });

        root.getChildren().addAll(startGameButton.getButton(), exitButton.getButton());
    }
}
