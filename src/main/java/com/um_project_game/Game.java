package com.um_project_game;

import com.um_project_game.Server.NetworkClient;
import com.um_project_game.board.GameInfo;
import com.um_project_game.board.MainBoard;
import com.um_project_game.util.Buttons;
import com.um_project_game.util.GameExporter;

import javafx.animation.*;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import org.joml.Vector2i;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Consumer;

public class Game {

    private MainBoard mainBoard = new MainBoard();

    public MainBoard getMainBoard() {
        return mainBoard;
    }

    private Timeline gameTimerPlayerOne;
    private Timeline gameTimerPlayerTwo;
    private int gameTimeLimit = 10 * 60; // 10 minutes in seconds
    private int remainingTimePlayerOne = gameTimeLimit;
    private int remainingTimePlayerTwo = gameTimeLimit;
    private GridPane board;
    private GameInfo gameInfo = new GameInfo();
    private BooleanBinding isWhiteTurn;

    private boolean isMultiplayer;
    private boolean isAgainstBot;
    private boolean isBotvBot;

    private Player playerWhite;
    private Player playerBlack;
    private List<Player> spectators = new ArrayList<>();

    private PauseTransition resizePause;

    private Pane gameRoot;
    private Launcher launcher;
    private Stage gameStage;

    // Dimensions
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

    // Game client communication
    private NetworkClient networkClient;

    /* --------------------------------------------------------------------------------
     *                               CONSTRUCTORS
     * -------------------------------------------------------------------------------- */

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

        // Fade in effect on first load
        animateFadeIn(gameRoot, 300);

        // Debounced resizing
        resizePause = new PauseTransition(Duration.millis(50));
        resizePause.setOnFinished(_ -> onResize(gameRoot, scene));

        scene.widthProperty().addListener((_, _, _) -> resizePause.playFromStart());
        scene.heightProperty().addListener((_, _, _) -> resizePause.playFromStart());

