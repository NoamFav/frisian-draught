package com.um_project_game;

import com.um_project_game.board.MainBoard;
import com.um_project_game.util.Buttons;

import javafx.geometry.Pos;
import javafx.scene.Node;
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
 
    // Dimensions
    private int topBarHeight = 75;
    private int bottomBarHeight = 55;
    private int buttonWidth = 510;
    private int buttonHeight = 60;

    private int controlButtonsX = 75;
    private int controlButtonsY = 115;
    private int controlButtonsSpacing = 15;

    private int recentBoardsX = 75;
    private int recentBoardsY = 422;
    private int recentBoardsSpacingY = 15;
    private int recentBoardsSize = 225;
    private int recentBoardsSpacingX = 105;

    private int liveGameX = 695;
    private int liveGameY = 120;
    private int liveGameSpacing = 15; 
    private int liveGameSize = 225;

    private int versionStatusX = 1030;
    private int versionStatusY = 99;
    private int versionStatusWidth = 300;
    private int versionStatusHeight = 570;

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

        Rectangle topBar = new Rectangle(0, 0, scene.getWidth(), topBarHeight);
        topBar.setFill(Color.WHITE);
        topBar.setStroke(Color.BLACK);

        Text windowTitle = new Text("Frisian draughts");
        windowTitle.setFill(Color.BLACK);

        StackPane titlePane = new StackPane();
        titlePane.getChildren().addAll(topBar, windowTitle);
        titlePane.setId("top-bar");

        root.getChildren().add(titlePane);
    }

    private void setBottomBar(Scene scene, Pane root) {

        Rectangle bottomBar = new Rectangle(0, scene.getHeight() - bottomBarHeight, scene.getWidth(), bottomBarHeight);
        bottomBar.setFill(Color.WHITE);
        bottomBar.setStroke(Color.BLACK);

        Text bottomBarText = new Text("© 2024 UM Project - Version 1.0.0");
        bottomBarText.setFill(Color.BLACK);

        StackPane bottomBarPane = new StackPane();
        bottomBarPane.getChildren().addAll(bottomBar, bottomBarText);
        bottomBarPane.setLayoutY(scene.getHeight() - bottomBarHeight);
        bottomBarPane.setId("bottom-bar");
        root.getChildren().add(bottomBarPane);
    }

    private void setMenuButtons(Scene scene, Pane root) {
        VBox controlButtons = new VBox();
        controlButtons.setSpacing(controlButtonsSpacing);
        controlButtons.setLayoutX(controlButtonsX);
        controlButtons.setLayoutY(controlButtonsY);

        Runnable nill = () -> {};

        Buttons startGameButton = new Buttons("Start Game", buttonWidth, buttonHeight, () -> Launcher.changeState(1));
        Buttons multiplayerButton = new Buttons("Multiplayer", buttonWidth, buttonHeight, () -> Launcher.changeState(2));
        Buttons tutorialButton = new Buttons("Tutorial", buttonWidth, buttonHeight, nill);
        Buttons settingsButton = new Buttons("Settings", buttonWidth, buttonHeight, nill);

        controlButtons.getChildren().addAll(startGameButton.getButton(), multiplayerButton.getButton(), tutorialButton.getButton(), settingsButton.getButton());
        controlButtons.setId("control-buttons");
        root.getChildren().addAll(controlButtons);
    }

    private void setRecentGames(Scene scene, Pane root) {

        VBox recentBoards = new VBox();
        recentBoards.setSpacing(recentBoardsSpacingY);
        recentBoards.setLayoutX(recentBoardsX);
        recentBoards.setLayoutY(recentBoardsY);

        Text recentBoardsTitle = new Text("Recent Boards");

        float boardSize = recentBoardsSize;
        HBox recentGames = new HBox();
        recentGames.setSpacing(recentBoardsSpacingX);
        for (int i = 0; i < 3; i++) {
            MainBoard mainBoard = new MainBoard();
            recentGames.getChildren().add(mainBoard.getRandomBoard(root, boardSize));
        }

        recentBoards.getChildren().addAll(recentBoardsTitle, recentGames);
        recentBoards.setId("recent-boards");

        root.getChildren().addAll(recentBoards);
    }

    private void setLiveGame(Scene scene, Pane root) {

        Text liveGameTitle = new Text("Live Game");

        MainBoard mainBoard = new MainBoard();
        GridPane liveboard = mainBoard.getRandomBoard(root, liveGameSize);

        VBox liveGame = new VBox();
        liveGame.setSpacing(liveGameSpacing);
        liveGame.setAlignment(Pos.CENTER);
        liveGame.setLayoutX(liveGameX);
        liveGame.setLayoutY(liveGameY);

        liveGame.getChildren().addAll(liveGameTitle, liveboard);
        liveGame.setId("live-game");
        root.getChildren().addAll(liveGame);
    }

    private void setVersionStatus(Scene scene, Pane root) {

        Rectangle versionStatus = new Rectangle(versionStatusX, versionStatusY, versionStatusWidth, versionStatusHeight);
        versionStatus.setFill(Color.TRANSPARENT);

        Text versionStatusText = new Text("Version Status");
        versionStatusText.setFill(Color.BLACK);

        StackPane versionStatusPane = new StackPane();
        versionStatusPane.getChildren().addAll(versionStatus, versionStatusText);
        versionStatusPane.setLayoutX(versionStatusX);
        versionStatusPane.setLayoutY(versionStatusY);
        versionStatusPane.getStyleClass().add("version-status");
        versionStatusPane.setId("version-status");

        root.getChildren().add(versionStatusPane);
    }

    public void onResize(Pane root, Scene scene) {
        
        Node topBar = root.lookup("#top-bar");
        Node bottomBar = root.lookup("#bottom-bar");
        Node controlButtons = root.lookup("#control-buttons");
        Node recentBoards = root.lookup("#recent-boards");
        Node liveGame = root.lookup("#live-game");
        Node versionStatus = root.lookup("#version-status");

        if (topBar != null) root.getChildren().remove(topBar);
        if (bottomBar != null) root.getChildren().remove(bottomBar);
        if (controlButtons != null) root.getChildren().remove(controlButtons);
        if (recentBoards != null) root.getChildren().remove(recentBoards);
        if (liveGame != null) root.getChildren().remove(liveGame);
        if (versionStatus != null) root.getChildren().remove(versionStatus);

        newDimensions(scene);
        setTopBar(scene, root);
        setBottomBar(scene, root);
        setMenuButtons(scene, root);
        setRecentGames(scene, root);
        setLiveGame(scene, root);
        setVersionStatus(scene, root);
    }

    private void newDimensions(Scene scene) {
    // Calculate the ratios using oldVal and newVal for width and height respectively
    int newSceneWidth = (int) scene.getWidth();
    int newSceneHeight = (int) scene.getHeight();

    int oldValHeight = Launcher.REF_HEIGHT;
    int oldValWidth = Launcher.REF_WIDTH;

    topBarHeight = convertDimensions(75, newSceneHeight, oldValHeight); 
    bottomBarHeight = convertDimensions(55, newSceneHeight, oldValHeight);
    buttonWidth = convertDimensions(510, newSceneWidth, oldValWidth);
    buttonHeight = convertDimensions(60, newSceneHeight, oldValHeight);

    controlButtonsX = convertDimensions(75, newSceneWidth, oldValWidth);
    controlButtonsY = convertDimensions(115, newSceneHeight, oldValHeight);
    controlButtonsSpacing = convertDimensions(15, newSceneHeight, oldValHeight);

    recentBoardsX = convertDimensions(75, newSceneWidth, oldValWidth);
    recentBoardsY = convertDimensions(422, newSceneHeight, oldValHeight);
    recentBoardsSpacingY = convertDimensions(15, newSceneHeight, oldValHeight);
    recentBoardsSize = convertDimensions(225, newSceneHeight, oldValHeight);
    recentBoardsSpacingX = convertDimensions(105, newSceneWidth, oldValWidth);

    liveGameX = convertDimensions(695, newSceneWidth, oldValWidth);
    liveGameY = convertDimensions(120, newSceneHeight, oldValHeight);
    liveGameSpacing = convertDimensions(15, newSceneHeight, oldValHeight);
    liveGameSize = convertDimensions(225, newSceneHeight, oldValHeight);

    versionStatusX = convertDimensions(1030, newSceneWidth, oldValWidth);
    versionStatusY = convertDimensions(99, newSceneHeight, oldValHeight);
    versionStatusWidth = convertDimensions(300, newSceneWidth, oldValWidth);
    versionStatusHeight = convertDimensions(570, newSceneHeight, oldValHeight);
}

    private int convertDimensions(int oldDimension, int newDimension, int oldReferenceDimension) {
        return (int) ((double) oldDimension * ((double) newDimension / (double) oldReferenceDimension));
    }
}
