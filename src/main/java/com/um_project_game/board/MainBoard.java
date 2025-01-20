package com.um_project_game.board;

import com.um_project_game.AI.DQNModel;
import com.um_project_game.Server.NetworkClient;
import com.um_project_game.util.PDNParser;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

import org.joml.Vector2i;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

/** Represents the main board of the game. */
public class MainBoard {

    public BoardState boardState = new BoardState();

    public BoardRendered boardRendered = new BoardRendered(boardState);
    public MoveManager moveManager = new MoveManager(boardState, this);
    public BotManager botManager = new BotManager(boardState, this);

    public MainBoard() {
        boardRendered.setMoveManager(moveManager);
        moveManager.setBotManager(botManager);
        moveManager.setBoardRendered(boardRendered);
        botManager.setBoardRendered(boardRendered);
        botManager.setMoveManager(moveManager);
    }

    public void setMovesListManager(MovesListManager movesListManager) {
        boardState.setMovesListManager(movesListManager);
    }

    public GridPane getBoard(GridPane movesListGridPane, GameInfo gameInfo) {
        boardState.setMovesListManager(new MovesListManager(movesListGridPane));
        boardState.setGameInfo(gameInfo);
        boardState.setActive(true);
        boardRendered.renderPawns();
        return boardState.getBoard();
    }

    public GridPane getBoard() {

        return boardState.getBoard();
    }

    /**
     * Creates and returns the main board, rendering it to the root pane.
     *
     * @param root Root pane of the window.
     * @param boardPixelSize Size of the board in pixels.
     * @param boardPosition Position where the board should be placed (x, y).
     * @return GridPane representing the board.
     */
    public GridPane getMainBoard(
            Pane root,
            float boardPixelSize,
            Vector2i boardPosition,
            GameInfo gameInfo,
            GridPane movesListGridPane,
            boolean isBotActive,
            boolean isBotvsBot) {
        boardState.setTileSize(boardPixelSize / BoardState.getMainBoardSize());
        boardState.setPawns(new ArrayList<>());
        boardState.setBoard(new GridPane());
        boardState.setWhiteTurn(true);
        boardState.setBotActive(isBotActive);
        boardState.setBotvsBot(isBotvsBot);
        boardState.setRoot(root);
        boardState.setGameInfo(gameInfo);

        System.out.println("isBotvsBot: " + isBotvsBot);
        gameInfo.playerTurn.set(1);
        boardState.setMovesListManager(new MovesListManager(movesListGridPane));

        boardState.getBoard().setLayoutX(boardPosition.x);
        boardState.getBoard().setLayoutY(boardPosition.y);

        boardRendered.setupBoard();
        boardRendered.renderBoard();
        boardRendered.renderPawns();

        highlightMovablePawns();

        Path savePath =
                Paths.get(
                        System.getProperty("user.home"),
                        ".frisian-draught",
                        "FrisianDraughtsExports");

        // Ensure the directory exists
        if (!Files.exists(savePath)) {
            try {
                Files.createDirectories(savePath);
            } catch (IOException e) {
                System.err.println("Failed to create directory: " + savePath);
                e.printStackTrace();
            }
        }
        if (isBotActive || isBotvsBot) {
            boardState.setBotModel(new DQNModel(101, 100, 100, 0.1));
            botManager.loadLatestModel(savePath);
        }

        if (isBotvsBot) {
            botManager.playBotVsBot(savePath.toString());
        }

        return boardState.getBoard();
    }

    public GridPane getMainBoardMultiplayer(
            Pane root,
            float boardPixelSize,
            Vector2i boardPosition,
            GameInfo gameInfo,
            GridPane movesListGridPane,
            boolean isMultiplayer,
            NetworkClient networkClient) {
        boardState.setMultiplayer(isMultiplayer);
        boardState.setNetworkClient(networkClient);

        return getMainBoard(
                root,
                boardPixelSize,
                boardPosition,
                gameInfo,
                movesListGridPane,
                boardState.isBotActive(),
                false);
    }

