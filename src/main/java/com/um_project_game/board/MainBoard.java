package com.um_project_game.board;

import com.um_project_game.AI.DQNModel;
import com.um_project_game.AI.Experience;
import com.um_project_game.AI.ReplayBuffer;
import com.um_project_game.Launcher;
import com.um_project_game.util.PDNParser;
import com.um_project_game.util.SoundPlayer;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;

import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

/** Represents the main board of the game. */
public class MainBoard {

    // Constants
    private static final int BOARD_SIZE = 10;

    // Game state variables
    private boolean isWhiteTurn = true; // White starts first
    private boolean isActive = true;
    private boolean boardInitialized = false;
    private boolean isAnimating = false;
    private boolean isMultiplayer = false;

    // Board-related fields
    private Vector2i boardSize = new Vector2i(BOARD_SIZE, BOARD_SIZE);
    private float tileSize;
    private GridPane board;
    private Node[][] boardTiles = new Node[BOARD_SIZE][BOARD_SIZE];
    private Pane root;

    // Pawn and move management
    private Pawn focusedPawn;
    private List<Pawn> allPawns = new ArrayList<>();
    private List<Pawn> pawns = new ArrayList<>();
    private List<Pawn> requiredPawns = new ArrayList<>();
    private List<Vector2i> possibleMoves = new ArrayList<>();

    private List<Move> takenMoves = new ArrayList<>();
    private MovesListManager movesListManager;
    private List<GameState> pastStates = new ArrayList<>();
    private Map<Pawn, ImageView> pawnViews = new HashMap<>();
    private List<Node> highlightNodes = new ArrayList<>();

    // Sound and game info
    private SoundPlayer soundPlayer = Launcher.soundPlayer;

