package com.um_project_game;

import com.um_project_game.board.GameInfo;
import com.um_project_game.board.MainBoard;
import com.um_project_game.util.Buttons;
import com.um_project_game.util.GameExporter;

import javafx.animation.PauseTransition;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import org.joml.Vector2i;

import java.net.URL;
import java.util.Optional;
import java.util.Random;
import java.util.function.Consumer;

public class Game {

    private MainBoard mainBoard = new MainBoard();

    public MainBoard getMainBoard() {
        return mainBoard;
    }

    private GridPane board;

    private GameInfo gameInfo = new GameInfo();
    private BooleanBinding isWhiteTurn;

    private boolean isMultiplayer;
    private boolean isAgainstBot;
    private boolean isBotvBot;

    private PauseTransition resizePause;

    private Pane gameRoot;
    private Launcher launcher;
    private Stage gameStage;

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
    private GridPane movesListGridPane = new GridPane();

    // Game export
    private GameExporter exporter = new GameExporter();

    public Game(boolean isMultiplayer, boolean isAgainstBot, boolean isBotvBot, Launcher launcher) {
        this.launcher = launcher;
        this.gameStage = new Stage();
        this.gameStage.setTitle("Frisian Draughts - Game");
        this.isMultiplayer = isMultiplayer;
        this.isAgainstBot = isAgainstBot;
        this.isBotvBot = isBotvBot;

        this.gameRoot = new Pane();
        Scene scene = new Scene(gameRoot, Launcher.REF_WIDTH, Launcher.REF_HEIGHT);
        Launcher.registerScene(scene);

        // Load CSS
        URL cssUrl =
                getClass().getResource(Launcher.DARK_MODE ? "/dark-theme.css" : "/light-theme.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.err.println("Stylesheet not found");
        }

        this.gameStage.setScene(scene);

        // Initialize game UI
        if (isMultiplayer) {
            mainGameBoardMultiplayer(gameRoot, scene);
        } else {
            mainGameBoard(gameRoot, scene, isAgainstBot);
        }
        playerUI(gameRoot, scene, true);
        playerUI(gameRoot, scene, false);
        chatUI(gameRoot, scene);
        buttonGameLogic(gameRoot, scene);
        moveList(gameRoot, scene);

        resizePause = new PauseTransition(Duration.millis(50));
        resizePause.setOnFinished(
                _ -> {
                    onResize(gameRoot, scene);
                });

        // Add resize listeners
        scene.widthProperty()
                .addListener(
                        (_, _, _) -> {
                            resizePause.playFromStart(); // Restart the pause every time the size
                            // changes
                        });

        scene.heightProperty()
                .addListener(
                        (_, _, _) -> {
                            resizePause.playFromStart(); // Restart the pause every time the size
                            // changes
                        });

        // Handle close event
        this.gameStage.setOnCloseRequest(
                e -> {
                    e.consume(); // Prevent the window from closing immediately
                    showExitConfirmation(); // Show the exit confirmation dialog
                });
    }

    public Game(Launcher launcher, MainBoard mainBoard) {
        this.launcher = launcher;
        this.gameStage = new Stage();
        this.gameStage.setTitle("Frisian Draughts - Game");
        this.movesListGridPane = mainBoard.getMovesListGridPane();
        this.mainBoard = mainBoard;
        this.isWhiteTurn =
                mainBoard.isWhiteTurn
                        ? Bindings.createBooleanBinding(() -> true)
                        : Bindings.createBooleanBinding(() -> false);
        mainBoard
                .getBoard()
                .setOnMouseClicked(null); // Disable board interaction when loading from PDN

        this.gameRoot = new Pane();
        Scene scene = new Scene(gameRoot, Launcher.REF_WIDTH, Launcher.REF_HEIGHT);
        Launcher.registerScene(scene);

        // Load CSS
        URL cssUrl =
                getClass().getResource(Launcher.DARK_MODE ? "/dark-theme.css" : "/light-theme.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.err.println("Stylesheet not found");
        }

        this.gameStage.setScene(scene);

        // Initialize game player

        System.out.println("list " + this.movesListGridPane);
        board = mainBoard.getBoard(this.movesListGridPane, this.gameInfo);
        playerUI(gameRoot, scene, true);
        playerUI(gameRoot, scene, false);
        chatUI(gameRoot, scene);
        buttonGameLogic(gameRoot, scene);
        moveList(gameRoot, scene);

        resizePause = new PauseTransition(Duration.millis(50));
        resizePause.setOnFinished(
                _ -> {
                    onResize(gameRoot, scene);
                });

        // Add resize listeners
        scene.widthProperty()
                .addListener(
                        (_, _, _) -> {
                            resizePause.playFromStart(); // Restart the pause every time the size
                            // changes
                        });

        scene.heightProperty()
                .addListener(
                        (_, _, _) -> {
                            resizePause.playFromStart(); // Restart the pause every time the size
                            // changes
                        });

        // Handle close event
        this.gameStage.setOnCloseRequest(
                e -> {
                    e.consume(); // Prevent the window from closing immediately
                    showExitConfirmation(); // Show the exit confirmation dialog
                });
    }