    /**
     * Resizes the board and its components.
     *
     * @param boardPixelSize New size of the board in pixels.
     * @return GridPane representing the resized board.
     */
    public GridPane resizeBoard(float boardPixelSize) {
        boardState.setTileSize(boardPixelSize / BoardState.getMainBoardSize());

        // Update the size of each tile
        for (int y = 0; y < boardState.getBoardSize().y; y++) {
            for (int x = 0; x < boardState.getBoardSize().x; x++) {
                Node node = boardState.getBoardTiles()[x][y];
                if (node instanceof Rectangle) {
                    Rectangle square = (Rectangle) node;
                    square.setWidth(boardState.getTileSize());
                    square.setHeight(boardState.getTileSize());
                }
            }
        }

        // Update the size and hover effect of each pawn
        double scaleFactor = 0.8;
        for (Map.Entry<Pawn, ImageView> entry : boardState.getPawnViews().entrySet()) {
            ImageView pawnView = entry.getValue();
            pawnView.setFitWidth(boardState.getTileSize() * scaleFactor);
            pawnView.setFitHeight(boardState.getTileSize() * scaleFactor);
        }

        for (Node highlightNode : boardState.getHighlightNodes()) {
            if (highlightNode instanceof Rectangle) {
                Rectangle square = (Rectangle) highlightNode;
                square.setWidth(boardState.getTileSize());
                square.setHeight(boardState.getTileSize());
            }
        }

        return boardState.getBoard();
    }

    /**
     * Resets the game to its initial state.
     *
     * @param boardPixelSize Size of the board in pixels.
     */
    public void resetGame(float boardPixelSize) {
        boardState.setTileSize(boardPixelSize / BoardState.getMainBoardSize());
        boardState.setWhiteTurn(true);
        boardState.setActive(true); // Ensure game is active
        boardState.getPastStates().clear(); // Clear game history
        boardState.getGameInfo().scorePlayerOne.set(0);
        boardState.getGameInfo().scorePlayerTwo.set(0);
        boardState.getGameInfo().playerTurn.set(1);
        resetTakenMoves();
        updateMovesListUI();

        // Reset board tiles
        for (int y = 0; y < boardState.getBoardSize().y; y++) {
            for (int x = 0; x < boardState.getBoardSize().x; x++) {
                Node node = boardState.getBoardTiles()[x][y];
                if (node instanceof Rectangle) {
                    Rectangle square = (Rectangle) node;
                    square.setWidth(boardState.getTileSize());
                    square.setHeight(boardState.getTileSize());
                }
            }
        }

        // Clear highlights and reset pawns
        boardRendered.clearHighlights();
        moveManager.resetPawnsToInitialPositions();

        // Resize pawns
        double scaleFactor = 0.8;
        for (Map.Entry<Pawn, ImageView> entry : boardState.getPawnViews().entrySet()) {
            Pawn pawn = entry.getKey();
            ImageView pawnView = entry.getValue();

            pawnView.setFitWidth(boardState.getTileSize() * scaleFactor);
            pawnView.setFitHeight(boardState.getTileSize() * scaleFactor);
            pawn.setKing(false); // Reset pawn to non-king
            Vector2i initialPosition = pawn.getInitialPosition();
            pawn.setPosition(initialPosition);
            GridPane.setColumnIndex(pawnView, initialPosition.x);
            GridPane.setRowIndex(pawnView, initialPosition.y);
        }
    }

    public void highlightMovablePawns() {
        GameState currentState =
                new GameState(boardState.getPawnPositionMap(), boardState.isWhiteTurn, this);
        Map<Pawn, List<Move>> validMovesMap = moveManager.getValidMovesForState(currentState);
        List<Pawn> maxCaptures = new ArrayList<>(validMovesMap.keySet());

        boardRendered.highlightMovablePawns(maxCaptures);
    }

    /**
     * Creates and returns a random board, suitable for non-unique layouts.
     *
     * @param root Root pane of the window.
     * @param boardPixelSize Size of the board in pixels.
     * @return GridPane representing a random board.
     */
    public GridPane getRandomBoard(Pane root, float boardPixelSize, String filePath) {
        boardState.setTileSize(boardPixelSize / BoardState.getMainBoardSize());
        boardState.setPawns(new ArrayList<>());
        boardState.setBoard(new GridPane());

        boardState.setRoot(root);

        boardState.setActive(false);

        boardRendered.setupBoard();
        boardRendered.renderBoard();

        if (filePath != null) {
            loadGameFromPDN(filePath);
        }

        boardRendered.renderPawns();
        boardState.getBoard().getStyleClass().add("board");

        highlightMovablePawns();

        return boardState.getBoard();
    }

    public GridPane getRandomBoard(Pane root, float boardPixelSize) {
        return getRandomBoard(root, boardPixelSize, null);
    }

    /**
     * Renders the board grid.
     *
     * @param tileSize Size of each tile.
     * @param board GridPane to render the board onto.
     */
    public GridPane getMovesListGridPane() {
        return boardState
                .getMovesListManager()
                .getMovesListGridPane(); // Reference to the VBox for moves
    }

    public void updateMovesListUI() {
        if (boardState.getMovesListManager() != null) {
            boardState.getMovesListManager().updateMovesListUI(boardState.getTakenMoves());
        }
    }

    /** Checks if it is the white player's turn. */
    public boolean isWhiteTurn() {
        return boardState.isWhiteTurn();
    }

