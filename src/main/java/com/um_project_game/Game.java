package com.um_project_game;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.joml.Vector2i;

import com.um_project_game.board.MainBoard;
import com.um_project_game.util.Buttons;

import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class Game {

    private final MainBoard mainBoard = new MainBoard();
    private GridPane board;
    private boolean isWhiteTurn = mainBoard.isWhiteTurn();

    private int scorePlayerOne = 0;
    private int scorePlayerTwo = 0;

    private int mainBoardSize = 614;
    private int mainBoardX = 376;
    private int mainBoardY = 77;

    private int playerUIHeight = 60;
    private int playerInfoSpacing = 100;

    private int chatUIHeight = 570;
    private int chatUIWidth = 252;
    private int chatUIX = 32;
    private int chatUIY = 99;

    private int buttonWidth = 269;
    private int buttonHeight = 50;
    private int buttonSpacing = 12;

    private int controlButtonsX = 1069;
    private int controlButtonsY = 99;

    private int movesListX = 1069;
    private int movesListY = 469;
    private int movesListWidth = buttonWidth;
    private int movesListHeight = 222;

    public Game(Pane root, Scene scene) {
        mainGameBoard(root, scene);
        playerUI(root, scene, true);
        playerUI(root, scene, false);
        chatUI(root, scene);
        buttonGameLogic(root, scene);
        moveList(root, scene);
    }

    private void mainGameBoard(Pane root, Scene scene) {
        board = mainBoard.getMainBoard(root, mainBoardSize, new Vector2i(mainBoardX, mainBoardY));
        board.getStyleClass().add("mainboard");
        root.getChildren().add(board);
    }

    private void playerUI(Pane root, Scene scene, boolean isPlayerOne) {
        StackPane playerUI = new StackPane();
        playerUI.setPrefSize(scene.getWidth(), playerUIHeight);
        playerUI.setLayoutX(0);
        playerUI.setLayoutY(!isPlayerOne ? 0 : scene.getHeight() - playerUIHeight);
        playerUI.getStyleClass().add("playerUI");
        playerUI.setId(isPlayerOne ? "playerOne" : "playerTwo");

        Consumer<Text> setPlayerStyle = (player) -> {
            if (player != null) {
                boolean shouldBeBold = (isWhiteTurn && isPlayerOne) || (!isWhiteTurn && !isPlayerOne);
                player.setStyle("-fx-font-size: " + (shouldBeBold ? 20 : 15) + ";"
                                + "-fx-font-weight: " + (shouldBeBold ? "bold" : "normal"));
            }
        };

        Text playerText = new Text(isPlayerOne ? "Player 1" : "Player 2");
        playerText.getStyleClass().add("playerText");
        setPlayerStyle.accept(playerText);
        playerText.setId(isPlayerOne ? "playerOneText" : "playerTwoText");

        Text playerScore = new Text("Score: " + (isPlayerOne ? scorePlayerOne : scorePlayerTwo));
        playerScore.getStyleClass().add("playerScore");
        setPlayerStyle.accept(playerScore);
        playerScore.setId(isPlayerOne ? "playerOneScore" : "playerTwoScore");

        Text playerTime = new Text("Time: 10:00");
        playerTime.getStyleClass().add("playerTime");
        setPlayerStyle.accept(playerTime);
        playerTime.setId(isPlayerOne ? "playerOneTime" : "playerTwoTime");

        HBox playerInfo = new HBox(playerText, playerScore, playerTime);
        playerInfo.getStyleClass().add("playerInfo");
        playerInfo.setSpacing(playerInfoSpacing);
        playerInfo.setAlignment(javafx.geometry.Pos.CENTER);

        playerUI.getChildren().add(playerInfo);
 
        root.getChildren().add(playerUI);
    }

    private void chatUI(Pane root, Scene scene) {
        StackPane chatUI = new StackPane();
        chatUI.setPrefSize(chatUIWidth, chatUIHeight);
        chatUI.setLayoutX(chatUIX);
        chatUI.setLayoutY(chatUIY);
        chatUI.getStyleClass().add("chatUI");

        Text chatText = new Text("Chat");
        chatText.getStyleClass().add("chatText");

        chatUI.getChildren().add(chatText);

        root.getChildren().add(chatUI);
    }

    private void buttonGameLogic(Pane root, Scene scene) {
        VBox controlButtons = new VBox();
        controlButtons.setSpacing(buttonSpacing);
        controlButtons.setLayoutX(controlButtonsX);
        controlButtons.setLayoutY(controlButtonsY);

        Buttons undoButton = new Buttons("Undo", buttonWidth, buttonHeight, () -> {});
        Buttons drawButton = new Buttons("Draw", buttonWidth, buttonHeight, () -> {});
        Buttons resignButton = new Buttons("Resign", buttonWidth, buttonHeight, () -> System.out.println("Resign"));
        Buttons restartButton = new Buttons("Restart", buttonWidth, buttonHeight, () -> mainBoard.resetGame(board, mainBoardSize));
        Buttons settingsButton = new Buttons("Settings", buttonWidth, buttonHeight, () -> {});
        Buttons exitButton = new Buttons("Exit", buttonWidth, buttonHeight, () -> Launcher.changeState(0));

        controlButtons.getChildren().addAll(undoButton.getButton(), drawButton.getButton(), resignButton.getButton(), restartButton.getButton(), settingsButton.getButton(), exitButton.getButton());
        root.getChildren().addAll(controlButtons);
    }

    private void moveList(Pane root, Scene scene) {
        StackPane movesList = new StackPane();
        movesList.setPrefSize(movesListWidth, movesListHeight);
        movesList.setLayoutX(movesListX);
        movesList.setLayoutY(movesListY);
        movesList.getStyleClass().add("movesList");

        Text movesListText = new Text("Moves List");
        movesListText.getStyleClass().add("movesListText");

        movesList.getChildren().add(movesListText);

        root.getChildren().add(movesList);
    }

    public void onResize(Pane root, Scene scene) {
        root.getChildren().clear();
        newDimension(scene);
        mainGameBoard(root, scene);
        playerUI(root, scene, true);
        playerUI(root, scene, false);
        chatUI(root, scene);
        buttonGameLogic(root, scene);
        moveList(root, scene);
    }

    private void newDimension(Scene scene) {
        int newSceneWidth = (int) scene.getWidth();
        int newSceneHeight = (int) scene.getHeight();

        int referenceWidth = Launcher.REF_WIDTH;
        int referenceHeight = Launcher.REF_HEIGHT;

        mainBoardSize = convertDimensions(614, newSceneWidth, referenceWidth);
        mainBoardX = convertDimensions(376, newSceneWidth, referenceWidth);
        mainBoardY = convertDimensions(77, newSceneHeight, referenceHeight);

        playerUIHeight = convertDimensions(60, newSceneHeight, referenceHeight);
        playerInfoSpacing = convertDimensions(100, newSceneHeight, referenceHeight);

        chatUIHeight = convertDimensions(570, newSceneHeight, referenceHeight);
        chatUIWidth = convertDimensions(252, newSceneWidth, referenceWidth);
        chatUIX = convertDimensions(32, newSceneWidth, referenceWidth);
        chatUIY = convertDimensions(99, newSceneHeight, referenceHeight);

        buttonWidth = convertDimensions(269, newSceneWidth, referenceWidth);
        buttonHeight = convertDimensions(50, newSceneHeight, referenceHeight);
        buttonSpacing = convertDimensions(12, newSceneHeight, referenceHeight);

        controlButtonsX = convertDimensions(1069, newSceneWidth, referenceWidth);
        controlButtonsY = convertDimensions(99, newSceneHeight, referenceHeight);

        movesListX = controlButtonsX;
        movesListY = convertDimensions(469, newSceneHeight, referenceHeight);
        movesListWidth = buttonWidth;
        movesListHeight = convertDimensions(222, newSceneHeight, referenceHeight);
    }

    private int convertDimensions(int oldDimension, int newDimension, int oldReferenceDimension) {
        return (int) ((double) oldDimension * ((double) newDimension / (double) oldReferenceDimension));
    }
}
