package com.um_project_game;

import com.um_project_game.util.Buttons;
import com.um_project_game.board.MainBoard;

import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import org.joml.Vector2i;

public class Menu {

    /**
     * @param root
     */
    public Menu(Pane root, Scene scene) {
        
        VBox controlButtons = new VBox();
        controlButtons.setSpacing(10);
        controlButtons.setLayoutX(50);
        controlButtons.setLayoutY(100);

        Runnable nill = () -> {}; // TODO: Implement the actions for the buttons
        int buttonWidth = (int)((scene.getWidth() / 2) - controlButtons.getLayoutX());
        int buttonHeight = 50;

        Buttons startGameButton = new Buttons("Start Game", buttonWidth, buttonHeight, nill);
        Buttons multiplayerButton = new Buttons("Multiplayer", buttonWidth, buttonHeight, nill);
        Buttons tutorialButton = new Buttons("Tutorial", buttonWidth, buttonHeight, nill);
        Buttons settingsButton = new Buttons("Settings", buttonWidth, buttonHeight, nill);

        new MainBoard(root, 200, new Vector2i(50, (int)(settingsButton.getButton().getLayoutY() - 20)));

        controlButtons.getChildren().addAll(startGameButton.getButton(), multiplayerButton.getButton(), tutorialButton.getButton(), settingsButton.getButton());

        root.getChildren().addAll(controlButtons);
    }
}