    public void showGameWindow() {
        this.gameStage.show();
    }

    public Pane getGameRoot() {
        return gameRoot;
    }

    private void mainGameBoard(Pane root, Scene scene, boolean isBotActive) {
        board =
                mainBoard.getMainBoard(
                        root,
                        mainBoardSize,
                        new Vector2i(mainBoardX, mainBoardY),
                        gameInfo,
                        movesListGridPane,
                        isBotActive,
                        isBotvBot);
        board.getStyleClass().add("mainboard");
        root.getChildren().add(board);

        isWhiteTurn = Bindings.equal(gameInfo.playerTurnProperty(), 1);
    }

    private void mainGameBoardMultiplayer(Pane root, Scene scene) {
        board =
                mainBoard.getMainBoardMultiplayer(
                        root,
                        mainBoardSize,
                        new Vector2i(mainBoardX, mainBoardY),
                        gameInfo,
                        movesListGridPane,
                        true);
        board.getStyleClass().add("mainboard");
        root.getChildren().add(board);

        isWhiteTurn = Bindings.equal(gameInfo.playerTurnProperty(), 1);
    }

    private void resizeBoard(Pane root) {
        board = mainBoard.resizeBoard(mainBoardSize);
        board.setLayoutX(mainBoardX);
        board.setLayoutY(mainBoardY);
        root.getChildren().add(board);
    }

    private void playerUI(Pane root, Scene scene, boolean isPlayerOne) {
        StackPane playerUI = new StackPane();
        playerUI.setPrefSize(scene.getWidth(), playerUIHeight);
        playerUI.setLayoutX(0);
        playerUI.setLayoutY(!isPlayerOne ? 0 : scene.getHeight() - playerUIHeight);
        playerUI.getStyleClass().add("playerUI");
        playerUI.setId(isPlayerOne ? "playerOne" : "playerTwo");

        Consumer<Text> setPlayerStyle =
                (player) -> {
                    if (player != null) {
                        boolean shouldBeBold =
                                (isWhiteTurn.get() && isPlayerOne)
                                        || (!isWhiteTurn.get() && !isPlayerOne);
                        player.setStyle(
                                "-fx-font-size: "
                                        + (shouldBeBold ? 20 : 15)
                                        + ";"
                                        + "-fx-font-weight: "
                                        + (shouldBeBold ? "bold" : "normal"));
                    }
                };

        Text playerText = new Text(isPlayerOne ? "Player 1" : "Player 2");
        playerText.getStyleClass().add("label");
        setPlayerStyle.accept(playerText);
        playerText.setId(isPlayerOne ? "playerOneText" : "playerTwoText");

        Text playerScore = new Text();
        playerScore.getStyleClass().add("label");
        playerScore.setId(isPlayerOne ? "playerOneScore" : "playerTwoScore");
        if (isPlayerOne) {
            playerScore
                    .textProperty()
                    .bind(Bindings.concat("Score: ", gameInfo.scorePlayerOneProperty()));
        } else {
            playerScore
                    .textProperty()
                    .bind(Bindings.concat("Score: ", gameInfo.scorePlayerTwoProperty()));
        }
        setPlayerStyle.accept(playerScore);

        Text playerTime = new Text("Time: 10:00");
        playerTime.getStyleClass().add("label");
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
        chatText.getStyleClass().add("label");

        chatUI.getChildren().add(chatText);

        root.getChildren().add(chatUI);
    }

    private void showExitConfirmation() {
        ExitGameConfirmation exitConfirmation = new ExitGameConfirmation();
        if (exitConfirmation.showAndWait()) { // If user confirmed exit
            if (exitConfirmation.shouldSaveOnExit()) {
                exporter.exportGameToPDN(
                        mainBoard.getTakenMoves(),
                        null,
                        isAgainstBot ? "1" : "0",
                        isMultiplayer ? "1" : "0",
                        gameInfo.getPlayerTurn() == 1 ? "W" : "B");

                Launcher.viewManager
                        .getMenu()
                        .onResize(Launcher.viewManager.getMenu().getMenuRoot(), Launcher.menuScene);
            }
            if (Launcher.menuStage == null) {
                // Recreate the menu
                launcher.showMenu();
            }
            gameStage.close(); // Close the game window
        }
    }

