package com.um_project_game;

import com.um_project_game.Server.NetworkClient;
import com.um_project_game.board.Bot.*;
import com.um_project_game.board.Bot.BotType;
import com.um_project_game.board.GameInfo;
import com.um_project_game.board.MainBoard;
import com.um_project_game.util.Buttons;
import com.um_project_game.util.ExitChoice;
import com.um_project_game.util.GameExporter;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.Pair;

import org.joml.Vector2i;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The Game class is responsible for managing the game state, including the board, players, timers,
 * and UI elements. It also handles server communication for multiplayer games.
 */
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

    private Player player;
    private Player opponent;

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

    public BotType playerBot;

    public BotType BotvsBotWhite;
    public BotType BotvsBotBlack;

    /* --------------------------------------------------------------------------------
     *                               CONSTRUCTORS
     * -------------------------------------------------------------------------------- */

    /**
     * Constructor for a single-player game.
     *
     * @param isMultiplayer
     * @param isAgainstBot
     * @param isBotvBot
     * @param launcher
     */
    public Game(boolean isMultiplayer, boolean isAgainstBot, boolean isBotvBot, Launcher launcher) {
        this(isMultiplayer, isAgainstBot, isBotvBot, launcher, null);
    }

    /**
     * Constructor for a single-player game loaded from a PDN file.
     *
     * @param isMultiplayer
     * @param isAgainstBot
     * @param isBotvBot
     * @param launcher
     * @param filePath
     */
    public Game(
            boolean isMultiplayer,
            boolean isAgainstBot,
            boolean isBotvBot,
            Launcher launcher,
            String filePath) {
        this.launcher = launcher;
        this.gameStage = new Stage();
        this.gameStage.setTitle("Frisian Draughts - Game");
        this.isMultiplayer = isMultiplayer;
        this.isAgainstBot = isAgainstBot;
        this.isBotvBot = isBotvBot;

        Launcher.user.playedGame();

        if (isBotvBot || isAgainstBot) {
            BotPicker botPicker = new BotPicker();
            if (isBotvBot) {
                Pair<BotType, BotType> botPair = botPicker.selectBotvsBot();
                BotvsBotWhite = botPair.getKey();
                BotvsBotBlack = botPair.getValue();
                System.out.println("Bot vs Bot: " + BotvsBotWhite + " vs " + BotvsBotBlack);
            } else {
                playerBot = botPicker.selectPlayerBot();
                System.out.println("Player vs Bot: " + playerBot);
            }
        }

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
            mainGameBoard(gameRoot, scene, isAgainstBot, filePath);
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

        if (isAgainstBot) {
            setBotPlayer(playerBot);
        } else if (isBotvBot) {
            setBotvsBotPlayers(BotvsBotWhite, BotvsBotBlack);
        }
    }

    /**
     * set the bot player
     *
     * @param botType
     */
    public void setBotPlayer(BotType botType) {
        mainBoard.boardState.setBotPlayer(BotFactory.createBot(botType, mainBoard));
    }

    /**
     * set the bot vs bot players
     *
     * @param whiteBot
     * @param blackBot
     */
    public void setBotvsBotPlayers(BotType whiteBot, BotType blackBot) {
        mainBoard.boardState.setBotvsBotWhite(BotFactory.createBot(whiteBot, mainBoard));
        mainBoard.boardState.setBotvsBotBlack(BotFactory.createBot(blackBot, mainBoard));
    }

    /**
     * public Game(Launcher launcher, MainBoard mainBoard) { this.launcher = launcher;
     * this.gameStage = new Stage(); this.gameStage.setTitle("Frisian Draughts - Game");
     * this.movesListGridPane = mainBoard.getMovesListGridPane(); this.mainBoard = mainBoard;
     * this.isWhiteTurn = mainBoard.boardState.isWhiteTurn() ? Bindings.createBooleanBinding(() ->
     * true) : Bindings.createBooleanBinding(() -> false);
     *
     * <p>// Disable board interaction when loading from PDN
     * mainBoard.getBoard().setOnMouseClicked(null);
     *
     * <p>this.gameRoot = new Pane(); Scene scene = new Scene(gameRoot, Launcher.REF_WIDTH,
     * Launcher.REF_HEIGHT); Launcher.registerScene(scene);
     *
     * <p>// Load CSS URL cssUrl = getClass().getResource(Launcher.DARK_MODE ? "/dark-theme.css" :
     * "/light-theme.css"); if (cssUrl != null) {
     * scene.getStylesheets().add(cssUrl.toExternalForm()); } else { System.err.println("Stylesheet
     * not found"); }
     *
     * <p>this.gameStage.setScene(scene);
     *
     * <p>// Initialize game player board = mainBoard.getBoard(this.movesListGridPane,
     * this.gameInfo); if (board == null) { System.err.println("Error: board is null. Please check
     * getBoard() in MainBoard."); return; } if (!gameRoot.getChildren().contains(board)) {
     * gameRoot.getChildren().add(board); }
     *
     * <p>playerUI(gameRoot, scene, true); playerUI(gameRoot, scene, false); chatUI(gameRoot,
     * scene); buttonGameLogic(gameRoot, scene); moveList(gameRoot, scene);
     *
     * <p>// Fade in effect on first load animateFadeIn(gameRoot, 300);
     *
     * <p>// Debounced resizing resizePause = new PauseTransition(Duration.millis(50));
     * resizePause.setOnFinished(_ -> onResize(gameRoot, scene));
     *
     * <p>scene.widthProperty().addListener((_, _, _) -> resizePause.playFromStart());
     * scene.heightProperty().addListener((_, _, _) -> resizePause.playFromStart());
     *
     * <p>// Handle close event this.gameStage.setOnCloseRequest( e -> { e.consume();
     * showExitConfirmation(); }); }
     */
    public void showGameWindow() {
        this.gameStage.show();
    }

    public Pane getGameRoot() {
        return gameRoot;
    }

    /* --------------------------------------------------------------------------------
     *                               BOARD METHODS
     * -------------------------------------------------------------------------------- */

    /**
     * Create the main game board.
     *
     * @param root The root pane
     * @param scene The scene
     * @param isBotActive Whether the bot is active
     * @param filePath The file path
     */
    private void mainGameBoard(Pane root, Scene scene, boolean isBotActive, String filePath) {
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

        mainBoard.loadGameFromPDN(filePath);
    }

    /**
     * Create the main game board for multiplayer games.
     *
     * @param root The root pane
     * @param scene The scene
     */
    private void mainGameBoardMultiplayer(Pane root, Scene scene) {

        try {
            networkClient = new NetworkClient(this);
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

    /**
     * Resize the game board.
     *
     * @param root The root pane
     */
    private void resizeBoard(Pane root) {
        board = mainBoard.resizeBoard(mainBoardSize);
        board.setLayoutX(mainBoardX);
        board.setLayoutY(mainBoardY);
        root.getChildren().add(board);
    }

    /* --------------------------------------------------------------------------------
     *                               PLAYER UI
     * -------------------------------------------------------------------------------- */
    /**
     * Create the player UI.
     *
     * @param root The root pane
     * @param scene The scene
     * @param isPlayerOne Whether the player is Player One
     */
    private void playerUI(Pane root, Scene scene, boolean isPlayerOne) {
        StackPane playerUI = new StackPane();
        playerUI.setPrefSize(scene.getWidth(), playerUIHeight);

        // Positioning based on "Player One" vs. "Player Two"
        // - If isPlayerOne = true, place at the bottom
        // - If isPlayerOne = false, place at the top
        playerUI.setLayoutX(0);
        playerUI.setLayoutY(!isPlayerOne ? 0 : scene.getHeight() - playerUIHeight);

        playerUI.getStyleClass().add("playerUI");
        playerUI.setId(isPlayerOne ? "playerOne" : "playerTwo");

        // This Consumer makes text bold and a bit larger if it's that player's turn
        Consumer<Text> setPlayerStyle =
                textNode -> {
                    if (textNode != null) {
                        boolean isActivePlayer =
                                (isWhiteTurn.get() && isPlayerOne)
                                        || (!isWhiteTurn.get() && !isPlayerOne);

                        textNode.setStyle(
                                "-fx-font-size: "
                                        + (isActivePlayer ? 20 : 15)
                                        + ";"
                                        + "-fx-font-weight: "
                                        + (isActivePlayer ? "bold" : "normal")
                                        + ";");
                    }
                };

        boolean isWhiteSide = isPlayerOne;
        String displayName;

        if (player != null && player.isWhite() == isWhiteSide) {
            // Local player is the correct color for this side
            displayName = player.getName();
        } else if (opponent != null && opponent.isWhite() == isWhiteSide) {
            // Opponent is the correct color for this side
            displayName = opponent.getName();
        } else {
            // Fallback if we don't have a player/opponent for this side yet
            displayName = isPlayerOne ? "Player 1" : "Player 2";
        }

        Text playerText = new Text(displayName);
        playerText.setId(isPlayerOne ? "playerOneText" : "playerTwoText");
        playerText.getStyleClass().add("label");
        setPlayerStyle.accept(playerText); // Apply turn-based style

        // Score text
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

        // Timer text
        Text playerTime = new Text("Time: 10:00");
        playerTime.getStyleClass().add("label");
        playerTime.setId(isPlayerOne ? "playerOneTime" : "playerTwoTime");
        setPlayerStyle.accept(playerTime);

        // Initialize timeline/timer for Player 1 if needed
        if (isPlayerOne && gameTimerPlayerOne == null) {
            gameTimerPlayerOne =
                    new Timeline(
                            new KeyFrame(
                                    Duration.seconds(1),
                                    _ -> {
                                        remainingTimePlayerOne--;
                                        int minutes = remainingTimePlayerOne / 60;
                                        int seconds = remainingTimePlayerOne % 60;
                                        playerTime.setText(
                                                String.format("Time: %02d:%02d", minutes, seconds));

                                        if (remainingTimePlayerOne <= 0) {
                                            gameTimerPlayerOne.stop();
                                            handleTimeUp(true);
                                        }
                                    }));
            gameTimerPlayerOne.setCycleCount(Timeline.INDEFINITE);
            gameTimerPlayerOne.play();
        }

        // Initialize timeline/timer for Player 2 if needed
        if (!isPlayerOne && gameTimerPlayerTwo == null) {
            gameTimerPlayerTwo =
                    new Timeline(
                            new KeyFrame(
                                    Duration.seconds(1),
                                    _ -> {
                                        remainingTimePlayerTwo--;
                                        int minutes = remainingTimePlayerTwo / 60;
                                        int seconds = remainingTimePlayerTwo % 60;
                                        playerTime.setText(
                                                String.format("Time: %02d:%02d", minutes, seconds));

                                        if (remainingTimePlayerTwo <= 0) {
                                            gameTimerPlayerTwo.stop();
                                            handleTimeUp(false);
                                        }
                                    }));
            gameTimerPlayerTwo.setCycleCount(Timeline.INDEFINITE);
            gameTimerPlayerTwo.play();
        }

        // Combine text nodes in an HBox
        HBox playerInfo = new HBox(playerText, playerScore, playerTime);
        playerInfo.getStyleClass().add("playerInfo");
        playerInfo.setSpacing(playerInfoSpacing);
        playerInfo.setAlignment(javafx.geometry.Pos.CENTER);

        playerUI.getChildren().add(playerInfo);
        root.getChildren().add(playerUI);

        boolean isActivePlayer =
                (isWhiteTurn.get() && isPlayerOne) || (!isWhiteTurn.get() && !isPlayerOne);
        if (isActivePlayer) {
            animatePulse(playerUI, 1.05, 500); // short pulse to highlight active turn
        }
    }

    /* --------------------------------------------------------------------------------

    *                            HANDLE TIME UP EVENT
    * -------------------------------------------------------------------------------- */
    /**
     * Handle the time up event for a player. This method is called when a player's timer reaches
     * zero.
     *
     * @param isPlayerOne Whether the player is Player One
     */
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
    /**
     * Create the chat UI.
     *
     * @param root The root pane
     * @param scene The scene
     */
    public void chatUI(Pane root, Scene scene) {
        // Main chat UI container
        StackPane chatUI = new StackPane();
        chatUI.setPrefSize(chatUIWidth, chatUIHeight);
        chatUI.setLayoutX(chatUIX);
        chatUI.setLayoutY(chatUIY);
        chatUI.getStyleClass().add("chatUI");
        chatUI.setId("chatUI");

        // Header for the chat
        HBox chatHeader = new HBox();
        chatHeader.getStyleClass().add("chatHeader");
        chatHeader.setSpacing(10);

        Label chatTitle = new Label("Game Chat");
        chatTitle.getStyleClass().add("chatTitle");

        chatHeader.getChildren().add(chatTitle);

        VBox chatMessages = new VBox();
        chatMessages.setSpacing(8);
        chatMessages.setId("chatMessages");
        chatMessages.getStyleClass().add("chatMessages");

        ScrollPane chatScrollPane = new ScrollPane(chatMessages);
        chatScrollPane.getStyleClass().add("chatScrollPane");
        chatScrollPane.setFitToWidth(true);
        // Hide horizontal bar, keep vertical bar as needed
        chatScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        chatScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        // Chat input field + send button container
        HBox chatInputBox = new HBox(10);
        chatInputBox.getStyleClass().add("chatInputBox");

        TextField chatInput = new TextField();
        chatInput.setPromptText("Type a message...");
        chatInput.getStyleClass().add("chatInput");
        chatInput.setId("chatInput");

        // Send button
        Button sendButton = new Button("Send");
        sendButton.getStyleClass().add("sendButton");

        // Send message on button click
        sendButton.setOnAction(
                _ -> {
                    String message = chatInput.getText().trim();
                    message = Launcher.user.getName() + ": " + message;
                    if (!message.isEmpty()) {
                        appendChatMessage(message);
                        if (isMultiplayer) networkClient.sendMessage("CHAT " + message);
                        chatInput.clear();
                    }
                });

        chatInput.setOnAction(_ -> sendButton.fire());

        chatInputBox.getChildren().addAll(chatInput, sendButton);

        // Main vertical container for all chat elements
        VBox chatContainer = new VBox(10);
        chatContainer.getChildren().addAll(chatHeader, chatScrollPane, chatInputBox);

        chatUI.getChildren().add(chatContainer);

        root.getChildren().add(chatUI);
    }

    /**
     * Append a chat message to the chat UI.
     *
     * @param message The message to append
     */
    public void appendChatMessage(String message) {
        // Find the chat UI by ID
        StackPane chatUI = (StackPane) gameRoot.lookup("#chatUI");
        if (chatUI == null) {
            System.out.println("Error: chatUI not found!");
            return;
        }

        // Check if the chatMessages container exists
        VBox chatMessages = (VBox) chatUI.lookup("#chatMessages");
        if (chatMessages == null) {
            // Create chatMessages container if it doesn't exist
            chatMessages = new VBox();
            chatMessages.setId("chatMessages");
            chatMessages.getStyleClass().add("chatMessages");

            // Wrap chatMessages in a ScrollPane if needed
            ScrollPane chatScrollPane = new ScrollPane(chatMessages);
            chatScrollPane.getStyleClass().add("chatScrollPane");
            chatScrollPane.setFitToWidth(true);
            chatScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            chatScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

            chatUI.getChildren().add(chatScrollPane);
        }

        // Create a message container
        HBox messageBox = new HBox();
        messageBox.getStyleClass().add("messageBox");

        // Different styles for Player 1 and other players

        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        messageLabel.getStyleClass().add("label");
        messageBox.getChildren().add(messageLabel);

        // Add the message to the chatMessages container
        chatMessages.getChildren().add(messageBox);

        // Auto-scroll to the bottom
        Platform.runLater(
                () -> {
                    ScrollPane scrollPane = (ScrollPane) chatUI.lookup(".chatScrollPane");
                    if (scrollPane != null) {
                        scrollPane.setVvalue(1.0);
                    }
                });
    }

    /* --------------------------------------------------------------------------------
     *                            EXIT CONFIRMATION
     * -------------------------------------------------------------------------------- */
    /**
     * Show an exit confirmation dialog. The user can choose to exit with or without saving the
     * game.
     */
    private void showExitConfirmation() {

        ExitChoice choice = ExitGameConfirmation.showSaveConfirmation(!isMultiplayer);
        if (isMultiplayer) {
            networkClient.close();
        }
        switch (choice) {
            case EXIT_WITH_SAVE:
                mainBoard.boardState.setActive(false);
                exporter.exportGameToPDN(
                        mainBoard.boardState.getPawns(),
                        mainBoard.getTakenMoves(),
                        null,
                        isAgainstBot ? "1" : "0",
                        isMultiplayer ? "1" : "0",
                        gameInfo.getPlayerTurn() == 1 ? "W" : "B");

            case EXIT_WITHOUT_SAVE:
                mainBoard.boardState.setActive(false);
                if (Launcher.menuStage == null) {
                    launcher.showMenu();
                }

                fadeOutAndClose(gameStage, 300);
                gameStage.close();
                break;

            case NOT_EXIT:
                break;
        }
    }

    /* --------------------------------------------------------------------------------
     *                           CONTROL BUTTONS
     * -------------------------------------------------------------------------------- */
    /**
     * Propose a draw to the opponent. The opponent can accept or decline the draw offer.
     *
     * @param root The root pane
     * @param scene The scene
     */
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
        Buttons drawButton =
                new Buttons(
                        "Draw",
                        buttonWidth,
                        buttonHeight,
                        () -> {
                            proposeDraw();
                            networkClient.sendMessage("DRAW"); // Send draw message
                        });
        Buttons resignButton =
                new Buttons(
                        "Resign",
                        buttonWidth,
                        buttonHeight,
                        () -> {
                            System.out.println("Resign");
                            networkClient.sendMessage(
                                    "RESIGN"); // Send resign message (only in MP but isnt visible
                            // in SP)
                            Alert alert = new Alert(AlertType.INFORMATION);
                            alert.setTitle("Game Over");
                            alert.setHeaderText("You have resigned!");
                            alert.setContentText("You lose!");
                            alert.showAndWait();
                            Launcher.user.forfeitedGame();

                            networkClient.close();

                            gameStage.close();
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
                        Stream.of(
                                        !isMultiplayer && !isBotvBot && !isAgainstBot
                                                ? undoButton.getButton()
                                                : null,
                                        isMultiplayer ? drawButton.getButton() : null,
                                        isMultiplayer ? resignButton.getButton() : null,
                                        !isMultiplayer ? restartButton.getButton() : null,
                                        settingsButton.getButton(),
                                        exitButton.getButton())
                                .filter(button -> button != null)
                                .collect(Collectors.toList()));
        root.getChildren().addAll(controlButtons);
    }

    /* --------------------------------------------------------------------------------
     *                            MOVES LIST
     * -------------------------------------------------------------------------------- */
    /**
     * Create the moves list UI.
     *
     * @param root The root pane
     * @param scene The scene
     */
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
    /** Propose a draw to the opponent. The opponent can accept or decline the draw offer. */
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
                                _ -> {
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
                                _ -> {
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

    /* --------------------------------------------------------------------------------
     *                            RESIZING LOGIC
     * -------------------------------------------------------------------------------- */
    /**
     * Resize the game UI elements when the window is resized.
     *
     * @param root The root pane
     * @param scene The scene
     */
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

    /**
     * Calculate new dimensions for the game UI elements based on the new scene dimensions.
     *
     * @param scene The scene
     */
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

    /**
     * Convert old dimensions to new dimensions based on a reference dimension.
     *
     * @param oldDimension The old dimension
     * @param newDimension The new dimension
     * @param oldReferenceDimension The old reference dimension
     * @return The new dimension
     */
    private int convertDimensions(int oldDimension, int newDimension, int oldReferenceDimension) {
        return (int)
                ((double) oldDimension * ((double) newDimension / (double) oldReferenceDimension));
    }

    /* --------------------------------------------------------------------------------
     *                           ANIMATION HELPERS
     * -------------------------------------------------------------------------------- */

    /**
     * Smooth fade-in of the given parent node over a specified duration (in ms).
     *
     * @param parent The parent node
     * @param durationMs The duration in milliseconds
     */
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
     *
     * @param node The node to animate
     * @param scaleTo The scale factor to animate to
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
     * Smooth fade-out of the given stage over a specified duration (in ms). The stage will be
     *
     * @param stage The stage to fade out
     * @param durationMs The duration in milliseconds
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

    /**
     * Set the player role (White, Black, or Spectator).
     *
     * @param role The player role
     * @param name The player name
     */
    public void setPlayerRole(String role, String name) {
        switch (role) {
            case "White":
                player = new Player(name, true);
                break;
            case "Black":
                player = new Player(name, false);
                break;
            case "Spectator":
                spectators.add(new Player(name, false, true));
                return;
            default:
                throw new IllegalArgumentException("Invalid role: " + role);
        }
        System.out.println("[DEBUG] Player role set to: " + role);

        if (player.isWhite()) {
            // ((Text) gameRoot.lookup("#playerOneText")).setText(player.getName());
            System.out.println("[DEBUG] Player is white: " + player.getName());
        } else {
            // ((Text) gameRoot.lookup("#playerTwoText")).setText(player.getName());
            System.out.println("[DEBUG] Player is black: " + player.getName());
        }

        // Ensure boardState has the player
        synchronized (mainBoard.boardState.getLock()) {
            mainBoard.boardState.setPlayer(player);
            System.out.println("[DEBUG] boardState player set to: " + player);

            mainBoard.boardState.getLock().notifyAll(); // Notify waiting threads
        }
    }

    /**
     * Set the opponent's name.
     *
     * @param name The opponent's name
     */
    public void setOpponentName(String name) {
        synchronized (mainBoard.boardState.getLock()) {
            opponent = new Player(name, !player.isWhite());
            System.out.println("[DEBUG] Opponent name set to: " + name);
            mainBoard.boardState.setOpponent(opponent);
            System.out.println("[DEBUG] boardState opponent set to: " + opponent);
        }
    }

    /**
     * Set the game state based on the given PDN string.
     *
     * @param pdn The PDN string
     */
    public void showResignDialog() {
        Launcher.user.wonGame();
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Game Over");
        alert.setHeaderText("Opponent has resigned!");
        alert.setContentText("You win!");
        alert.showAndWait();

        networkClient.close();

        gameStage.close();
    }

    /** Show a dialog when the opponent resigns. */
    public void proposeDraw() {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Propose Draw");
        alert.setHeaderText("Are you sure you want to propose a draw?");
        alert.setContentText("Are you sure you want to propose a draw?");
        ButtonType acceptButton = new ButtonType("Accept", ButtonData.OK_DONE);
        ButtonType declineButton = new ButtonType("Decline", ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(acceptButton, declineButton);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == acceptButton) {
            networkClient.sendMessage("DRAW");

            networkClient.close();

            gameStage.close();
        }
        alert.showAndWait();
    }

    /**
     * Show a dialog when the opponent proposes a draw. The user can accept or decline the draw
     * offer.
     */
    public void showDrawDialog() {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Game Over");
        alert.setHeaderText("Opponent has proposed a draw!");
        alert.setContentText("Do you accept?");
        ButtonType acceptButton = new ButtonType("Accept", ButtonData.OK_DONE);
        ButtonType declineButton = new ButtonType("Decline", ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(acceptButton, declineButton);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == acceptButton) {
            Launcher.user.drewGame();
            networkClient.sendMessage("DRAW ACCEPTED");

            networkClient.close();

            gameStage.close();
        } else {
            networkClient.sendMessage("DRAW DECLINED");
        }
        alert.showAndWait();
    }

    /** Show a dialog when the opponent accepts the draw offer. The game ends in a draw. */
    public void showDrawAcceptedDialog() {
        Launcher.user.drewGame();
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Game Over");
        alert.setHeaderText("Draw accepted!");
        alert.setContentText("Game over - draw!");
        alert.showAndWait();

        networkClient.close();

        gameStage.close();
    }

    /** Show a dialog when the opponent declines the draw offer. The game continues. */
    public void showDrawDeclinedDialog() {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Draw Declined");
        alert.setHeaderText("Opponent has declined the draw!");
        alert.setContentText("Game continues!");
        alert.showAndWait();
    }
}