    private GameInfo gameInfo;
    private DQNModel botModel;
    private boolean isBotActive = false;
    private ReplayBuffer replayBuffer = new ReplayBuffer(1000);
    private static final int BATCH_SIZE = 32;
    private static final double GAMMA = 0.99;

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
            boolean isBotActive) {
        tileSize = boardPixelSize / BOARD_SIZE;
        pawns = new ArrayList<>();
        board = new GridPane();
        isWhiteTurn = true;
        this.isBotActive = isBotActive;
        this.root = root;
        this.gameInfo = gameInfo;
        gameInfo.playerTurn.set(1);
        movesListManager = new MovesListManager(movesListGridPane);

        board.setLayoutX(boardPosition.x);
        board.setLayoutY(boardPosition.y);

        setupBoard();
        renderBoard();
        renderPawns();

        botModel = new DQNModel(101, 100, 100, 0.1);

        System.out.println("isMultiplayer: " + isMultiplayer);

        return board;
    }

    public GridPane getMainBoardMultiplayer(
            Pane root,
            float boardPixelSize,
            Vector2i boardPosition,
            GameInfo gameInfo,
            GridPane movesListGridPane,
            boolean isMultiplayer) {
        this.isMultiplayer = isMultiplayer;
        return getMainBoard(
                root, boardPixelSize, boardPosition, gameInfo, movesListGridPane, isBotActive);
    }

    /**
     * Resizes the board and its components.
     *
     * @param boardPixelSize New size of the board in pixels.
     * @return GridPane representing the resized board.
     */
    public GridPane resizeBoard(float boardPixelSize) {
        tileSize = boardPixelSize / BOARD_SIZE;

        // Update the size of each tile
        for (int y = 0; y < boardSize.y; y++) {
            for (int x = 0; x < boardSize.x; x++) {
                Node node = boardTiles[x][y];
                if (node instanceof Rectangle) {
                    Rectangle square = (Rectangle) node;
                    square.setWidth(tileSize);
                    square.setHeight(tileSize);
                }
            }
        }

        // Update the size and hover effect of each pawn
        double scaleFactor = 0.8;
        for (Map.Entry<Pawn, ImageView> entry : pawnViews.entrySet()) {
            ImageView pawnView = entry.getValue();
            pawnView.setFitWidth(tileSize * scaleFactor);
            pawnView.setFitHeight(tileSize * scaleFactor);
        }

        // Update the size of highlight nodes if necessary
        for (Node highlightNode : highlightNodes) {
            if (highlightNode instanceof Rectangle) {
                Rectangle square = (Rectangle) highlightNode;
                square.setWidth(tileSize);
                square.setHeight(tileSize);
            }
        }

        return board;
    }

    /**
     * Resets the game to its initial state.
     *
     * @param boardPixelSize Size of the board in pixels.
     */
    public void resetGame(float boardPixelSize) {
        tileSize = boardPixelSize / BOARD_SIZE;
        isWhiteTurn = true;
        gameInfo.scorePlayerOne.set(0);
        gameInfo.scorePlayerTwo.set(0);
        gameInfo.playerTurn.set(1);
        // Clear moves list
        resetTakenMoves();
        updateMovesListUI();

        // Reset the size of the tiles
        for (int y = 0; y < boardSize.y; y++) {
            for (int x = 0; x < boardSize.x; x++) {
                Node node = boardTiles[x][y];
                if (node instanceof Rectangle) {
                    Rectangle square = (Rectangle) node;
                    square.setWidth(tileSize);
                    square.setHeight(tileSize);
                }
            }
        }

        // Clear highlights if any
        clearHighlights();

        // Reset pawns to initial positions
        resetPawnsToInitialPositions();

        // Update the size and position of each pawn
        double scaleFactor = 0.8;
        for (Map.Entry<Pawn, ImageView> entry : pawnViews.entrySet()) {
            Pawn pawn = entry.getKey();
            ImageView pawnView = entry.getValue();

            // Update pawn image size
            pawnView.setFitWidth(tileSize * scaleFactor);
            pawnView.setFitHeight(tileSize * scaleFactor);

            // Reset pawn properties
            pawn.setKing(false);

            // Set pawn to initial position
            Vector2i initialPosition = pawn.getInitialPosition();
            pawn.setPosition(initialPosition);
            GridPane.setColumnIndex(pawnView, initialPosition.x);
            GridPane.setRowIndex(pawnView, initialPosition.y);
        }
    }

    /**
     * Creates and returns a random board, suitable for non-unique layouts.
     *
     * @param root Root pane of the window.
     * @param boardPixelSize Size of the board in pixels.
     * @return GridPane representing a random board.
     */
    public GridPane getRandomBoard(Pane root, float boardPixelSize, String filePath) {
        tileSize = boardPixelSize / BOARD_SIZE;
        pawns = new ArrayList<>();
        board = new GridPane();

        isActive = false;

        setupBoard();
        renderBoard();
        renderPawns();
        if (filePath != null) {
            loadGameFromPDN(filePath);
        }
        board.getStyleClass().add("board");

        return board;
    }

    public GridPane getRandomBoard(Pane root, float boardPixelSize) {
        return getRandomBoard(root, boardPixelSize, null);
    }

    /**
     * Sets up the initial positions of the pawns on the board.
     *
     * @param pawns List to populate with the initial pawns.
     */
    private void setupBoard() {
        // Function to add pawns to the board
        BiConsumer<Integer, Boolean> addPawns =
                (startRow, isWhite) -> {
                    for (int y = startRow; y < startRow + 4; y++) {
                        for (int x = 0; x < boardSize.x; x++) {
                            if ((x + y) % 2 == 1) {
                                pawns.add(new Pawn(new Vector2i(x, y), isWhite));
                            }
                        }
                    }
                };

        // Add white pawns
        addPawns.accept(0, false);
        // Add black pawns
        addPawns.accept(6, true);

        allPawns.clear();
        allPawns.addAll(pawns);
    }

    /**
     * Renders the board grid.
     *
     * @param tileSize Size of each tile.
     * @param board GridPane to render the board onto.
     */
    public GridPane getMovesListGridPane() {
        return movesListManager.getMovesListGridPane(); // Reference to the VBox for moves
    }

    private void updateMovesListUI() {
        if (movesListManager != null) {
            movesListManager.updateMovesListUI(takenMoves);
        }
    }

    private void renderBoard() {
        if (boardInitialized) return; // Avoid re-initializing the board

        for (int y = 0; y < boardSize.y; y++) {
            for (int x = 0; x < boardSize.x; x++) {
                Rectangle square = new Rectangle(tileSize, tileSize);
                square.setFill((x + y) % 2 == 0 ? Color.WHITE : Color.BLACK);
                board.add(square, x, y);
                boardTiles[x][y] = square; // Keep a reference to each tile
            }
        }
        boardInitialized = true;
    }

    /**
     * Renders the pawns onto the board.
     *
     * @param board GridPane representing the board.
     * @param pawns List of pawns to render.
     * @param tileSize Size of each tile.
     */
    private void renderPawns() {
        // For new pawns, create ImageViews and add them to the board
        for (Pawn pawn : pawns) {
            if (!pawnViews.containsKey(pawn)) {
                ImageView pawnView = createPawnImageView(pawn, 0.8);
                setupPawnInteractions(pawnView, pawn);
                board.add(pawnView, pawn.getPosition().x, pawn.getPosition().y);
                GridPane.setHalignment(pawnView, HPos.CENTER);
                GridPane.setValignment(pawnView, VPos.CENTER);
                pawnViews.put(pawn, pawnView);
            }
        }
    }

    /**
     * Creates an ImageView for a pawn.
     *
     * @param pawn The pawn for which to create the ImageView.
     * @param tileSize Size of each tile.
     * @param scaleFactor Scaling factor for the pawn image.
     * @return ImageView representing the pawn.
     */
    private ImageView createPawnImageView(Pawn pawn, double scaleFactor) {
        ImageView pawnView = new ImageView(pawn.getImage());

        pawnView.setFitWidth(tileSize * scaleFactor);
        pawnView.setFitHeight(tileSize * scaleFactor);
        pawnView.setPreserveRatio(true);
        pawnView.setUserData(pawn);

        // Add the hover listener here
        pawnView.hoverProperty()
                .addListener(
                        (observable, oldValue, newValue) -> {
                            double endScale = newValue ? 1.1 : 1.0;

                            ScaleTransition scaleTransition =
                                    new ScaleTransition(Duration.millis(200), pawnView);
                            scaleTransition.setToX(endScale);
                            scaleTransition.setToY(endScale);
                            scaleTransition.setInterpolator(Interpolator.EASE_BOTH);
                            scaleTransition.play();
                        });

        return pawnView;
    }

    /**
     * Sets up the interactions for a pawn ImageView.
     *
     * @param pawnView The ImageView of the pawn.
     * @param pawn The pawn object.
     * @param board GridPane representing the board.
     * @param pawns List of all pawns.
     * @param tileSize Size of each tile.
     */
    private void setupPawnInteractions(ImageView pawnView, Pawn pawn) {
        pawnView.hoverProperty()
                .addListener(
                        (observable, oldValue, newValue) -> {
                            double scaleFactor = newValue ? 0.96 : 0.8; // Increase size on hover
                            pawnView.setFitWidth(tileSize * scaleFactor);
                            pawnView.setFitHeight(tileSize * scaleFactor);
                        });

        if (!isActive) {
            return;
        }

        pawnView.setOnMouseClicked(
                event -> {
                    clearHighlights();
                    focusedPawn = pawn;
                    seePossibleMove(focusedPawn);
                    renderPawns();
                });
    }

    /**
     * Retrieves a pawn at a given position.
     *
     * @param pawns List of all pawns.
     * @param position Position to check.
     * @return Pawn at the position, or null if none.
     */
    private Pawn getPawnAtPosition(Vector2i position) {
        return pawns.stream()
                .filter(p -> p.getPosition().equals(position))
                .findFirst()
                .orElse(null);
    }

    /**
     * Finds pawns with the maximum number of captures.
     *
     * @param pawns List of all pawns.
     * @return List of pawns with the maximum number of captures.
     */
    private List<Pawn> findPawnsWithMaxCaptures() {
        Map<Pawn, List<CapturePath>> pawnCapturePaths = new HashMap<>();

        for (Pawn pawn : pawns) { // Ensure only active pawns are iterated
            // Only consider pawns belonging to the current player
            if (pawn.isWhite() != isWhiteTurn) continue;

            List<CapturePath> paths = new ArrayList<>();
            captureCheck(
                    pawn,
                    (x, y) -> x >= 0 && x < boardSize.x && y >= 0 && y < boardSize.y,
                    pawn.getPosition().x,
                    pawn.getPosition().y,
                    new CapturePath(),
                    paths);

            if (!paths.isEmpty()) {
                pawnCapturePaths.put(pawn, paths);
            }
        }

        if (pawnCapturePaths.isEmpty()) {
            requiredPawns.clear();
            return Collections.emptyList();
        }

        double maxCaptureValue =
                pawnCapturePaths.values().stream()
                        .flatMap(List::stream)
                        .mapToDouble(CapturePath::getCaptureValue)
                        .max()
                        .orElse(0);

        // Identify pawns with capture paths equal to maxCaptureValue
        List<Pawn> required =
                pawnCapturePaths.entrySet().stream()
                        .filter(
                                entry ->
                                        entry.getValue().stream()
                                                .anyMatch(
                                                        path ->
                                                                path.getCaptureValue()
                                                                        == maxCaptureValue))
                        .map(Map.Entry::getKey)
                        .collect(Collectors.toList());

        requiredPawns = required.stream().filter(pawns::contains).collect(Collectors.toList());

        return requiredPawns;
    }

    /**
     * Highlights possible moves for the selected pawn.
     *
     * @param board GridPane representing the board.
     * @param pawns List of all pawns.
     * @param pawn The selected pawn.
     * @param tileSize Size of each tile.
     */
    private void seePossibleMove(Pawn pawn) {
        possibleMoves.clear();
        clearHighlights();
        Vector2i position = pawn.getPosition();
        int x = position.x;
        int y = position.y;

        BiPredicate<Integer, Integer> inBounds =
                (newX, newY) -> newX >= 0 && newX < boardSize.x && newY >= 0 && newY < boardSize.y;

        List<CapturePath> allPaths = new ArrayList<>();

        captureCheck(pawn, inBounds, x, y, new CapturePath(), allPaths);

        if (!allPaths.isEmpty()) {
            handleCaptureMoves(pawn, allPaths, false, true);
        } else if (requiredPawns.isEmpty()) {
            handleNormalMoves(pawn, inBounds, x, y, false, true);
        }
    }

    /**
     * Handles capture moves by highlighting them and setting up interactions.
     *
     * @param board GridPane representing the board.
     * @param pawns List of all pawns.
     * @param pawn The selected pawn.
     * @param tileSize Size of each tile.
     * @param allPaths List of all possible capture paths.
     */
    private void handleCaptureMoves(
            Pawn pawn, List<CapturePath> allPaths, boolean isAutomatic, boolean isAnimated) {
        double maxCaptureValue =
                allPaths.stream().mapToDouble(CapturePath::getCaptureValue).max().orElse(0);

        // Filter paths that have the maximum capture value
        List<CapturePath> maxCapturePaths =
                allPaths.stream()
                        .filter(path -> path.getCaptureValue() == maxCaptureValue)
                        .collect(Collectors.toList());

        if (isAutomatic) {
            // Execute the first maximum capture path automatically
            if (!maxCapturePaths.isEmpty()) {
                executeCaptureMove(pawn, maxCapturePaths.get(0), isAnimated);
            }
            return; // Exit after executing automatic move
        }

        // If not automatic, set up click listeners for manual move handling
        for (CapturePath path : maxCapturePaths) {
            Vector2i landingPos = path.getLastPosition();

            Rectangle square = createHighlightSquare(Color.RED);
            board.add(square, landingPos.x, landingPos.y);
            possibleMoves.add(landingPos);

            // Set up manual click event
            square.setOnMouseClicked(
                    event -> {
                        if (isWhiteTurn != pawn.isWhite() || isAnimating) {
                            System.out.println("Not your turn or animation in progress!");
                            return;
                        }
                        soundPlayer.playCaptureSound();

                        // Clear highlights and reset move count
                        clearHighlights();
                        pawn.resetNumberOfNonCapturingMoves();

                        // Execute capture move
                        executeCaptureMove(pawn, path, isAnimated);
                    });
        }
    }

    private void executeCaptureMove(Pawn pawn, CapturePath path, boolean isAnimated) {
        // Writing here what happens. If animated, then
        if (isAnimated) {
            animatePawnCaptureMovement(pawn, path, () -> processAfterCaptureMove(pawn, path));
            addMoveToHistory(pawn, path);
        } else {
            // Directly process the capture steps without animation
            addMoveToHistory(pawn, path);
            processCaptureSteps(pawn, path);
            processAfterCaptureMove(pawn, path); // Finalize the move
        }
    }

    private void processCaptureSteps(Pawn pawn, CapturePath path) {
        List<Vector2i> positions = path.positions;
        List<Pawn> capturedPawns = path.capturedPawns;

        for (int i = 0; i < positions.size(); i++) {
            Vector2i nextPos = positions.get(i);

            // Update the pawn position and capture any pawns in the path
            processCaptureStep(pawn, nextPos, capturedPawns, i);
        }
    }

    private void processCaptureStep(
            Pawn pawn, Vector2i nextPos, List<Pawn> capturedPawns, int stepIndex) {
        ImageView pawnView = pawnViews.get(pawn);

        // Update the pawn's position in the GridPane
        GridPane.setColumnIndex(pawnView, nextPos.x);
        GridPane.setRowIndex(pawnView, nextPos.y);

        // Update the pawn's position in the data model
        pawn.setPosition(nextPos);

        // Capture any pawn at this step if one exists
        if (stepIndex < capturedPawns.size()) {
            Pawn capturedPawn = capturedPawns.get(stepIndex);
            removePawn(capturedPawn); // This should handle removal from the board and updating the
            // game state
        }
    }

    private void processAfterCaptureMove(Pawn pawn, CapturePath path) {
        checkGameOver();
        promotePawnIfNeeded(pawn, path.getLastPosition());
        updateMovesListUI();
        recordBoardState();
        switchTurn();
        focusedPawn = null;
        renderPawns();

        // Update scores
        int captures = path.getCaptureCount();
        if (gameInfo != null) {
            if (pawn.isWhite()) {
                gameInfo.scorePlayerOne.set(gameInfo.scorePlayerOne.get() + captures);
            } else {
                gameInfo.scorePlayerTwo.set(gameInfo.scorePlayerTwo.get() + captures);
            }
        }
    }

    /**
     * Handles normal moves when no captures are available.
     *
     * @param board GridPane representing the board.
     * @param pawns List of all pawns.
     * @param pawn The selected pawn.
     * @param tileSize Size of each tile.
     * @param inBounds Predicate to check if a position is within bounds.
     * @param x Current x-coordinate of the pawn.
     * @param y Current y-coordinate of the pawn.
     */
    private void handleNormalMoves(
            Pawn pawn,
            BiPredicate<Integer, Integer> inBounds,
            int x,
            int y,
            boolean isAutomatic,
            boolean isAnimated) {
        BiConsumer<Integer, Integer> highlightMove =
                (newX, newY) -> {
                    Rectangle square = createHighlightSquare(Color.GREEN);
                    board.add(square, newX, newY);
                    possibleMoves.add(new Vector2i(newX, newY));

                    square.setOnMouseClicked(
                            event -> {
                                if (isWhiteTurn != pawn.isWhite()) {
                                    System.out.println("Not your turn!");
                                    return;
                                }
                                if (isAnimating) {
                                    System.out.println(
                                            "Please wait for the current move to finish.");
                                    return;
                                }
                                soundPlayer.playMoveSound();
                                Vector2i landingPos = new Vector2i(newX, newY);

                                takenMoves.add(new Move(pawn.getPosition(), landingPos));

                                if (isAnimated) {
                                    animatePawnMovement(
                                            pawn, landingPos, () -> executeMove(pawn, landingPos));
                                } else {
                                    executeMove(pawn, landingPos);
                                }
                            });
                };

        if (!pawn.isKing()) {
            int direction = pawn.isWhite() ? -1 : 1;
            int[][] moveDirections = {{-1, direction}, {1, direction}};

            for (int[] dir : moveDirections) {
                int newX = x + dir[0];
                int newY = y + dir[1];

                if (inBounds.test(newX, newY)
                        && getPawnAtPosition(new Vector2i(newX, newY)) == null) {
                    highlightMove.accept(newX, newY);
                    if (isAutomatic) {
                        executeMove(pawn, new Vector2i(newX, newY));
                        return; // Stop after the first automatic move
                    }
                }
            }
        } else {
            if (pawn.getNumberOfNonCapturingMoves() >= 3 && onlyKingsLeft()) {
                System.out.println("King cannot move more than 3 times without capturing.");
                return;
            }

            pawn.incrementNumberOfNonCapturingMoves();

            int[][] diagonalDirections = {{-1, -1}, {1, -1}, {-1, 1}, {1, 1}};
            for (int[] dir : diagonalDirections) {
                int dx = dir[0];
                int dy = dir[1];
                int newX = x + dx;
                int newY = y + dy;

                while (inBounds.test(newX, newY)
                        && getPawnAtPosition(new Vector2i(newX, newY)) == null) {
                    highlightMove.accept(newX, newY);
                    if (isAutomatic) {
                        executeMove(pawn, new Vector2i(newX, newY));
                        return;
                    }
                    newX += dx;
                    newY += dy;
                }
            }
        }
    }

    private void executeMove(Pawn pawn, Vector2i landingPos) {
        pawn.setPosition(landingPos);
        ImageView pawnView = pawnViews.get(pawn);
        GridPane.setColumnIndex(pawnView, landingPos.x);
        GridPane.setRowIndex(pawnView, landingPos.y);

        // Process the move as a normal move
        promotePawnIfNeeded(pawn, landingPos);
        checkGameOver();
        clearHighlights();
        updateMovesListUI();
        recordBoardState();
        switchTurn();
        focusedPawn = null;
    }

    private void addMoveToHistory(Pawn pawn, CapturePath path) {
        Vector2i landingPos = path.getLastPosition();
        List<Vector2i> capturedPositions =
                path.capturedPawns.stream().map(Pawn::getPosition).collect(Collectors.toList());

        // Create the Move object with the start and end positions
        takenMoves.add(new Move(pawn.getPosition(), landingPos, capturedPositions));
    }

    /**
     * Recursively explores all possible capture paths from a position.
     *
     * @param pawns List of all pawns.
     * @param pawn The pawn to move.
     * @param inBounds Predicate to check if a position is within bounds.
     * @param x Current x-coordinate.
     * @param y Current y-coordinate.
     * @param currentPath The current capture path.
     * @param allPaths List to collect all capture paths.
     */
    private void captureCheck(
            Pawn pawn,
            BiPredicate<Integer, Integer> inBounds,
            int x,
            int y,
            CapturePath currentPath,
            List<CapturePath> allPaths) {
        boolean foundCapture = false;

        int[][] directions = {
            {1, 1}, {-1, 1}, {1, -1}, {-1, -1},
            {0, 2}, {0, -2}, {2, 0}, {-2, 0}
        };

        for (int[] dir : directions) {
            int dx = dir[0];
            int dy = dir[1];

            int maxSteps = pawn.isKing() ? boardSize.x : 1;

            for (int i = 1; i <= maxSteps; i++) {
                int captureX = x + dx * i;
                int captureY = y + dy * i;

                // Check if capture position is within bounds
                if (!inBounds.test(captureX, captureY)) {
                    break; // Stop if out of bounds
                }

                Vector2i capturePos = new Vector2i(captureX, captureY);
                Pawn capturedPawn = getPawnAtPosition(capturePos);

                // If we find a capturable opponent pawn
                if (capturedPawn != null
                        && capturedPawn.isWhite() != pawn.isWhite()
                        && !currentPath.capturedPawns.contains(capturedPawn)) {

                    // Check for landing positions after capturing the pawn
                    for (int j = 1; j <= maxSteps; j++) {
                        int landingX = captureX + dx * j;
                        int landingY = captureY + dy * j;

                        if (!inBounds.test(landingX, landingY)) {
                            break; // Stop if landing position is out of bounds
                        }

                        Vector2i landingPos = new Vector2i(landingX, landingY);

                        // If landing position is empty, it's a valid move
                        if (getPawnAtPosition(landingPos) == null
                                || landingPos.equals(pawn.getPosition())) {
                            foundCapture = true;
                            CapturePath newPath = new CapturePath(currentPath);
                            newPath.addMove(landingPos, capturedPawn);

                            // Recursively check for further captures
                            captureCheck(pawn, inBounds, landingX, landingY, newPath, allPaths);
                        } else {
                            break; // Stop if the landing position is blocked
                        }
                    }

                    // **Stop if two pawns are adjacent**: Check for a pawn directly next to the
                    // first one
                    int nextX = captureX + dx;
                    int nextY = captureY + dy;
                    if (inBounds.test(nextX, nextY)
                            && getPawnAtPosition(new Vector2i(nextX, nextY)) != null) {
                        break; // Stop if there's no gap between pawns
                    }
                }

                // If a non-capturable piece is found, stop checking this direction
                if (capturedPawn != null) {
                    break;
                }
            }
        }

        // Add path to allPaths if a capture was made
        if (!foundCapture && currentPath.getCaptureCount() > 0) {
            allPaths.add(currentPath);
        }
    }

    /** Records the current state of the board. */
    private void recordBoardState() {
        Map<Vector2i, Pawn> currentState = new HashMap<>();
        for (Pawn pawn : pawns) {
            currentState.put(pawn.getPosition(), pawn);
        }
        GameState newState = new GameState(currentState, isWhiteTurn);

        pastStates.add(newState);

        // Check for threefold repetition
        long repetitionCount = pastStates.stream().filter(state -> state.equals(newState)).count();

        if (repetitionCount >= 3) {
            // Declare a draw
            Platform.runLater(
                    () -> {
                        Alert drawAlert = new Alert(Alert.AlertType.INFORMATION);
                        drawAlert.setTitle("Draw!");
                        drawAlert.setHeaderText("Threefold Repetition Rule");
                        drawAlert.setContentText(
                                "The game is a draw due to repeated board positions.");
                        drawAlert.showAndWait();
                        resetGame(tileSize * BOARD_SIZE);
                    });
        }
    }

    /** Checks if the game is over. */
    public void checkGameOver() {
        // Check if the current player has any pawns
        boolean oppositePlayerHasPawns = pawns.stream().anyMatch(p -> p.isWhite() == !isWhiteTurn);

        Platform.runLater(
                () -> {
                    if (!oppositePlayerHasPawns) {
                        Alert gameOverAlert = new Alert(Alert.AlertType.INFORMATION);
                        gameOverAlert.setTitle("Game Over");
                        gameOverAlert.setHeaderText("Game Over!");
                        gameOverAlert.setContentText("Player " + (isWhiteTurn ? 2 : 1) + " wins!");
                        gameOverAlert.showAndWait();
                        resetGame(tileSize * BOARD_SIZE);

                        // Optionally, disable further interactions
                        isActive = false;
                    }
                });
    }

    /**
     * Creates a highlight square for possible moves.
     *
     * @param tileSize Size of each tile.
     * @param color Color of the highlight.
     * @return Rectangle representing the highlight.
     */
    private Rectangle createHighlightSquare(Color color) {
        Rectangle square = new Rectangle(tileSize, tileSize);
        square.setFill(color);
        highlightNodes.add(square); // Keep track of highlights
        return square;
    }

    /**
     * Promotes a pawn to a king if it reaches the opposite end.
     *
     * @param pawn The pawn to check for promotion.
     * @param landingPos The landing position of the pawn.
     */
    private void promotePawnIfNeeded(Pawn pawn, Vector2i landingPos) {
        if ((pawn.isWhite() && landingPos.y == 0)
                || (!pawn.isWhite() && landingPos.y == boardSize.y - 1)) {
            pawn.setKing(true);
            ImageView pawnView = pawnViews.get(pawn);
            pawnView.setImage(pawn.getImage());
        }
    }

    /**
     * Clears any highlights from the board.
     *
     * @param board GridPane representing the board.
     * @param tileSize Size of each tile.
     */
    private void clearHighlights() {
        board.getChildren().removeAll(highlightNodes);
        highlightNodes.clear();
    }

    /**
     * Removes a captured pawn from the board and the list of pawns.
     *
     * @param board GridPane representing the board.
     * @param pawns List of all pawns.
     * @param capturedPawn The pawn to remove.
     */
    private void removePawn(Pawn capturedPawn) {
        pawns.remove(capturedPawn);
        requiredPawns.remove(capturedPawn);

        ImageView capturedPawnView = pawnViews.get(capturedPawn);

        FadeTransition fadeTransition = new FadeTransition(Duration.millis(300), capturedPawnView);
        fadeTransition.setFromValue(1.0);
        fadeTransition.setToValue(0.0);

        fadeTransition.setOnFinished(
                e -> {
                    board.getChildren().remove(capturedPawnView);
                    pawnViews.remove(capturedPawn);
                });

        fadeTransition.play();
    }

    /** Switches the turn to the opposite player. */
    private void switchTurn() {
        if (gameInfo == null) {
            return;
        }
        requiredPawns.clear();
        isWhiteTurn = !isWhiteTurn;
        gameInfo.playerTurn.set(isWhiteTurn ? 1 : 2);
        updatePlayerStyles();
        findPawnsWithMaxCaptures();
        if (!isWhiteTurn && isBotActive) {
            triggerBotMove();
        }
    }

    /** Checks if it is the white player's turn. */
    public boolean isWhiteTurn() {
        return isWhiteTurn;
    }

    /**
     * Method to undo the last move
     *
     * @param move Move to be undone
     */
    public void undoMove(Move move) {
        // Retrieve the pawn that was moved
        Vector2i initialPos = move.getStartPosition();
        Vector2i finalPos = move.getEndPosition();

        // Find the pawn that needs to be moved back
        Pawn movedPawn = getPawnAtPosition(finalPos);
        if (movedPawn != null) {
            // Move the pawn back to its original position
            movedPawn.setPosition(initialPos);

            // Clear the existing pawn view for the moved pawn
            ImageView movedPawnView = pawnViews.remove(movedPawn);
            if (movedPawnView != null) {
                board.getChildren().remove(movedPawnView); // Remove the existing ImageView
            }
            List<Vector2i> capturedPostions = move.getCapturedPositions();
            // If a capture occurred during the move, restore the captured pawn
            if (capturedPostions != null) {
                // Create a new captured pawn object
                for (Vector2i capturedPostion : capturedPostions) {
                    Pawn capturedPawn = new Pawn(capturedPostion, !movedPawn.isWhite());

                    // Restore the captured pawn to its original position
                    pawns.add(capturedPawn);
                }
            }
        }

        updateMovesListUI();
        switchTurn();
        // Clear any highlights and re-render the board
        clearHighlights();
        renderPawns();
    }

    public void undoLastMove() {
        if (takenMoves.size() > 0) {
            if (pastStates.size() > 0) {
                pastStates.removeLast();
            }
            undoMove(takenMoves.removeLast());
            updateMovesListUI();
        }
    }

    /** Resets the pawns to their initial positions. */
    private void resetPawnsToInitialPositions() {
        // Re-add any missing pawns
        for (Pawn pawn : allPawns) {
            if (!pawnViews.containsKey(pawn)) {
                ImageView pawnView = createPawnImageView(pawn, 0.8);
                pawns = new ArrayList<>(allPawns);
                setupPawnInteractions(pawnView, pawn);
                board.add(pawnView, pawn.getInitialPosition().x, pawn.getInitialPosition().y);
                GridPane.setHalignment(pawnView, HPos.CENTER);
                GridPane.setValignment(pawnView, VPos.CENTER);
                pawnViews.put(pawn, pawnView);
            }

            // Reset pawn properties
            pawn.setKing(false);
            pawnViews.get(pawn).setImage(pawn.getImage());
            pawn.setPosition(pawn.getInitialPosition());

            if (!pawns.contains(pawn)) {
                pawns.add(pawn);
            }

            // Update the pawn's ImageView position
            ImageView pawnView = pawnViews.get(pawn);
            GridPane.setColumnIndex(pawnView, pawn.getPosition().x);
            GridPane.setRowIndex(pawnView, pawn.getPosition().y);
        }
    }

    /**
     * Animates the movement of a pawn to a new position.
     *
     * @param pawn The pawn to move.
     * @param landingPos The landing position of the pawn.
     * @param onFinished Callback to run after the animation finishes.
     */
    private void animatePawnMovement(Pawn pawn, Vector2i landingPos, Runnable onFinished) {
        if (isAnimating) return; // Prevent new animations during an ongoing one
        isAnimating = true;
        ImageView pawnView = pawnViews.get(pawn);

        // Bring pawnView to front by moving it to the end of the children list
        board.getChildren().remove(pawnView);
        board.getChildren().add(pawnView);

        // Calculate translation distances
        double deltaX = (landingPos.x - pawn.getPosition().x) * tileSize;
        double deltaY = (landingPos.y - pawn.getPosition().y) * tileSize;

        // Create TranslateTransition
        TranslateTransition transition = new TranslateTransition(Duration.millis(300), pawnView);
        transition.setByX(deltaX);
        transition.setByY(deltaY);
        transition.setInterpolator(Interpolator.EASE_BOTH);

        // When the animation finishes, reset the translate values and update the GridPane position
        transition.setOnFinished(
                e -> {
                    // Reset translation
                    pawnView.setTranslateX(0);
                    pawnView.setTranslateY(0);

                    // Update the pawn's position in the GridPane
                    GridPane.setColumnIndex(pawnView, landingPos.x);
                    GridPane.setRowIndex(pawnView, landingPos.y);
                    pawn.setPosition(landingPos);

                    isAnimating = false;

                    // Call the onFinished callback
                    if (onFinished != null) {
                        onFinished.run();
                    }
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
    private void animatePawnCaptureMovement(Pawn pawn, CapturePath path, Runnable onFinished) {
        ImageView pawnView = pawnViews.get(pawn);
        List<Vector2i> positions = path.positions;
        List<Pawn> capturedPawns = path.capturedPawns;

        System.out.println("Animating moves");

        List<Animation> animations = new ArrayList<>();

        Vector2i currentPos = pawn.getPosition();
        for (int i = 0; i < positions.size(); i++) {
            Vector2i nextPos = positions.get(i);

            double deltaX = (nextPos.x - currentPos.x) * tileSize;
            double deltaY = (nextPos.y - currentPos.y) * tileSize;

            TranslateTransition transition =
                    new TranslateTransition(Duration.millis(300), pawnView);
            transition.setByX(deltaX);
            transition.setByY(deltaY);
            transition.setInterpolator(Interpolator.EASE_BOTH);

            // Create a final index variable for use in the lambda
            int index = i;

            transition.setOnFinished(
                    e -> {
                        // Reset translation
                        pawnView.setTranslateX(0);
                        pawnView.setTranslateY(0);

                        // Process each step to handle captures and update positions
                        processCaptureStep(pawn, nextPos, capturedPawns, index);
                    });

            animations.add(transition);

            currentPos = nextPos;
        }

        // Create a SequentialTransition
        SequentialTransition sequentialTransition = new SequentialTransition();
        sequentialTransition.getChildren().addAll(animations);

        sequentialTransition.setOnFinished(
                e -> {
                    isAnimating = false;
                    onFinished.run();
                });

        isAnimating = true;

        // Add whole CapturePath to takenMoves list
        List<Vector2i> capturedPositions = new ArrayList<>();
        path.capturedPawns.forEach(
                capturedPawn -> {
                    capturedPositions.add(capturedPawn.getPosition());
                });
        System.out.println(takenMoves.getLast());

        // Bring pawnView to front
        board.getChildren().remove(pawnView);
        board.getChildren().add(pawnView);

        sequentialTransition.play();
    }

    /** Checks if only kings are left on the board. */
    private boolean onlyKingsLeft() {
        return pawns.stream().allMatch(Pawn::isKing);
    }

    /** Updates the styles of the player names based on the current turn. */
    private void updatePlayerStyles() {
        BiConsumer<Text, Boolean> setPlayerStyle =
                (player, isPlayerOne) -> {
                    if (player != null) {
                        boolean shouldBeBold =
                                (isWhiteTurn && isPlayerOne) || (!isWhiteTurn && !isPlayerOne);
                        player.setStyle(
                                "-fx-font-size: "
                                        + (shouldBeBold ? 20 : 15)
                                        + ";"
                                        + "-fx-font-weight: "
                                        + (shouldBeBold ? "bold" : "normal"));
                    }
                };

        Text playerOne = (Text) root.lookup("#playerOneText");
        Text playerTwo = (Text) root.lookup("#playerTwoText");
        Text playerOneScore = (Text) root.lookup("#playerOneScore");
        Text playerTwoScore = (Text) root.lookup("#playerTwoScore");
        Text playerOneTime = (Text) root.lookup("#playerOneTime");
        Text playerTwoTime = (Text) root.lookup("#playerTwoTime");

        setPlayerStyle.accept(playerOne, true);
        setPlayerStyle.accept(playerTwo, false);
        setPlayerStyle.accept(playerOneScore, true);
        setPlayerStyle.accept(playerTwoScore, false);
        setPlayerStyle.accept(playerOneTime, true);
        setPlayerStyle.accept(playerTwoTime, false);
    }

    public void loadGameFromPDN(String pdnFilePath) {
        try {
            System.out.println("Loading game from: " + pdnFilePath);
            PDNParser pdnParser = new PDNParser(pdnFilePath);
            pdnParser.parseFile();
            for (Move move : pdnParser.getMoves()) {
                Pawn pawn = getPawnAtPosition(move.getStartPosition());
                if (pawn == null) {
                    System.out.println(
                            "No pawn at pos: "
                                    + move.getStartPosition().x
                                    + ", "
                                    + move.getStartPosition().y);
                    throw new RuntimeException("Couldn't find pawn to process the move: " + move);
                }
                List<CapturePath> possibleCapturePaths = findCapturePathsForPawn(pawn);
                boolean captureExecuted = false;
                for (CapturePath ct : possibleCapturePaths) {
                    if (ct.getLastPosition().equals(move.getEndPosition())) {
                        executeCaptureMove(pawn, ct, false);
                        captureExecuted = true;
                        break; // Capture executed, no need to check further paths for this move
                    }
                }

                if (!captureExecuted) {
                    // If no capture was executed, proceed with a normal move
                    executeMove(pawn, move.getEndPosition());
                }
            }

        } catch (Exception e) {
            System.err.println("Error loading game from PDN: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private List<CapturePath> findCapturePathsForPawn(Pawn pawn) {
        List<CapturePath> capturePaths = new ArrayList<>();

        // Define a boundary check for positions within board limits
        BiPredicate<Integer, Integer> inBounds =
                (x, y) -> x >= 0 && x < boardSize.x && y >= 0 && y < boardSize.y;

        // Start capture path search from the pawn's current position
        captureCheck(
                pawn,
                inBounds,
                pawn.getPosition().x,
                pawn.getPosition().y,
                new CapturePath(),
                capturePaths);

        return capturePaths;
    }

    private void resetTakenMoves() {
        takenMoves.clear();
        pastStates.clear();
        requiredPawns.clear();
        updateMovesListUI();
    }

    public List<Move> getTakenMoves() {
        return takenMoves;
    }

    private void triggerBotMove() {
        Platform.runLater(
                () -> {
                    try {
                        // Bot selects a move
                        GameState currentState = getBoardState();
                        List<Move> possibleMoves = currentState.generateMoves();

                        if (possibleMoves.isEmpty()) {
                            System.out.println("No possible moves for the bot.");
                            return;
                        }

                        Map<Vector2i, Double> qValues = botModel.predict(currentState);
                        Vector2i chosenAction =
                                qValues.entrySet().stream()
                                        .max(Map.Entry.comparingByValue())
                                        .map(Map.Entry::getKey)
                                        .orElse(null);

                        Move selectedMove =
                                possibleMoves.stream()
                                        .filter(move -> move.getEndPosition().equals(chosenAction))
                                        .findFirst()
                                        .orElse(null);

                        if (selectedMove == null) {
                            System.out.println("Bot selected an invalid action.");
                            return;
                        }

                        // Handle captures
                        if (selectedMove.getCapturedPositions() != null
                                && !selectedMove.getCapturedPositions().isEmpty()) {
                            for (Vector2i capturedPosition : selectedMove.getCapturedPositions()) {
                                Pawn capturedPawn = getPawnAtPosition(capturedPosition);
                                if (capturedPawn != null) {
                                    removePawn(
                                            capturedPawn); // Ensure this logic works for both mouse
                                    // and bot moves
                                }
                            }
                        }

                        // Apply the move and animate
                        MoveResult result = currentState.applyMove(selectedMove);
                        Pawn pawn = getPawnAtPosition(selectedMove.getStartPosition());
                        if (pawn != null) {
                            animatePawnMovement(
                                    pawn, selectedMove.getEndPosition(), () -> applyMove(result));
                        } else {
                            applyMove(result);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
    }

    private void trainModel(List<Experience> batch) {
        double totalLoss = 0.0;
        for (Experience experience : batch) {
            Map<Vector2i, Double> qValues = botModel.predict(experience.state);
            double target = experience.reward;
            if (!experience.isTerminal) {
                Map<Vector2i, Double> nextQValues = botModel.predict(experience.nextState);
                target += GAMMA * nextQValues.values().stream().max(Double::compareTo).orElse(0.0);
            }
            botModel.updateWeights(
                    experience.state, experience.action, target - qValues.get(experience.action));
            totalLoss += Math.abs(target - qValues.get(experience.action));
        }
        System.out.println("Average Loss: " + (totalLoss / batch.size()));
    }

    private void applyMove(MoveResult result) {
        if (result != null) {
            // Update the board state
            pastStates.add(result.getNextState());
            renderPawns(); // Ensure the board visually updates

            if (result.isGameOver()) {
                checkGameOver();
            }

            // Switch turn
            switchTurn();
        }
    }

    public GameState getBoardState() {
        Map<Vector2i, Pawn> currentState = new HashMap<>();

        // Iterate over all pawns and add their positions to the state
        for (Pawn pawn : pawns) {
            currentState.put(pawn.getPosition(), pawn);
        }

        return new GameState(currentState, isWhiteTurn);
    }

    private GameState createGameState() {
        return new GameState(
                pawns.stream().collect(Collectors.toMap(Pawn::getPosition, pawn -> pawn)),
                isWhiteTurn);
    }
}