    private void buttonGameLogic(Pane root, Scene scene) {
        VBox controlButtons = new VBox();
        controlButtons.setSpacing(buttonSpacing);
        controlButtons.setLayoutX(controlButtonsX);
        controlButtons.setLayoutY(controlButtonsY);

        Buttons undoButton =
                new Buttons(
                        "Undo",
                        buttonWidth,
                        buttonHeight,
                        () -> {
                            mainBoard.undoLastMove();
                        });
        Buttons drawButton = new Buttons("Draw", buttonWidth, buttonHeight, () -> drawWarning());
        Buttons resignButton =
                new Buttons(
                        "Resign", buttonWidth, buttonHeight, () -> System.out.println("Resign"));
        Buttons restartButton =
                new Buttons("Restart", buttonWidth, buttonHeight, () -> restartWarning());
        Buttons settingsButton =
                new Buttons("Settings", buttonWidth, buttonHeight, Launcher.settings::show);
        Buttons exitButton =
                new Buttons("Exit", buttonWidth, buttonHeight, () -> showExitConfirmation());

        controlButtons
                .getChildren()
                .addAll(
                        undoButton.getButton(),
                        drawButton.getButton(),
                        resignButton.getButton(),
                        restartButton.getButton(),
                        settingsButton.getButton(),
                        exitButton.getButton());
        root.getChildren().addAll(controlButtons);
    }

    private void moveList(Pane root, Scene scene) {
        StackPane movesList = new StackPane();
        movesList.setPrefSize(movesListWidth, movesListHeight);
        movesList.setMaxWidth(movesListWidth);
        movesList.setLayoutX(movesListX);
        movesList.setLayoutY(movesListY);
        movesList.getStyleClass().add("movesList");

        Text movesListText = new Text("Moves List");
        movesListText.getStyleClass().add("label");

        // Fetch the moves list grid pane
        movesListGridPane = mainBoard.getMovesListGridPane();

        // Check if movesListGridPane is null
        if (movesListGridPane == null) {
            System.err.println(
                    "Error: movesListGridPane is null. Please check getMovesListGridPane() in"
                            + " MainBoard.");
            return;
        }

        // Three columns: Turn - White - Black
        int numColumns = 3;

        // Calculated width of columns
        double columnWidth = movesListWidth / numColumns;

        // Add column constraints
        for (int i = 0; i < numColumns; i++) {
            movesListGridPane.getColumnConstraints().add(new ColumnConstraints(columnWidth));
        }

        // Create a ScrollPane and add movesListGridPane to it
        ScrollPane scrollPane = new ScrollPane(movesListGridPane);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.getStyleClass().add("movesListScrollPane");

        VBox scrollPaneWrapper = new VBox(scrollPane);
        scrollPaneWrapper.getStyleClass().add("movesListScrollPane");
        scrollPaneWrapper.setPadding(new Insets(10));
        scrollPaneWrapper.setPrefSize(movesListWidth, movesListHeight);

        // Add to StackPane
        movesList.getChildren().add(scrollPaneWrapper);
        root.getChildren().add(movesList);
    }

    private void restartWarning() {

        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Restart Confirmation");
        alert.setHeaderText("Are you sure you want to restart the game?");
        alert.setContentText("All progress will be lost.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            mainBoard.resetGame(mainBoardSize);
        }
    }

    private void drawWarning() {

        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Draw Confirmation");
        alert.setHeaderText("Are you sure you want to propose a draw?");
        alert.setContentText("Both players will receive 1 point.");

        ButtonType yesButton = new ButtonType("Yes", ButtonData.OK_DONE);
        ButtonType noButton = new ButtonType("No", ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(yesButton, noButton);

        Optional<ButtonType> result = alert.showAndWait();

        if (result.get() == yesButton) {
            String botDecision = "Bot is thinking...";
            Alert botAlert = new Alert(AlertType.INFORMATION);
            botAlert.setTitle("Bot Decision");
            botAlert.setContentText(botDecision);
            botAlert.showAndWait();
            Random rand = new Random();
            int n = rand.nextInt(2);
            if (n == 0) {
                botDecision = "Bot has accepted the draw!";
                botAlert.setContentText("Game over - draw!");
            } else {
                botDecision = "Bot has declined the draw!";
                botAlert.setContentText("Game continues!");
            }
            botAlert.setHeaderText(botDecision);
            botAlert.showAndWait();
            if (n == 0) {
                mainBoard.resetGame(mainBoardSize);
            }
        }
    }

    public void onResize(Pane root, Scene scene) {
        root.getChildren().clear();
        newDimension(scene);
        resizeBoard(root);
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
        return (int)
                ((double) oldDimension * ((double) newDimension / (double) oldReferenceDimension));
    }
}
