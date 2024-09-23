package com.um_project_game;

import com.um_project_game.board.MainBoard;
import com.um_project_game.util.Buttons;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

public class Menu {

    private MainBoard mainBoard = new MainBoard();

    /**
     * @param root
     */
    public Menu(Pane root, Scene scene) {
        setTopBar(scene, root);
        setBottomBar(scene, root);
        setMenuButtons(scene, root);
        setRecentGames(scene, root);
        setLiveGame(scene, root);
        setVersionStatus(scene, root);
    }

    private void setTopBar(Scene scene, Pane root) {

        Rectangle topBar = new Rectangle(0, 0, scene.getWidth(), 75);
        topBar.setFill(Color.WHITE);
        topBar.setStroke(Color.BLACK);

        Text windowTitle = new Text("Frisian draughts");
        windowTitle.setFill(Color.BLACK);

        StackPane titlePane = new StackPane();
        titlePane.getChildren().addAll(topBar, windowTitle);

        root.getChildren().add(titlePane);
    }

    private void setBottomBar(Scene scene, Pane root) {

        float bottomBarHeight = 55;
        Rectangle bottomBar = new Rectangle(0, scene.getHeight() - bottomBarHeight, scene.getWidth(), bottomBarHeight);
        bottomBar.setFill(Color.WHITE);
        bottomBar.setStroke(Color.BLACK);

        Text bottomBarText = new Text("© 2021 UM Project - Version 1.0.0");
        bottomBarText.setFill(Color.BLACK);

        StackPane bottomBarPane = new StackPane();
        bottomBarPane.getChildren().addAll(bottomBar, bottomBarText);
        bottomBarPane.setLayoutY(scene.getHeight() - bottomBarHeight);

        root.getChildren().add(bottomBarPane);
    }

    private void setMenuButtons(Scene scene, Pane root) {
        VBox controlButtons = new VBox();
        controlButtons.setSpacing(15);
        controlButtons.setLayoutX(75);
        controlButtons.setLayoutY(115);

        Runnable nill = () -> {}; // TODO: Implement the actions for the buttons
        int buttonWidth = 510;
        int buttonHeight = 60;

        Buttons startGameButton = new Buttons("Start Game", buttonWidth, buttonHeight, nill);
        Buttons multiplayerButton = new Buttons("Multiplayer", buttonWidth, buttonHeight, nill);
        Buttons tutorialButton = new Buttons("Tutorial", buttonWidth, buttonHeight, nill);
        Buttons settingsButton = new Buttons("Settings", buttonWidth, buttonHeight, nill);

        controlButtons.getChildren().addAll(startGameButton.getButton(), multiplayerButton.getButton(), tutorialButton.getButton(), settingsButton.getButton());

        root.getChildren().addAll(controlButtons);
    }

    private void setRecentGames(Scene scene, Pane root) {

        VBox recentBoards = new VBox();
        recentBoards.setSpacing(15);
        recentBoards.setLayoutX(75);
        recentBoards.setLayoutY(422);

        Text recentBoardsTitle = new Text("Recent Boards");

        float boardSize = 225;
        HBox recentGames = new HBox();
        recentGames.setSpacing(105);
        for (int i = 0; i < 3; i++) recentGames.getChildren().add(mainBoard.getRandomBoard(root, boardSize));

        recentBoards.getChildren().addAll(recentBoardsTitle, recentGames);

        root.getChildren().addAll(recentBoards);
    }

    private void setLiveGame(Scene scene, Pane root) {

        Text liveGameTitle = new Text("Live Game");

        GridPane liveboard = mainBoard.getRandomBoard(root, 225);

        VBox liveGame = new VBox();
        liveGame.setSpacing(15);
        liveGame.setAlignment(Pos.CENTER);
        liveGame.setLayoutY(120);
        liveGame.setLayoutX(695);

        liveGame.getChildren().addAll(liveGameTitle, liveboard);
        root.getChildren().addAll(liveGame);
    }

    private void setVersionStatus(Scene scene, Pane root) {

        Rectangle versionStatus = new Rectangle(99, 1030, 300, 570);
        versionStatus.setFill(Color.TRANSPARENT);

        Text versionStatusText = new Text("Version Status");
        versionStatusText.setFill(Color.BLACK);

        StackPane versionStatusPane = new StackPane();
        versionStatusPane.getChildren().addAll(versionStatus, versionStatusText);
        versionStatusPane.setLayoutX(1030);
        versionStatusPane.setLayoutY(99);
        versionStatusPane.getStyleClass().add("version-status");

        root.getChildren().add(versionStatusPane);
    }
}
