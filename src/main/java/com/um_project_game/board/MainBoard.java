package com.um_project_game.board;

import com.um_project_game.AI.DQNModel;
import com.um_project_game.AI.Experience;
import com.um_project_game.Launcher;
import com.um_project_game.util.PDNParser;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.PauseTransition;
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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

/** Represents the main board of the game. */
public class MainBoard {

    public BoardState boardState = new BoardState();

    public void setMovesListManager(MovesListManager movesListManager) {
        boardState.setMovesListManager(movesListManager);
    }

    public GridPane getBoard(GridPane movesListGridPane, GameInfo gameInfo) {
        boardState.setMovesListManager(new MovesListManager(movesListGridPane));
        boardState.setGameInfo(gameInfo);
        boardState.setActive(true);
        renderPawns();
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

        setupBoard();
        renderBoard();
        renderPawns();

        if (isBotActive) {
            boardState.setBotModel(new DQNModel(101, 100, 100, 0.1));
        }
        if (isBotvsBot) {
            boardState.setBotModel(new DQNModel(101, 100, 100, 0.1));
            playBotVsBot();
        }

        return boardState.getBoard();
    }

    public GridPane getMainBoardMultiplayer(
            Pane root,
            float boardPixelSize,
            Vector2i boardPosition,
            GameInfo gameInfo,
            GridPane movesListGridPane,
            boolean isMultiplayer) {
        boardState.setMultiplayer(isMultiplayer);
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
        clearHighlights();
        resetPawnsToInitialPositions();

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

        boardState.setActive(true);

        setupBoard();
        renderBoard();
        renderPawns();
        if (filePath != null) {
            loadGameFromPDN(filePath);
        }
        boardState.getBoard().getStyleClass().add("board");

        return boardState.getBoard();
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
        BiConsumer<Integer, Boolean> addPawns =
                (startRow, isWhite) -> {
                    for (int y = startRow; y < startRow + 4; y++) {
                        for (int x = 0; x < boardState.getBoardSize().x; x++) {
                            if ((x + y) % 2 == 1) {
                                boardState.getPawns().add(new Pawn(new Vector2i(x, y), isWhite));
                            }
                        }
                    }
                };

        // Add white pawns
        addPawns.accept(0, false);
        // Add black pawns
        addPawns.accept(6, true);

        boardState.getAllPawns().clear();
        boardState.getAllPawns().addAll(boardState.getPawns());
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

    private void updateMovesListUI() {
        if (boardState.getMovesListManager() != null) {
            boardState.getMovesListManager().updateMovesListUI(boardState.getTakenMoves());
        }
    }

    private void renderBoard() {
        if (boardState.isBoardInitialized()) return;

        for (int y = 0; y < boardState.getBoardSize().y; y++) {
            for (int x = 0; x < boardState.getBoardSize().x; x++) {
                Rectangle square =
                        new Rectangle(boardState.getTileSize(), boardState.getTileSize());
                square.setFill((x + y) % 2 == 0 ? Color.WHITE : Color.BLACK);
                boardState.getBoard().add(square, x, y);
                boardState.getBoardTiles()[x][y] = square; // Keep a reference to each tile
            }
        }
        boardState.setBoardInitialized(true);
    }

    /**
     * Renders the pawns onto the board.
     *
     * @param board GridPane representing the board.
     * @param pawns List of pawns to render.
     * @param tileSize Size of each tile.
     */
    private void renderPawns() {
        for (Pawn pawn : boardState.getPawns()) {
            ImageView pawnView = boardState.getPawnViews().get(pawn);

            if (pawnView == null) {
                pawnView = createPawnImageView(pawn, 0.8);
                boardState.getBoard().add(pawnView, pawn.getPosition().x, pawn.getPosition().y);
                GridPane.setHalignment(pawnView, HPos.CENTER);
                GridPane.setValignment(pawnView, VPos.CENTER);
                boardState.getPawnViews().put(pawn, pawnView);
            }

            setupPawnInteractions(pawnView, pawn);
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

        pawnView.setFitWidth(boardState.getTileSize() * scaleFactor);
        pawnView.setFitHeight(boardState.getTileSize() * scaleFactor);
        pawnView.setPreserveRatio(true);
        pawnView.setUserData(pawn);

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
                            pawnView.setFitWidth(boardState.getTileSize() * scaleFactor);
                            pawnView.setFitHeight(boardState.getTileSize() * scaleFactor);
                        });

        if (!boardState.isActive()) {
            return;
        }

        pawnView.setOnMouseClicked(
                event -> {
                    clearHighlights();
                    boardState.setFocusedPawn(pawn);
                    seePossibleMove(pawn);
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
        return boardState.getPawns().stream()
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

        for (Pawn pawn : boardState.getPawns()) { // Ensure only active pawns are iterated
            // Only consider pawns belonging to the current player
            if (pawn.isWhite() != boardState.isWhiteTurn()) continue;

            List<CapturePath> paths = new ArrayList<>();
            captureCheck(
                    pawn,
                    (x, y) ->
                            x >= 0
                                    && x < boardState.getBoardSize().x
                                    && y >= 0
                                    && y < boardState.getBoardSize().y,
                    pawn.getPosition().x,
                    pawn.getPosition().y,
                    new CapturePath(),
                    paths);

            if (!paths.isEmpty()) {
                pawnCapturePaths.put(pawn, paths);
            }
        }

        if (pawnCapturePaths.isEmpty()) {
            boardState.getRequiredPawns().clear();
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

        boardState.setRequiredPawns(
                required.stream()
                        .filter(boardState.getPawns()::contains)
                        .collect(Collectors.toList()));

        return boardState.getRequiredPawns();
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
        boardState.getPossibleMoves().clear();
        clearHighlights();
        Vector2i position = pawn.getPosition();
        int x = position.x;
        int y = position.y;

        BiPredicate<Integer, Integer> inBounds =
                (newX, newY) ->
                        newX >= 0
                                && newX < boardState.getBoardSize().x
                                && newY >= 0
                                && newY < boardState.getBoardSize().y;

        List<CapturePath> allPaths = new ArrayList<>();

        captureCheck(pawn, inBounds, x, y, new CapturePath(), allPaths);

        boardState.setCurrentCapturePaths(new ArrayList<>(allPaths));

        if (!allPaths.isEmpty()) {
            handleCaptureMoves(pawn, allPaths, true);
        } else if (boardState.getRequiredPawns().isEmpty()) {
            handleNormalMoves(pawn, inBounds, x, y, true);
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
    private void handleCaptureMoves(Pawn pawn, List<CapturePath> allPaths, boolean isAnimated) {
        double maxCaptureValue =
                allPaths.stream().mapToDouble(CapturePath::getCaptureValue).max().orElse(0);

        // Filter paths that have the maximum capture value
        List<CapturePath> maxCapturePaths =
                allPaths.stream()
                        .filter(path -> path.getCaptureValue() == maxCaptureValue)
                        .collect(Collectors.toList());

        for (CapturePath path : maxCapturePaths) {
            Vector2i landingPos = path.getLastPosition();

            Rectangle square = createHighlightSquare(Color.RED);
            boardState.getBoard().add(square, landingPos.x, landingPos.y);
            boardState.getPossibleMoves().add(landingPos);

            // Set up manual click event
            square.setOnMouseClicked(
                    event -> {
                        if (boardState.isWhiteTurn() != pawn.isWhite()
                                || boardState.isAnimating()) {
                            System.out.println("Not your turn or animation in progress!");
                            return;
                        }
                        boardState.getSoundPlayer().playCaptureSound();

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
        ImageView pawnView = boardState.getPawnViews().get(pawn);

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
        boardState.setFocusedPawn(null);
        renderPawns();

        // Update scores
        int captures = path.getCaptureCount();
        if (boardState.getGameInfo() != null) {
            if (pawn.isWhite()) {
                boardState
                        .getGameInfo()
                        .scorePlayerOne
                        .set(boardState.getGameInfo().scorePlayerOne.get() + captures);
            } else {
                boardState
                        .getGameInfo()
                        .scorePlayerTwo
                        .set(boardState.getGameInfo().scorePlayerTwo.get() + captures);
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
            Pawn pawn, BiPredicate<Integer, Integer> inBounds, int x, int y, boolean isAnimated) {
        BiConsumer<Integer, Integer> highlightMove =
                (newX, newY) -> {
                    Rectangle square = createHighlightSquare(Color.GREEN);
                    boardState.getBoard().add(square, newX, newY);
                    boardState.getPossibleMoves().add(new Vector2i(newX, newY));

                    square.setOnMouseClicked(
                            event -> {
                                if (boardState.isWhiteTurn() != pawn.isWhite()) {
                                    System.out.println("Not your turn!");
                                    return;
                                }
                                if (boardState.isAnimating()) {
                                    System.out.println(
                                            "Please wait for the current move to finish.");
                                    return;
                                }
                                boardState.getSoundPlayer().playMoveSound();
                                Vector2i landingPos = new Vector2i(newX, newY);

                                boardState
                                        .getTakenMoves()
                                        .add(new Move(pawn.getPosition(), landingPos));

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

                    newX += dx;
                    newY += dy;
                }
            }
        }
    }

    private void executeMove(Pawn pawn, Vector2i landingPos) {
        pawn.setPosition(landingPos);
        ImageView pawnView = boardState.getPawnViews().get(pawn);
        GridPane.setColumnIndex(pawnView, landingPos.x);
        GridPane.setRowIndex(pawnView, landingPos.y);

        promotePawnIfNeeded(pawn, landingPos);
        checkGameOver();
        clearHighlights();
        updateMovesListUI();
        recordBoardState();
        switchTurn();
        boardState.setFocusedPawn(null);
    }

    private void addMoveToHistory(Pawn pawn, CapturePath path) {
        Vector2i landingPos = path.getLastPosition();
        List<Vector2i> capturedPositions =
                path.capturedPawns.stream().map(Pawn::getPosition).collect(Collectors.toList());

        // Create the Move object with the start and end positions
        boardState.getTakenMoves().add(new Move(pawn.getPosition(), landingPos, capturedPositions));
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
        currentPath.initialPawn = pawn;

        int[][] directions = {
            {1, 1}, {-1, 1}, {1, -1}, {-1, -1},
            {0, 2}, {0, -2}, {2, 0}, {-2, 0}
        };

        for (int[] dir : directions) {
            int dx = dir[0];
            int dy = dir[1];

            int maxSteps = pawn.isKing() ? boardState.getBoardSize().x : 1;

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
        for (Pawn pawn : boardState.getPawns()) {
            currentState.put(pawn.getPosition(), pawn);
        }
        GameState newState = new GameState(currentState, boardState.isWhiteTurn(), this);

        boardState.getPastStates().add(newState);

        // Check for threefold repetition
        long repetitionCount =
                boardState.getPastStates().stream().filter(state -> state.equals(newState)).count();

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
                        resetGame(boardState.getTileSize() * BoardState.getMainBoardSize());
                    });
        }
    }

    /** Checks if the game is over. */
    public void checkGameOver() {
        // Check if the current player has any pawns
        boolean oppositePlayerHasPawns =
                boardState.getPawns().stream()
                        .anyMatch(p -> p.isWhite() == !boardState.isWhiteTurn());

        Platform.runLater(
                () -> {
                    if (!oppositePlayerHasPawns) {
                        Alert gameOverAlert = new Alert(Alert.AlertType.INFORMATION);
                        gameOverAlert.setTitle("Game Over");
                        gameOverAlert.setHeaderText("Game Over!");
                        gameOverAlert.setContentText(
                                "Player " + (boardState.isWhiteTurn() ? 2 : 1) + " wins!");
                        gameOverAlert.showAndWait();
                        resetGame(boardState.getTileSize() * BoardState.getMainBoardSize());

                        boardState.setActive(false);
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
        Rectangle square = new Rectangle(boardState.getTileSize(), boardState.getTileSize());
        square.setFill(color);
        boardState.getHighlightNodes().add(square); // Keep track of highlights
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
                || (!pawn.isWhite() && landingPos.y == boardState.getBoardSize().y - 1)) {
            pawn.setKing(true);
            ImageView pawnView = boardState.getPawnViews().get(pawn);
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
        boardState.getBoard().getChildren().removeAll(boardState.getHighlightNodes());
        boardState.getHighlightNodes().clear();
    }

    /**
     * Removes a captured pawn from the board and the list of pawns.
     *
     * @param board GridPane representing the board.
     * @param pawns List of all pawns.
     * @param capturedPawn The pawn to remove.
     */
    private void removePawn(Pawn capturedPawn) {
        boardState.getPawns().remove(capturedPawn);
        boardState.getRequiredPawns().remove(capturedPawn);

        ImageView capturedPawnView = boardState.getPawnViews().get(capturedPawn);

        FadeTransition fadeTransition = new FadeTransition(Duration.millis(300), capturedPawnView);
        fadeTransition.setFromValue(1.0);
        fadeTransition.setToValue(0.0);

        fadeTransition.setOnFinished(
                _ -> {
                    boardState.getBoard().getChildren().remove(capturedPawnView);
                    boardState.getPawnViews().remove(capturedPawn);
                });

        fadeTransition.play();
    }

    /** Switches the turn to the opposite player. */
    private void switchTurn() {
        if (boardState.getGameInfo() == null) {
            return;
        }
        boardState.getRequiredPawns().clear();
        boardState.setWhiteTurn(!boardState.isWhiteTurn());
        boardState.getGameInfo().playerTurn.set(boardState.isWhiteTurn() ? 1 : 2);
        updatePlayerStyles();
        findPawnsWithMaxCaptures();

        if (!boardState.isWhiteTurn() && boardState.isBotActive()) {
            if (Launcher.dqnbot) {
                triggerBotMove();
            } else {
                triggerBotMoveR();
            }
        }
        System.out.println("Player " + (boardState.isWhiteTurn() ? 1 : 2) + "'s turn");
    }

    /** Checks if it is the white player's turn. */
    public boolean isWhiteTurn() {
        return boardState.isWhiteTurn();
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

        boardState.getRequiredPawns().clear();
        // Find the pawn that needs to be moved back
        Pawn movedPawn = getPawnAtPosition(finalPos);
        if (movedPawn != null) {
            // Move the pawn back to its original position
            movedPawn.setPosition(initialPos);

            // Clear the existing pawn view for the moved pawn
            ImageView movedPawnView = boardState.getPawnViews().remove(movedPawn);
            if (movedPawnView != null) {
                boardState.getBoard().getChildren().remove(movedPawnView);
            }
            List<Vector2i> capturedPostions = move.getCapturedPositions();
            // If a capture occurred during the move, restore the captured pawn
            if (capturedPostions != null) {
                // Create a new captured pawn object
                for (Vector2i capturedPostion : capturedPostions) {
                    Pawn capturedPawn = new Pawn(capturedPostion, !movedPawn.isWhite());

                    // Restore the captured pawn to its original position
                    boardState.getPawns().add(capturedPawn);
                }
            }
        }

        updateMovesListUI();
        if (!boardState.isBotActive()) {
            switchTurn();
        }
        // Clear any highlights and re-render the board
        clearHighlights();
        renderPawns();
    }

    public void undoLastMove() {
        if (boardState.getTakenMoves().size() > 0) {
            if (boardState.getPastStates().size() > 0) {
                boardState.getPastStates().removeLast();
            }
            undoMove(boardState.getTakenMoves().removeLast());
            if (boardState.isBotActive()) {
                undoMove(boardState.getTakenMoves().removeLast());
            }
            updateMovesListUI();
        }
    }

    /** Resets the pawns to their initial positions. */
    private void resetPawnsToInitialPositions() {
        // Re-add any missing pawns
        for (Pawn pawn : boardState.getAllPawns()) {
            if (!boardState.getPawnViews().containsKey(pawn)) {
                ImageView pawnView = createPawnImageView(pawn, 0.8);
                boardState.setPawns(new ArrayList<>(boardState.getAllPawns()));
                setupPawnInteractions(pawnView, pawn);
                boardState
                        .getBoard()
                        .add(pawnView, pawn.getInitialPosition().x, pawn.getInitialPosition().y);
                GridPane.setHalignment(pawnView, HPos.CENTER);
                GridPane.setValignment(pawnView, VPos.CENTER);
                boardState.getPawnViews().put(pawn, pawnView);
            }

            // Reset pawn properties
            pawn.setKing(false);
            boardState.getPawnViews().get(pawn).setImage(pawn.getImage());
            pawn.setPosition(pawn.getInitialPosition());

            if (!boardState.getPawns().contains(pawn)) {
                boardState.getPawns().add(pawn);
            }

            // Update the pawn's ImageView position
            ImageView pawnView = boardState.getPawnViews().get(pawn);
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
    private void animatePawnCaptureMovement(Pawn pawn, CapturePath path, Runnable onFinished) {
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
                        // Reset translation
                        pawnView.setTranslateX(0);
                        pawnView.setTranslateY(0);

                        // Process each step to handle captures and update positions
                        processCaptureStep(pawn, nextPos, capturedPawns, index);
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
        System.out.println(boardState.getTakenMoves().getLast());

        // Bring pawnView to front
        boardState.getBoard().getChildren().remove(pawnView);
        boardState.getBoard().getChildren().add(pawnView);

        sequentialTransition.play();
    }

    /** Checks if only kings are left on the board. */
    private boolean onlyKingsLeft() {
        return boardState.getPawns().stream().allMatch(Pawn::isKing);
    }

    /** Updates the styles of the player names based on the current turn. */
    private void updatePlayerStyles() {
        BiConsumer<Text, Boolean> setPlayerStyle =
                (player, isPlayerOne) -> {
                    if (player != null) {
                        boolean shouldBeBold =
                                (boardState.isWhiteTurn() && isPlayerOne)
                                        || (!boardState.isWhiteTurn() && !isPlayerOne);
                        player.setStyle(
                                "-fx-font-size: "
                                        + (shouldBeBold ? 20 : 15)
                                        + ";"
                                        + "-fx-font-weight: "
                                        + (shouldBeBold ? "bold" : "normal"));
                    }
                };

        Text playerOne = (Text) boardState.getRoot().lookup("#playerOneText");
        Text playerTwo = (Text) boardState.getRoot().lookup("#playerTwoText");
        Text playerOneScore = (Text) boardState.getRoot().lookup("#playerOneScore");
        Text playerTwoScore = (Text) boardState.getRoot().lookup("#playerTwoScore");
        Text playerOneTime = (Text) boardState.getRoot().lookup("#playerOneTime");
        Text playerTwoTime = (Text) boardState.getRoot().lookup("#playerTwoTime");

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

            renderPawns();
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
        captureCheck(
                pawn,
                inBounds,
                pawn.getPosition().x,
                pawn.getPosition().y,
                new CapturePath(),
                capturePaths);

        return capturePaths;
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
            seePossibleMove(pawn);
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

    private void resetTakenMoves() {
        boardState.getTakenMoves().clear();
        boardState.getPastStates().clear();
        boardState.getRequiredPawns().clear();
        updateMovesListUI();
    }

    public List<Move> getTakenMoves() {
        return boardState.getTakenMoves();
    }

    private void triggerBotMove() {
        Platform.runLater(
                () -> {
                    try {
                        GameState currentState = getBoardState();

                        ArrayList<Pawn> capturedPawnsList = new ArrayList<>();

                        // Compute capture paths for the bot
                        List<CapturePath> capturePaths = computeCapturePathsForBot();
                        Map<Vector2i, Double> qValues =
                                boardState.getBotModel().predict(currentState);

                        if (capturePaths != null && !capturePaths.isEmpty()) {
                            System.out.println("Bot has capture opportunities. Available paths:");
                            for (CapturePath path : capturePaths) {
                                System.out.println(
                                        " - Path: "
                                                + path.positions
                                                + ", Captures: "
                                                + path.capturedPawns);
                            }

                            // Select the capture path with the highest predicted value
                            CapturePath bestPath =
                                    capturePaths.stream()
                                            .max(
                                                    Comparator.comparingDouble(
                                                            path ->
                                                                    qValues.getOrDefault(
                                                                            path.getLastPosition(),
                                                                            0.0)))
                                            .orElse(null);

                            if (bestPath != null) {
                                System.out.println("Best capture path: " + bestPath);
                                Pawn pawn = bestPath.initialPawn;
                                if (pawn != null) {
                                    capturedPawnsList.addAll(bestPath.capturedPawns);

                                    ArrayList<Vector2i> capturedPawnPositions = new ArrayList<>();
                                    capturedPawnsList.forEach(
                                            capturedPawn ->
                                                    capturedPawnPositions.add(
                                                            capturedPawn.getPosition()));

                                    animatePawnCaptureMovement(
                                            pawn,
                                            bestPath,
                                            () -> processAfterCaptureMove(pawn, bestPath));
                                    boardState
                                            .getTakenMoves()
                                            .add(
                                                    new Move(
                                                            pawn.getPosition(),
                                                            bestPath.getLastPosition(),
                                                            capturedPawnPositions));
                                    return; // Ensure no fallback to normal moves
                                }
                            }
                        }

                        // If no captures, proceed with normal moves
                        List<Move> possibleMoves = currentState.generateMoves();
                        if (possibleMoves.isEmpty()) {
                            System.out.println("No possible moves for the bot.");
                            return;
                        }

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

                        if (selectedMove != null) {
                            Pawn pawn = getPawnAtPosition(selectedMove.getStartPosition());
                            if (pawn != null) {
                                boardState.getTakenMoves().add(selectedMove);
                                animatePawnMovement(
                                        pawn,
                                        selectedMove.getEndPosition(),
                                        () -> applyMove(currentState.applyMove(selectedMove)));
                                return;
                            } else {
                                System.out.println("Fallback for takenMoves: " + selectedMove);
                                boardState.getTakenMoves().add(selectedMove);
                                applyMove(currentState.applyMove(selectedMove));
                                return;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
    }

    private void triggerBotMoveR() {
        Platform.runLater(
                () -> {
                    try {
                        // Compute capture paths for the bot
                        List<CapturePath> capturePaths = computeCapturePathsForBot();
                        if (capturePaths != null && !capturePaths.isEmpty()) {
                            System.out.println("Bot has capture opportunities. Available paths:");
                            for (CapturePath path : capturePaths) {
                                System.out.println(
                                        " - Path: "
                                                + path.positions
                                                + ", Captures: "
                                                + path.capturedPawns);
                            }

                            // Find the maximum capture value
                            double maxCaptureValue =
                                    capturePaths.stream()
                                            .mapToDouble(CapturePath::getCaptureValue)
                                            .max()
                                            .orElse(Double.NEGATIVE_INFINITY);

                            // Filter paths with the maximum value
                            List<CapturePath> bestPaths =
                                    capturePaths.stream()
                                            .filter(
                                                    path ->
                                                            path.getCaptureValue()
                                                                    == maxCaptureValue)
                                            .collect(Collectors.toList());

                            // Randomly select one of the best paths
                            CapturePath bestPath =
                                    bestPaths.get(new Random().nextInt(bestPaths.size()));

                            System.out.println("Selected capture path: " + bestPath);

                            if (bestPath != null) {
                                Pawn pawn = bestPath.initialPawn;
                                if (pawn != null) {
                                    System.out.println("Bot executing capture path: " + bestPath);

                                    boardState
                                            .getTakenMoves()
                                            .add(
                                                    new Move(
                                                            pawn.getPosition(),
                                                            bestPath.getLastPosition(),
                                                            bestPath.capturedPawns.stream()
                                                                    .map(Pawn::getPosition)
                                                                    .collect(Collectors.toList())));
                                    animatePawnCaptureMovement(
                                            pawn,
                                            bestPath,
                                            () -> processAfterCaptureMove(pawn, bestPath));
                                    return; // Ensure no fallback to normal moves
                                }
                            }
                        }

                        // If no captures, proceed with normal moves
                        GameState currentState = getBoardState();
                        List<Move> possibleMoves = currentState.generateMoves();

                        if (possibleMoves.isEmpty()) {
                            System.out.println("No possible moves for the agent.");
                            return;
                        }

                        // Randomly select a normal move
                        Random random = new Random();
                        Move selectedMove = possibleMoves.get(random.nextInt(possibleMoves.size()));

                        if (selectedMove != null) {
                            Pawn pawn = getPawnAtPosition(selectedMove.getStartPosition());
                            if (pawn != null) {
                                boardState.getTakenMoves().add(selectedMove);
                                animatePawnMovement(
                                        pawn,
                                        selectedMove.getEndPosition(),
                                        () -> applyMove(currentState.applyMove(selectedMove)));
                            } else {
                                boardState.getTakenMoves().add(selectedMove);
                                applyMove(currentState.applyMove(selectedMove));
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
    }

    private List<CapturePath> computeCapturePathsForBot() {

        List<Pawn> whitepawns =
                this.boardState.getPawns().stream()
                        .filter(pawn -> pawn.isWhite())
                        .collect(Collectors.toList()); // Bot's pawns

        List<Pawn> botPawnsblack =
                boardState.getPawns().stream()
                        .filter(pawn -> !pawn.isWhite())
                        .collect(Collectors.toList()); // Bot's pawns
        List<CapturePath> allCapturePaths = new ArrayList<>();

        for (Pawn pawn :
                boardState.isWhiteTurn() && boardState.isBotvsBot() ? whitepawns : botPawnsblack) {
            seePossibleMove(pawn);
            if (boardState.getCurrentCapturePaths() != null
                    && !boardState.getCurrentCapturePaths().isEmpty()) {
                allCapturePaths.addAll(boardState.getCurrentCapturePaths()); // Add computed paths
            }
        }

        return allCapturePaths;
    }

    private void trainModel(List<Experience> batch) {
        double totalLoss = 0.0;
        for (Experience experience : batch) {
            Map<Vector2i, Double> qValues = boardState.getBotModel().predict(experience.state);
            double target = experience.reward;
            if (!experience.isTerminal) {
                Map<Vector2i, Double> nextQValues =
                        boardState.getBotModel().predict(experience.nextState);
                target +=
                        BoardState.getGamma()
                                * nextQValues.values().stream().max(Double::compareTo).orElse(0.0);
            }
            boardState
                    .getBotModel()
                    .updateWeights(
                            experience.state,
                            experience.action,
                            target - qValues.get(experience.action));
            totalLoss += Math.abs(target - qValues.get(experience.action));
        }
        System.out.println("Average Loss: " + (totalLoss / batch.size()));
    }

    private void applyMove(MoveResult result) {
        if (result != null) {
            // Update the board state
            boardState.getPastStates().add(result.getNextState());
            renderPawns();

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

    public void playBotVsBot() {
        Platform.runLater(
                () -> {
                    if (!boardState.isActive()) {
                        System.out.println("Game is inactive. Stopping bot-vs-bot.");
                        return;
                    }

                    if (boardState.isBotvsBot()) {
                        if (boardState.isWhiteTurn()) {
                            triggerBotMoveR(); // Random bot for White
                        } else {
                            triggerBotMove(); // Other bot for Black
                        }
                        // Delay to make the moves visually distinguishable and avoid crash due to
                        // the game buffer
                        PauseTransition pause = new PauseTransition(Duration.millis(1000));
                        pause.setOnFinished(_ -> playBotVsBot());
                        pause.play();
                    }
                });
    }

    public void refreshBoard() {
        if (!boardState.getRoot().getChildren().contains(boardState.getBoard())) {
            boardState
                    .getRoot()
                    .getChildren()
                    .add(boardState.getBoard()); // Re-add the board if not already in the root
        }
        boardState.getBoard().toFront(); // Bring the board to the front
    }
}