    /**
     * Animates the movement of a pawn to a new position.
     *
     * @param pawn The pawn to move.
     * @param landingPos The landing position of the pawn.
     * @param onFinished Callback to run after the animation finishes.
     */
    public void animatePawnMovement(Pawn pawn, Vector2i landingPos, Runnable onFinished) {
        if (boardState.isAnimating()) return; // Prevent new animations during an ongoing one
        boardState.setAnimating(true);
        ImageView pawnView = boardState.getPawnViews().get(pawn);

        // Bring pawnView to front by moving it to the end of the children list
        boardState.getBoard().getChildren().remove(pawnView);
        boardState.getBoard().getChildren().add(pawnView);

        // Calculate translation distances
        double deltaX = (landingPos.x - pawn.getPosition().x) * boardState.getTileSize();
        double deltaY = (landingPos.y - pawn.getPosition().y) * boardState.getTileSize();

        // Create TranslateTransition
        TranslateTransition transition = new TranslateTransition(Duration.millis(300), pawnView);
        transition.setByX(deltaX);
        transition.setByY(deltaY);
        transition.setInterpolator(Interpolator.EASE_BOTH);

        // When the animation finishes, reset the translate values and update the GridPane position
        transition.setOnFinished(
                _ -> {
                    if (boardState.isMultiplayer()) {
                        boardState.getNetworkClient().sendMove(pawn.getPosition(), landingPos);
                    }

                    // Reset translation
                    pawnView.setTranslateX(0);
                    pawnView.setTranslateY(0);

                    // Update the pawn's position in the GridPane
                    GridPane.setColumnIndex(pawnView, landingPos.x);
                    GridPane.setRowIndex(pawnView, landingPos.y);
                    pawn.setPosition(landingPos);

                    boardState.setAnimating(false);

                    // Call the onFinished callback
                    if (onFinished != null) {
                        onFinished.run();
                    }
                    updateMovesListUI();
                });

        // Play the animation
        transition.play();
    }

    /**
     * Animates the movement of a pawn along a capture path.
     *
     * @param pawn The pawn to move.
     * @param path The capture path to follow.
     * @param onFinished Callback to run after the animation finishes.
     */
    public void animatePawnCaptureMovement(Pawn pawn, CapturePath path, Runnable onFinished) {
        System.out.println("Animating capture moves");
        ImageView pawnView = boardState.getPawnViews().get(pawn);
        List<Vector2i> positions = path.positions;
        List<Pawn> capturedPawns = path.capturedPawns;

        System.out.println("Animating moves");

        List<Animation> animations = new ArrayList<>();

        Vector2i currentPos = pawn.getPosition();
        for (int i = 0; i < positions.size(); i++) {
            Vector2i nextPos = positions.get(i);

            double deltaX = (nextPos.x - currentPos.x) * boardState.getTileSize();
            double deltaY = (nextPos.y - currentPos.y) * boardState.getTileSize();

            TranslateTransition transition =
                    new TranslateTransition(Duration.millis(300), pawnView);
            transition.setByX(deltaX);
            transition.setByY(deltaY);
            transition.setInterpolator(Interpolator.EASE_BOTH);

            // Create a final index variable for use in the lambda
            int index = i;

            transition.setOnFinished(
                    _ -> {
                        if (boardState.isMultiplayer()) {
                            List<Vector2i> capturedPositions = new ArrayList<>();
                            for (Pawn capturedPawn : capturedPawns) {
                                capturedPositions.add(capturedPawn.getPosition());
                            }
                            boardState
                                    .getNetworkClient()
                                    .sendMove(pawn.getPosition(), nextPos, capturedPositions);
                        }

                        // Reset translation
                        pawnView.setTranslateX(0);
                        pawnView.setTranslateY(0);

                        // Process each step to handle captures and update positions
                        moveManager.processCaptureStep(pawn, nextPos, capturedPawns, index);
                        updateMovesListUI();
                    });

            animations.add(transition);

            currentPos = nextPos;
        }

        // Create a SequentialTransition
        SequentialTransition sequentialTransition = new SequentialTransition();
        sequentialTransition.getChildren().addAll(animations);

        sequentialTransition.setOnFinished(
                _ -> {
                    boardState.setAnimating(false);
                    onFinished.run();
                });
        boardState.setAnimating(true);
        // Add whole CapturePath to takenMoves list
        List<Vector2i> capturedPositions = new ArrayList<>();
        path.capturedPawns.forEach(
                capturedPawn -> {
                    capturedPositions.add(capturedPawn.getPosition());
                });

        // Bring pawnView to front
        boardState.getBoard().getChildren().remove(pawnView);
        boardState.getBoard().getChildren().add(pawnView);

        sequentialTransition.play();
    }

