package com.um_project_game;

import com.um_project_game.util.Buttons;

import javafx.scene.layout.Pane;

public class Menu {

    /**
     * @param root
     */
    public Menu(Pane root) {
        Buttons startGameButton = new Buttons("Start Game", 200, 100, 200, 50, () -> System.out.println("Game started"));
        Buttons exitButton = new Buttons("Exit", 200, 200, 200, 50, () -> System.exit(0));

        root.getChildren().addAll(startGameButton.getButton(), exitButton.getButton());
    }
}