        // Handle close event
        this.gameStage.setOnCloseRequest(
                e -> {
                    e.consume();
                    showExitConfirmation();
                });
    }

    public Game(Launcher launcher, MainBoard mainBoard) {
        this.launcher = launcher;
        this.gameStage = new Stage();
        this.gameStage.setTitle("Frisian Draughts - Game");
        this.movesListGridPane = mainBoard.getMovesListGridPane();
        this.mainBoard = mainBoard;
        this.isWhiteTurn =
                mainBoard.boardState.isWhiteTurn()
                        ? Bindings.createBooleanBinding(() -> true)
                        : Bindings.createBooleanBinding(() -> false);

        // Disable board interaction when loading from PDN
        mainBoard.getBoard().setOnMouseClicked(null);

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
        board = mainBoard.getBoard(this.movesListGridPane, this.gameInfo);
        playerUI(gameRoot, scene, true);
        playerUI(gameRoot, scene, false);
        chatUI(gameRoot, scene);
        buttonGameLogic(gameRoot, scene);
        moveList(gameRoot, scene);

        // Fade in effect on first load
        animateFadeIn(gameRoot, 300);

        // Debounced resizing
        resizePause = new PauseTransition(Duration.millis(50));
        resizePause.setOnFinished(_ -> onResize(gameRoot, scene));

        scene.widthProperty().addListener((_, _, _) -> resizePause.playFromStart());
        scene.heightProperty().addListener((_, _, _) -> resizePause.playFromStart());

        // Handle close event
        this.gameStage.setOnCloseRequest(
                e -> {
                    e.consume();
                    showExitConfirmation();
                });
    }

    public void showGameWindow() {
        this.gameStage.show();
    }

    public Pane getGameRoot() {
        return gameRoot;
    }

    /* --------------------------------------------------------------------------------
     *                               BOARD METHODS
     * -------------------------------------------------------------------------------- */
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
        try {
            networkClient = new NetworkClient("localhost", 9000, this);
            System.out.println("Connected to server at localhost:9000");
        } catch (Exception e) {
            e.printStackTrace();
        }
        board =
                mainBoard.getMainBoardMultiplayer(
                        root,
                        mainBoardSize,
                        new Vector2i(mainBoardX, mainBoardY),
                        gameInfo,
                        movesListGridPane,
                        true,
                        networkClient);
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

    /* --------------------------------------------------------------------------------
     *                               PLAYER UI
     * -------------------------------------------------------------------------------- */
    private void playerUI(Pane root, Scene scene, boolean isPlayerOne) {
        StackPane playerUI = new StackPane();
        playerUI.setPrefSize(scene.getWidth(), playerUIHeight);

        // Positioning based on player
        playerUI.setLayoutX(0);
        playerUI.setLayoutY(!isPlayerOne ? 0 : scene.getHeight() - playerUIHeight);

        playerUI.getStyleClass().add("playerUI");
        playerUI.setId(isPlayerOne ? "playerOne" : "playerTwo");

        Consumer<Text> setPlayerStyle =
                player -> {
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

        // Player text and score setup
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

        // Timer setup for the entire game
        Text playerTime = new Text("Time: 10:00");
        playerTime.getStyleClass().add("label");
        setPlayerStyle.accept(playerTime);
        playerTime.setId(isPlayerOne ? "playerOneTime" : "playerTwoTime");

        // Initialize separate timers for Player 1 and Player 2
        if (isPlayerOne && gameTimerPlayerOne == null) {
            gameTimerPlayerOne =
                    new Timeline(
                            new KeyFrame(
                                    Duration.seconds(1),
                                    e -> {
                                        remainingTimePlayerOne--;
                                        int minutes = remainingTimePlayerOne / 60;
                                        int seconds = remainingTimePlayerOne % 60;
                                        playerTime.setText(
                                                String.format("Time: %02d:%02d", minutes, seconds));

                                        if (remainingTimePlayerOne <= 0) {
                                            gameTimerPlayerOne.stop();
                                            handleTimeUp(isPlayerOne);
                                        }
                                    }));
            gameTimerPlayerOne.setCycleCount(Timeline.INDEFINITE);
            gameTimerPlayerOne.play();
        }

        if (!isPlayerOne && gameTimerPlayerTwo == null) {
            gameTimerPlayerTwo =
                    new Timeline(
                            new KeyFrame(
                                    Duration.seconds(1),
                                    e -> {
                                        remainingTimePlayerTwo--;
                                        int minutes = remainingTimePlayerTwo / 60;
                                        int seconds = remainingTimePlayerTwo % 60;
                                        playerTime.setText(
                                                String.format("Time: %02d:%02d", minutes, seconds));

                                        if (remainingTimePlayerTwo <= 0) {
                                            gameTimerPlayerTwo.stop();
                                            handleTimeUp(isPlayerOne);
                                        }
                                    }));
            gameTimerPlayerTwo.setCycleCount(Timeline.INDEFINITE);
            gameTimerPlayerTwo.play();
        }

        // UI setup with spacing
        HBox playerInfo = new HBox(playerText, playerScore, playerTime);
        playerInfo.getStyleClass().add("playerInfo");
        playerInfo.setSpacing(playerInfoSpacing);
        playerInfo.setAlignment(javafx.geometry.Pos.CENTER);

        playerUI.getChildren().add(playerInfo);
        root.getChildren().add(playerUI);

        // OPTIONAL: Add a pulse animation for the active player
        if ((isWhiteTurn.get() && isPlayerOne) || (!isWhiteTurn.get() && !isPlayerOne)) {
            animatePulse(playerUI, 1.05, 500); // Pulse to emphasize active player's turn
        }
    }

    /* --------------------------------------------------------------------------------
     *                            HANDLE TIME UP EVENT
     * -------------------------------------------------------------------------------- */
    private void handleTimeUp(boolean isPlayerOne) {
        Alert timeUpAlert = new Alert(Alert.AlertType.INFORMATION);
        timeUpAlert.setTitle("Time's Up!");
        timeUpAlert.setHeaderText("Game Over!");
        timeUpAlert.setContentText(
                isPlayerOne ? "Player 1 ran out of time!" : "Player 2 ran out of time!");
        timeUpAlert.showAndWait();
        gameStage.close(); // Automatically close the game window
    }

    /* --------------------------------------------------------------------------------
     *                                CHAT UI
     * -------------------------------------------------------------------------------- */
    private void chatUI(Pane root, Scene scene) {
        StackPane chatUI = new StackPane();
        chatUI.setPrefSize(chatUIWidth, chatUIHeight);
        chatUI.setLayoutX(chatUIX);
        chatUI.setLayoutY(chatUIY);
        chatUI.getStyleClass().add("chatUI");
        chatUI.setId("chatUI");

        Text chatText = new Text("Chat");
        chatText.getStyleClass().add("label");
        chatUI.getChildren().add(chatText);

        root.getChildren().add(chatUI);
    }

    /* --------------------------------------------------------------------------------
     *                            EXIT CONFIRMATION
     * -------------------------------------------------------------------------------- */
    private void showExitConfirmation() {

        if (ExitGameConfirmation.showSaveConfirmation(true)) {
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
            launcher.showMenu();
        }

        fadeOutAndClose(gameStage, 300);

        gameStage.close();
    }

    /* --------------------------------------------------------------------------------
     *                           CONTROL BUTTONS
     * -------------------------------------------------------------------------------- */
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
                            mainBoard.moveManager.undoLastMove();
                        });
        Buttons drawButton = new Buttons("Draw", buttonWidth, buttonHeight, this::drawWarning);
        Buttons resignButton =
                new Buttons(
                        "Resign",
                        buttonWidth,
                        buttonHeight,
                        () -> {
                            System.out.println("Resign");
                        });
        Buttons restartButton =
                new Buttons("Restart", buttonWidth, buttonHeight, this::restartWarning);
        Buttons settingsButton =
                new Buttons("Settings", buttonWidth, buttonHeight, Launcher.settings::show);
        Buttons exitButton =
                new Buttons("Exit", buttonWidth, buttonHeight, this::showExitConfirmation);

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

    /* --------------------------------------------------------------------------------
     *                            MOVES LIST
     * -------------------------------------------------------------------------------- */
    private void moveList(Pane root, Scene scene) {
        StackPane movesList = new StackPane();
        movesList.setPrefSize(movesListWidth, movesListHeight);
        movesList.setMaxWidth(movesListWidth);
        movesList.setLayoutX(movesListX);
        movesList.setLayoutY(movesListY);
        movesList.getStyleClass().add("movesList");

        Text movesListText = new Text("Moves List");
        movesListText.getStyleClass().add("label");

        movesListGridPane = mainBoard.getMovesListGridPane();
        if (movesListGridPane == null) {
            System.err.println(
                    "Error: movesListGridPane is null. Please check getMovesListGridPane() in"
                            + " MainBoard.");
            return;
        }

        // Three columns: Turn - White - Black
        int numColumns = 3;
        double columnWidth = movesListWidth / numColumns;

        for (int i = 0; i < numColumns; i++) {
            movesListGridPane.getColumnConstraints().add(new ColumnConstraints(columnWidth));
        }

        ScrollPane scrollPane = new ScrollPane(movesListGridPane);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.getStyleClass().add("movesListScrollPane");

        VBox scrollPaneWrapper = new VBox(scrollPane);
        scrollPaneWrapper.getStyleClass().add("movesListScrollPane");
        scrollPaneWrapper.setPadding(new Insets(10));
        scrollPaneWrapper.setPrefSize(movesListWidth, movesListHeight);

        movesList.getChildren().add(scrollPaneWrapper);
        root.getChildren().add(movesList);
    }

    /* --------------------------------------------------------------------------------
     *                     RESTART & DRAW CONFIRMATIONS
     * -------------------------------------------------------------------------------- */
    private void restartWarning() {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Restart Confirmation");
        alert.setHeaderText("Are you sure you want to restart the game?");
        alert.setContentText("All progress will be lost.");

        // when restart the game also reset the timer
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {

            // Stop the timers if they are running
            if (gameTimerPlayerOne != null) {
                gameTimerPlayerOne.stop();
                remainingTimePlayerOne = gameTimeLimit; // Reset Player 1 time
            }
            if (gameTimerPlayerTwo != null) {
                gameTimerPlayerTwo.stop();
                remainingTimePlayerTwo = gameTimeLimit; // Reset Player 2 time
            }

            // Reset the board and game state
            mainBoard.resetGame(mainBoardSize);

            // Restart the timers for both players with full time
            startTimers();
        }
    }

    /**
     * Initializes and starts the countdown timers for both Player One and Player Two. Each player's
     * timer counts down from the preset game time limit. If a player's timer reaches zero, the game
     * will end for that player. This method is called when the game starts or restarts.
     */
    private void startTimers() {
        // Timer for Player One
        gameTimerPlayerOne =
                new Timeline(
                        new KeyFrame(
                                Duration.seconds(1),
                                e -> {
                                    remainingTimePlayerOne--;
                                    int minutes = remainingTimePlayerOne / 60;
                                    int seconds = remainingTimePlayerOne % 60;
                                    // Update the Player One UI time
                                    ((Text) gameRoot.lookup("#playerOneTime"))
                                            .setText(
                                                    String.format(
                                                            "Time: %02d:%02d", minutes, seconds));

                                    if (remainingTimePlayerOne <= 0) {
                                        gameTimerPlayerOne.stop();
                                        handleTimeUp(true);
                                    }
                                }));
        gameTimerPlayerOne.setCycleCount(Timeline.INDEFINITE);
        gameTimerPlayerOne.play();

        // Timer for Player Two
        gameTimerPlayerTwo =
                new Timeline(
                        new KeyFrame(
                                Duration.seconds(1),
                                e -> {
                                    remainingTimePlayerTwo--;
                                    int minutes = remainingTimePlayerTwo / 60;
                                    int seconds = remainingTimePlayerTwo % 60;
                                    // Update the Player Two UI time
                                    ((Text) gameRoot.lookup("#playerTwoTime"))
                                            .setText(
                                                    String.format(
                                                            "Time: %02d:%02d", minutes, seconds));

                                    if (remainingTimePlayerTwo <= 0) {
                                        gameTimerPlayerTwo.stop();
                                        handleTimeUp(false);
                                    }
                                }));
        gameTimerPlayerTwo.setCycleCount(Timeline.INDEFINITE);
        gameTimerPlayerTwo.play();
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

    /* --------------------------------------------------------------------------------
     *                            RESIZING LOGIC
     * -------------------------------------------------------------------------------- */
    public void onResize(Pane root, Scene scene) {
        root.getChildren().clear();
        newDimension(scene);

        // Rebuild everything
        resizeBoard(root);
        playerUI(root, scene, true);
        playerUI(root, scene, false);
        chatUI(root, scene);
        buttonGameLogic(root, scene);
        moveList(root, scene);

        // Fade in after resizing
        animateFadeIn(root, 300);
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

    /* --------------------------------------------------------------------------------
     *                           ANIMATION HELPERS
     * -------------------------------------------------------------------------------- */

    /** Smooth fade-in of the given parent node over a specified duration (in ms). */
    private void animateFadeIn(Pane parent, int durationMs) {
        parent.setOpacity(0); // Start fully transparent
        FadeTransition ft = new FadeTransition(Duration.millis(durationMs), parent);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
    }

    /**
     * Scale/pulse effect for a node. For example, call this on a player's UI if it's their turn.
     * The node scales up and then back down once.
     */
    private void animatePulse(Pane node, double scaleTo, int durationMs) {
        ScaleTransition st = new ScaleTransition(Duration.millis(durationMs), node);
        st.setFromX(1.0);
        st.setFromY(1.0);
        st.setToX(scaleTo);
        st.setToY(scaleTo);
        st.setAutoReverse(true);
        st.setCycleCount(2); // Go up, then back down
        st.play();
    }

    /**
     * Optional: fade out the window before closing. If you want to use it, call
     * fadeOutAndClose(gameStage, 300) in showExitConfirmation().
     */
    private void fadeOutAndClose(Stage stage, int durationMs) {
        FadeTransition ft =
                new FadeTransition(Duration.millis(durationMs), stage.getScene().getRoot());
        ft.setFromValue(1);
        ft.setToValue(0);
        ft.setOnFinished(_ -> stage.close());
        ft.play();
    }

    /* --------------------------------------------------------------------------------
     *                           SERVER COMMUNICATION
     * -------------------------------------------------------------------------------- */

    public void appendChatMessage(String message, String name) {
        StackPane chatUI = (StackPane) gameRoot.lookup("#chatUI");
        chatUI.getChildren().add(new Text(name + ": " + message));
    }

    public void setPlayerRole(String role, String name) {
        switch (role) {
            case "WHITE":
                playerWhite = new Player(name, true);
                break;
            case "BLACK":
                playerBlack = new Player(name, false);
                break;
            case "SPEC":
                spectators.add(new Player(name, false, true));
                break;
            default:
                throw new IllegalArgumentException("Invalid role: " + role);
        }
    }
}
