package com.um_project_game.board;

import com.um_project_game.Launcher;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.control.Alert;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
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

public class MoveManager {
    private final BoardState boardState;
    private final MainBoard mainBoard;
    private BoardRendered boardRendered;
    private BotManager botManager;

    public MoveManager(BoardState boardState, MainBoard mainBoard) {
        this.boardState = boardState;
        this.mainBoard = mainBoard;
    }

    public void setBotManager(BotManager botManager) {
        this.botManager = botManager;
    }

    public void setBoardRendered(BoardRendered boardRendered) {
        this.boardRendered = boardRendered;
    }

    /**
     * Retrieves a pawn at a given position.
     *
     * @param pawns List of all pawns.
     * @param position Position to check.
     * @return Pawn at the position, or null if none.
     */
    public Pawn getPawnAtPosition(Vector2i position) {
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
    public void seePossibleMove(Pawn pawn, boolean highlighting) {

        boardState.getPossibleMoves().clear();
        boardRendered.clearHighlights();
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

            // check if pawn is in requiredPawns:
            if (boardState.getRequiredPawns().contains(pawn)) {
                handleCaptureMoves(pawn, allPaths, true, highlighting);
            }
        } else if (boardState.getRequiredPawns().isEmpty()) {
            handleNormalMoves(pawn, inBounds, x, y, true, highlighting);
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
            Pawn pawn, List<CapturePath> allPaths, boolean isAnimated, boolean highlight) {

        double maxCaptureValue =
                allPaths.stream().mapToDouble(CapturePath::getCaptureValue).max().orElse(0);

        // Filter paths that have the maximum capture value
        List<CapturePath> maxCapturePaths =
                allPaths.stream()
                        .filter(path -> path.getCaptureValue() == maxCaptureValue)
                        .collect(Collectors.toList());

        for (CapturePath path : maxCapturePaths) {
            Vector2i landingPos = path.getLastPosition();

            Rectangle square = boardRendered.createHighlightSquare(Color.RED);
            if (highlight) {
                boardState.getBoard().add(square, landingPos.x, landingPos.y);
            }

            boardState.getPossibleMoves().add(landingPos);

            // Set up manual click event
            square.setOnMouseClicked(
                    _ -> {
                        if (boardState.isWhiteTurn() != pawn.isWhite()
                                || boardState.isAnimating()) {
                            System.out.println("Not your turn or animation in progress!");
                            return;
                        }
                        boardState.getSoundPlayer().playCaptureSound();

                        // Clear highlights and reset move count
                        boardRendered.clearHighlights();
                        pawn.resetNumberOfNonCapturingMoves();

                        // Execute capture move
                        executeCaptureMove(pawn, path, isAnimated);
                    });
        }
    }

    public void executeCaptureMove(Pawn pawn, CapturePath path, boolean isAnimated) {
        // Writing here what happens. If animated, then
        if (isAnimated) {
            mainBoard.animatePawnCaptureMovement(
                    pawn, path, () -> processAfterCaptureMove(pawn, path));
            addMoveToHistory(pawn, path);
        } else {
            // Directly process the capture steps without animation
            addMoveToHistory(pawn, path);
            processCaptureSteps(pawn, path);
            processAfterCaptureMove(pawn, path); // Finalize the move
        }
    }

    public void processCaptureSteps(Pawn pawn, CapturePath path) {
        List<Vector2i> positions = path.positions;
        List<Pawn> capturedPawns = path.capturedPawns;

        for (int i = 0; i < positions.size(); i++) {
            Vector2i nextPos = positions.get(i);

            // Update the pawn position and capture any pawns in the path
            processCaptureStep(pawn, nextPos, capturedPawns, i);
        }
    }

    public void processCaptureStep(
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

    public void processAfterCaptureMove(Pawn pawn, CapturePath path) {
        checkGameOver();
        promotePawnIfNeeded(pawn, path.getLastPosition());
        mainBoard.updateMovesListUI();
        recordBoardState();
        switchTurn();
        boardState.setFocusedPawn(null);
        boardRendered.renderPawns();

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
            Pawn pawn,
            BiPredicate<Integer, Integer> inBounds,
            int x,
            int y,
            boolean isAnimated,
            boolean highlighting) {
        BiConsumer<Integer, Integer> highlightMove =
                (newX, newY) -> {
                    Rectangle square = boardRendered.createHighlightSquare(Color.GREEN);
                    if (highlighting) {
                        boardState.getBoard().add(square, newX, newY);
                    }

                    boardState.getPossibleMoves().add(new Vector2i(newX, newY));

                    square.setOnMouseClicked(
                            _ -> {
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
                                    mainBoard.animatePawnMovement(
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

    public void executeMove(Pawn pawn, Vector2i landingPos) {
        pawn.setPosition(landingPos);
        ImageView pawnView = boardState.getPawnViews().get(pawn);
        System.out.println("Moving pawn to " + landingPos);

        GridPane.setColumnIndex(pawnView, landingPos.x);
        GridPane.setRowIndex(pawnView, landingPos.y);

        promotePawnIfNeeded(pawn, landingPos);
        checkGameOver();
        boardRendered.clearHighlights();
        mainBoard.updateMovesListUI();
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
    public List<Vector2i> captureCheck(
            Pawn pawn,
            BiPredicate<Integer, Integer> inBounds,
            int x,
            int y,
            CapturePath currentPath,
            List<CapturePath> allPaths) {
        boolean foundCapture = false;
        currentPath.initialPawn = pawn;
        List<Vector2i> capturedPositions = new ArrayList<>();

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

                // Check if capture position is within ounds
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

                            capturedPositions.add(capturePos);

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

        return capturedPositions;
    }

    /** Records the current state of the board. */
    private void recordBoardState() {
        Map<Vector2i, Pawn> currentState = new HashMap<>();
        for (Pawn pawn : boardState.getPawns()) {
            currentState.put(pawn.getPosition(), pawn);
        }
        GameState newState = new GameState(currentState, boardState.isWhiteTurn(), mainBoard);

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
                        mainBoard.resetGame(
                                boardState.getTileSize() * BoardState.getMainBoardSize());
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
                        mainBoard.resetGame(
                                boardState.getTileSize() * BoardState.getMainBoardSize());

                        boardState.setActive(false);
                    }
                });
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
     * Removes a captured pawn from the board and the list of pawns.
     *
     * @param board GridPane representing the board.
     * @param pawns List of all pawns.
     * @param capturedPawn The pawn to remove.
     */
    public void removePawn(Pawn capturedPawn) {

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
    public void switchTurn() {
        if (boardState.getGameInfo() == null) {
            return;
        }
        boardState.getRequiredPawns().clear();
        boardState.setWhiteTurn(!boardState.isWhiteTurn());
        boardState.getGameInfo().playerTurn.set(boardState.isWhiteTurn() ? 1 : 2);
        updatePlayerStyles();
        findPawnsWithMaxCaptures();

        // Here I need to get the pawns List instead of the maxCaptures one.
        GameState currentState =
                new GameState(boardState.getPawnPositionMap(), boardState.isWhiteTurn, mainBoard);
        Map<Pawn, List<Move>> validMovesMap = getValidMovesForState(currentState);
        List<Pawn> maxCaptures = new ArrayList<>(validMovesMap.keySet());

        boardRendered.highlightMovablePawns(maxCaptures);

        if (!boardState.isWhiteTurn() && boardState.isBotActive()) {
            if (Launcher.dqnbot) {
                botManager.triggerBotMove();
            } else {
                botManager.triggerBotMoveR();
            }
        }
        System.out.println("Player " + (boardState.isWhiteTurn() ? 1 : 2) + "'s turn");
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

        mainBoard.updateMovesListUI();
        if (!boardState.isBotActive()) {
            switchTurn();
        }
        // Clear any highlights and re-render the board
        boardRendered.clearHighlights();
        boardRendered.renderPawns();
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
            mainBoard.updateMovesListUI();
        }
    }

    /** Resets the pawns to their initial positions. */
    public void resetPawnsToInitialPositions() {
        // Re-add any missing pawns
        for (Pawn pawn : boardState.getAllPawns()) {
            if (!boardState.getPawnViews().containsKey(pawn)) {
                ImageView pawnView = boardRendered.createPawnImageView(pawn, 0.8);
                boardState.setPawns(new ArrayList<>(boardState.getAllPawns()));
                boardRendered.setupPawnInteractions(pawnView, pawn);
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

    /**
     * Finds pawns with the maximum number of captures.
     *
     * @param pawns List of all pawns.
     * @return List of pawns with the maximum number of captures.
     */
    public Map<Pawn, List<Move>> getValidMovesForState(GameState state) {
        Map<Pawn, List<Move>> pawnMovesMap = new HashMap<>();
        double maxCaptureValue = 0;
        List<Pawn> maxCapturesPawns = findPawnsWithMaxCaptures();
        List<Pawn> validPawns;

        if (maxCapturesPawns.size() > 0) {
            validPawns = maxCapturesPawns;
        } else {
            validPawns = boardState.getPawns();
        }
        // Iterate through all pawns in the game state
        for (Pawn pawn : validPawns) {
            Vector2i position = pawn.getPosition();

            // Ensure this pawn belongs to the current player
            if (pawn.isWhite() != state.isWhiteTurn()) {
                continue;
            }

            // Generate moves for this pawn
            seePossibleMove(pawn, false);

            List<Move> pawnMoves =
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
                            .collect(Collectors.toList());

            // Check for maximum capture value for the pawn
            double captureValue =
                    pawnMoves.stream()
                            .filter(move -> !move.getCapturedPositions().isEmpty())
                            .mapToDouble(
                                    move ->
                                            move.getCapturedPositions()
                                                    .size()) // Use size of captures
                            .max()
                            .orElse(0);

            // Update the maxCaptureValue for the state
            maxCaptureValue = Math.max(maxCaptureValue, captureValue);

            // Add the pawn and its moves to the map
            pawnMovesMap.put(pawn, pawnMoves);
        }

        // Filter the map to include only pawns with the longest moves
        double finalMaxCaptureValue = maxCaptureValue;
        Map<Pawn, List<Move>> filteredMap =
                pawnMovesMap.entrySet().stream()
                        .filter(
                                entry ->
                                        entry.getValue().stream()
                                                .anyMatch(
                                                        move ->
                                                                move.getCapturedPositions().size()
                                                                        == finalMaxCaptureValue))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        return filteredMap;
    }
}