    public void loadGameFromPDN(String pdnFilePath) {
        try {
            System.out.println("Loading game from: " + pdnFilePath);
            PDNParser pdnParser = new PDNParser(pdnFilePath);
            pdnParser.parseFile();

            List<Pawn> pawns = pdnParser.getPawns();
            if (pawns.isEmpty()) {
                for (Move move : pdnParser.getMoves()) {
                    Pawn pawn = moveManager.getPawnAtPosition(move.getStartPosition());
                    if (pawn == null) {
                        System.out.println(
                                "No pawn at pos: "
                                        + move.getStartPosition().x
                                        + ", "
                                        + move.getStartPosition().y);
                        throw new RuntimeException(
                                "Couldn't find pawn to process the move: " + move);
                    }
                    List<CapturePath> possibleCapturePaths = findCapturePathsForPawn(pawn);
                    boolean captureExecuted = false;
                    for (CapturePath ct : possibleCapturePaths) {
                        if (ct.getLastPosition().equals(move.getEndPosition())) {
                            moveManager.executeCaptureMove(pawn, ct, false);
                            captureExecuted = true;
                            break; // Capture executed, no need to check further paths for this move
                        }
                    }

                    if (!captureExecuted) {
                        // If no capture was executed, proceed with a normal move
                        moveManager.executeMove(pawn, move.getEndPosition());
                    }
                }
            } else {
                boardState.setPawns(pawns);
            }

            boardState.setBotActive(pdnParser.getIsBot().equals("1"));
            boardState.setMultiplayer(pdnParser.getIsMultiplayer().equals("1"));
            boardState.setWhiteTurn(pdnParser.getTurn().equals("W"));

        } catch (Exception e) {
            System.err.println("Error loading game from PDN: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private List<CapturePath> findCapturePathsForPawn(Pawn pawn) {
        List<CapturePath> capturePaths = new ArrayList<>();

        // Define a boundary check for positions within board limits
        BiPredicate<Integer, Integer> inBounds =
                (x, y) ->
                        x >= 0
                                && x < boardState.getBoardSize().x
                                && y >= 0
                                && y < boardState.getBoardSize().y;

        // Start capture path search from the pawn's current position
        moveManager.captureCheck(
                pawn,
                inBounds,
                pawn.getPosition().x,
                pawn.getPosition().y,
                new CapturePath(),
                capturePaths);

        return capturePaths;
    }

    private void resetTakenMoves() {
        boardState.getTakenMoves().clear();
        boardState.getPastStates().clear();
        boardState.getRequiredPawns().clear();
        updateMovesListUI();
    }

    public List<Move> getTakenMoves() {
        return boardState.getTakenMoves();
    }

    public GameState getBoardState() {
        Map<Vector2i, Pawn> currentState = new HashMap<>();

        // Iterate over all pawns and add their positions to the state
        for (Pawn pawn : boardState.getPawns()) {
            currentState.put(pawn.getPosition(), pawn);
        }

        return new GameState(currentState, boardState.isWhiteTurn(), this);
    }

    private GameState createGameState() {
        return new GameState(
                boardState.getPawns().stream()
                        .collect(Collectors.toMap(Pawn::getPosition, pawn -> pawn)),
                boardState.isWhiteTurn(),
                this);
    }

    public List<Move> getValidMovesForState(GameState state) {
        List<Move> validMoves = new ArrayList<>();
        // Iterate through all pawns in the game state
        for (Map.Entry<Vector2i, Pawn> entry : state.getBoardState().entrySet()) {
            Vector2i position = entry.getKey();
            Pawn pawn = entry.getValue();
            // Ensure this pawn belongs to the current player
            if (pawn.isWhite() != state.isWhiteTurn()) {
                continue;
            }
            // Generate moves for this pawn
            moveManager.seePossibleMove(pawn, false);
            validMoves.addAll(
                    boardState.getPossibleMoves().stream()
                            .map(
                                    move ->
                                            new Move(
                                                    position,
                                                    move,
                                                    new ArrayList<>(
                                                            boardState.getRequiredPawns().stream()
                                                                    .map(Pawn::getPosition)
                                                                    .collect(Collectors.toList()))))
                            .collect(Collectors.toList()));
        }
        List<Move> captureMoves =
                validMoves.stream()
                        .filter(move -> !move.getCapturedPositions().isEmpty())
                        .collect(Collectors.toList());
        if (!captureMoves.isEmpty()) {
            System.out.println("Capture moves found: " + captureMoves);
            return captureMoves;
        }
        System.out.println("Normal moves found: " + validMoves);
        return validMoves;
    }

    private Stage primaryStage;

    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }
}
