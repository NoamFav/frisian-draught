package com.um_project_game;

import com.um_project_game.util.Buttons;
import com.um_project_game.board.MainBoard;

import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import org.joml.Vector2i;

public class Menu {

    private MainBoard mainBoard = new MainBoard();

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

        controlButtons.getChildren().addAll(startGameButton.getButton(), multiplayerButton.getButton(), tutorialButton.getButton(), settingsButton.getButton());
        
        int recentBoardsY = (int)(controlButtons.getLayoutY() + (controlButtons.getSpacing() + buttonHeight) * controlButtons.getChildren().size());

        VBox recentBoards = new VBox();
        recentBoards.setSpacing(20);
        recentBoards.setLayoutX(50);
        recentBoards.setLayoutY(recentBoardsY + 50);

        Text recentBoardsTitle = new Text("Recent Boards");

        float boardSize = 400;
        HBox recentGames = new HBox();
        recentGames.setSpacing(100);
        for (int i = 0; i < 3; i++) {
            recentGames.getChildren().add(mainBoard.getRandomBoard(root, boardSize));
        }

        recentBoards.getChildren().addAll(recentBoardsTitle, recentGames);

        root.getChildren().addAll(controlButtons, recentBoards);
    }
}
