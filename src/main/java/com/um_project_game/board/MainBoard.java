package com.um_project_game.board;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

import com.um_project_game.Game;
import javafx.scene.layout.VBox;
import org.joml.Vector2i;

import com.um_project_game.util.SoundPlayer;

import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

/**
 * Represents the main board of the game.
 */
public class MainBoard {

    SoundPlayer soundPlayer = new SoundPlayer();

    private static final int TILEAMOUNT = 10;
    private final Vector2i tileamount = new Vector2i(TILEAMOUNT, TILEAMOUNT);
    private Pawn focusedPawn;
    private boolean isWhiteTurn = true; // White starts first

    private boolean isActive = true;
    private Pane root;

    private GameInfo gameInfo;

    private float tileSize;
    private GridPane board;
    private List<Pawn> pawns;
    // Moves stored in game for easy access
    private List<Move>moves = new ArrayList<>();

    private MovesListManager movesListManager;

    /**
     * Creates and returns the main board, rendering it to the root pane.
     *
     * @param root          Root pane of the window.
     * @param boardPixelSize Size of the board in pixels.
     * @param boardPosition Position where the board should be placed (x, y).
     * @return GridPane representing the board.
     */
    public GridPane getMainBoard(Pane root, float boardPixelSize, Vector2i boardPosition, GameInfo gameInfo, VBox movesListVBox) {
        tileSize = boardPixelSize / TILEAMOUNT;
        pawns = new ArrayList<>();
        board = new GridPane();
        isWhiteTurn = true;
        this.root = root;
        this.gameInfo = gameInfo;
        gameInfo.playerTurn.set(1);
        movesListManager = new MovesListManager(movesListVBox);

        board.setLayoutX(boardPosition.x);
        board.setLayoutY(boardPosition.y);

        setupBoard();
        renderBoard();
        renderPawns();

        return board;
    }

    /**
     * Resize the board according to the current board Size
     * @param boardPixelSize Size of the board in pixels.
     * @return GridPane
     */
    public GridPane resizeBoard(float boardPixelSize) {
        tileSize = boardPixelSize / TILEAMOUNT;

        // Resize the board tiles and pawns without recreating everything
        for (var child : board.getChildren()) {
            if (child instanceof Rectangle square) {
                // Resize each tile (Rectangle)
                square.setWidth(tileSize);
                square.setHeight(tileSize);
            } else if (child instanceof ImageView pawnView) {
                // Resize each pawn (ImageView)
                pawnView.setFitWidth(tileSize * 0.8);
                pawnView.setFitHeight(tileSize * 0.8);
                pawnView.hoverProperty().addListener((observable, oldValue, newValue) -> {
                    double scaleFactor = newValue ? 0.96 : 0.8; // Increase size on hover
                    pawnView.setFitWidth(tileSize * scaleFactor);
                    pawnView.setFitHeight(tileSize * scaleFactor);
                });
            }
        }

        // Avoid clearing and re-rendering the entire board to minimize lag
        return board;
    }

    public VBox getMovesListVBox() {
        return movesListManager.getMovesListVBox(); // Reference to the VBox for moves
    }

    private void updateMovesListUI() {
        movesListManager.updateMovesListUI(moves);
    }

    /**
     * Resets the game
     * @param boardPixelSize Size of the board in pixels.
     */
    public void resetGame(float boardPixelSize) {
        tileSize = boardPixelSize / TILEAMOUNT;
        pawns = new ArrayList<>();
        isWhiteTurn = true;
        board.getChildren().clear();
        renderBoard();
        setupBoard();
        renderPawns();
        gameInfo.scorePlayerOne.set(0);
        gameInfo.scorePlayerTwo.set(0);
    }

    /**
     * Creates and returns a random board, suitable for non-unique layouts.
     *
     * @param root Root pane of the window.
     * @param boardPixelSize Size of the board in pixels.
     * @return GridPane representing a random board.
     */
    public GridPane getRandomBoard(Pane root, float boardPixelSize) {
        tileSize = boardPixelSize / TILEAMOUNT;
        pawns = new ArrayList<>();
        board = new GridPane();

        isActive = false;

        setupBoard();
        renderBoard();
        renderPawns();
        board.getStyleClass().add("board");

        return board;
    }

    /**
     * Sets up the initial positions of the pawns on the board.
     *
     */
    private void setupBoard() {
        // Function to add pawns to the board
        BiConsumer<Integer, Boolean> addPawns = (startRow, isWhite) -> {
            for (int y = startRow; y < startRow + 4; y++) {
                for (int x = 0; x < tileamount.x; x++) {
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
    }

    /**
     * Renders the board grid by rendering each tile
     */
    private void renderBoard() {
        BiConsumer<Integer, Integer> renderTile = (x, y) -> {
            Rectangle square = new Rectangle(tileSize, tileSize);
            square.setFill((x + y) % 2 == 0 ? Color.WHITE : Color.BLACK);
            board.add(square, x, y);
        };

        for (int y = 0; y < tileamount.y; y++) {
            for (int x = 0; x < tileamount.x; x++) {
                renderTile.accept(x, y);
            }
        }
    }

    /**
     * Renders each pawn on the board
     */
    private void renderPawns() {
        double scaleFactor = 0.8;

        pawns.forEach(pawn -> {
            ImageView pawnView = createPawnImageView(pawn, scaleFactor);
            setupPawnInteractions(pawnView, pawn);
            board.add(pawnView, pawn.getPosition().x, pawn.getPosition().y);
            GridPane.setHalignment(pawnView, HPos.CENTER);
            GridPane.setValignment(pawnView, VPos.CENTER);
        });
    }

    /**
     * Creates an imageview for the pawn
     * @param pawn Current pawn
     * @param scaleFactor ScaleFactor of the imageview
     * @return ImageView
     */
    private ImageView createPawnImageView(Pawn pawn, double scaleFactor) {
        ImageView pawnView = new ImageView(pawn.getImage());

        pawnView.setFitWidth(tileSize * scaleFactor);
        pawnView.setFitHeight(tileSize * scaleFactor);
        pawnView.setPreserveRatio(true);
        pawnView.setUserData(pawn);

        return pawnView;
    }

    /**
     * Sets up the interactions for a pawn ImageView.
     * @param pawnView ImageView of the Pawn
     * @param pawn Current Pawn
     */
    private void setupPawnInteractions(ImageView pawnView, Pawn pawn) {
        pawnView.hoverProperty().addListener((observable, oldValue, newValue) -> {
            double scaleFactor = newValue ? 0.96 : 0.8; // Increase size on hover
            pawnView.setFitWidth(tileSize * scaleFactor);
            pawnView.setFitHeight(tileSize * scaleFactor);
        });

        if (!isActive) {
            return;
        }

        pawnView.setOnMouseClicked(event -> {
            clearHighlights();
            focusedPawn = pawn;
            seePossibleMove(focusedPawn);
            renderPawns();
        });
    }

    /**
     * Retrieves a pawn at a given position.
     *
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
     * Highlights possible moves for the selected pawn.
     * @param pawn Current Pawn
     */
    private void seePossibleMove(Pawn pawn) {
        Vector2i position = pawn.getPosition();
        int x = position.x;
        int y = position.y;

        BiPredicate<Integer, Integer> inBounds = (newX, newY) ->
                newX >= 0 && newX < tileamount.x && newY >= 0 && newY < tileamount.y;

        List<CapturePath> allPaths = new ArrayList<>();

        captureCheck(pawn, inBounds, x, y, new CapturePath(), allPaths);

        if (!allPaths.isEmpty()) {
            handleCaptureMoves(pawn,allPaths);
        } else {
            handleNormalMoves(pawn, inBounds, x, y);
        }
    }

    /**
     * Handles capture moves by highlighting them and setting up interactions.
     * @param pawn Current Pawn
     * @param allPaths All possible Capture Paths
     */
    private void handleCaptureMoves(Pawn pawn, List<CapturePath> allPaths) {
        int maxCaptures = allPaths.stream().mapToInt(CapturePath::getCaptureCount).max().orElse(0);

        List<CapturePath> maxCapturePaths = allPaths.stream()
                .filter(path -> path.getCaptureCount() == maxCaptures).toList();

        BiConsumer<CapturePath, Vector2i> highlightCaptureMove = (path, landingPos) -> {
            Rectangle square = createHighlightSquare(Color.RED);
            board.add(square, landingPos.x, landingPos.y);

            square.setOnMouseClicked(event -> {
                if (isWhiteTurn != pawn.isWhite()) {
                    System.out.println("Not your turn!");
                    return;
                }
                soundPlayer.playMoveSound();
                List<Vector2i> capturedPositions = new ArrayList<>();


                path.capturedPawns.forEach(capturedPawn -> {
                    capturedPositions.add(capturedPawn.getPosition());
                    removePawn(capturedPawn);
                });
                moves.add(new Move(pawn.getPosition(),landingPos, capturedPositions));
                pawn.setPosition(landingPos);
                promotePawnIfNeeded(pawn, landingPos);
                clearHighlights();
                renderPawns();
                updateMovesListUI();
                switchTurn();
                focusedPawn = null;
                if (pawn.isWhite()) {
                    gameInfo.scorePlayerOne.set(gameInfo.scorePlayerOne.get() + maxCaptures);
                } else {
                    gameInfo.scorePlayerTwo.set(gameInfo.scorePlayerTwo.get() + maxCaptures);
                }
            });
        };

        maxCapturePaths.forEach(path -> {
            Vector2i landingPos = path.getLastPosition();
            highlightCaptureMove.accept(path, landingPos);
        });
    }

    /**
     * Handles normal moves when no captures are available.
     * @param pawn Current Pawn
     * @param inBounds Bounds of the board
     * @param x Coordinate x
     * @param y Coordinate y
     */
    private void handleNormalMoves(Pawn pawn,
                                   BiPredicate<Integer, Integer> inBounds, int x, int y) {
        BiConsumer<Integer, Integer> highlightMove = (newX, newY) -> {
            Rectangle square = createHighlightSquare(Color.GREEN);
            board.add(square, newX, newY);

            square.setOnMouseClicked(event -> {
                if (isWhiteTurn != pawn.isWhite()) {
                    System.out.println("Not your turn!");
                    return;
                }
                soundPlayer.playMoveSound();
                Vector2i landingPos = new Vector2i(newX,newY);
                moves.add(new Move(pawn.getPosition(),landingPos));
                pawn.setPosition(landingPos);

                clearHighlights();
                promotePawnIfNeeded(pawn, landingPos);
                renderPawns();
                updateMovesListUI();
                switchTurn();
                focusedPawn = null;

            });
        };

        if (!pawn.isKing()) {
            int direction = pawn.isWhite() ? -1 : 1;
            int[][] moveDirections = {{-1, direction}, {1, direction}};

            for (int[] dir : moveDirections) {
                int newX = x + dir[0];
                int newY = y + dir[1];

                if (inBounds.test(newX, newY) && getPawnAtPosition(new Vector2i(newX, newY)) == null) {
                    highlightMove.accept(newX, newY);
                }
            }
        } else {
            int[][] diagonalDirections = {{-1, -1}, {1, -1}, {-1, 1}, {1, 1}};
            for (int[] dir : diagonalDirections) {
                int dx = dir[0];
                int dy = dir[1];
                int newX = x + dx;
                int newY = y + dy;

                while (inBounds.test(newX, newY) && getPawnAtPosition(new Vector2i(newX, newY)) == null) {
                    highlightMove.accept(newX, newY);
                    newX += dx;
                    newY += dy;
                }
            }
        }
    }

    /**
     * Recursively explores all possible capture paths from a position.
     *
     * @param pawn             The pawn to move.
     * @param inBounds         Predicate to check if a position is within bounds.
     * @param x                Current x-coordinate.
     * @param y                Current y-coordinate.
     * @param currentPath      The current capture path.
     * @param allPaths         List to collect all capture paths.
     */
    private void captureCheck(Pawn pawn,
                          BiPredicate<Integer, Integer> inBounds, int x, int y,
                          CapturePath currentPath, List<CapturePath> allPaths) {
    boolean foundCapture = false;

    int[][] directions = {
            {1, 1}, {-1, 1}, {1, -1}, {-1, -1},
            {0, 2}, {0, -2}, {2, 0}, {-2, 0}
    };

    for (int[] dir : directions) {
        int dx = dir[0];
        int dy = dir[1];

        int maxSteps = pawn.isKing() ? tileamount.x : 1;

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
            if (capturedPawn != null && capturedPawn.isWhite() != pawn.isWhite() && !currentPath.capturedPawns.contains(capturedPawn)) {

                // Check for landing positions after capturing the pawn
                for (int j = 1; j <= maxSteps; j++) {
                    int landingX = captureX + dx * j;
                    int landingY = captureY + dy * j;

                    if (!inBounds.test(landingX, landingY)) {
                        break; // Stop if landing position is out of bounds
                    }

                    Vector2i landingPos = new Vector2i(landingX, landingY);

                    // If landing position is empty, it's a valid move
                    if (getPawnAtPosition(landingPos) == null || landingPos.equals(pawn.getPosition())) {
                        foundCapture = true;
                        CapturePath newPath = new CapturePath(currentPath);
                        newPath.addMove(landingPos, capturedPawn);

                        // Recursively check for further captures
                        captureCheck(pawn, inBounds, landingX, landingY, newPath, allPaths);
                    } else {
                        break; // Stop if the landing position is blocked
                    }
                }

                // **Stop if two pawns are adjacent**: Check for a pawn directly next to the first one
                int nextX = captureX + dx;
                int nextY = captureY + dy;
                if (inBounds.test(nextX, nextY) && getPawnAtPosition(new Vector2i(nextX, nextY)) != null) {
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
    /**
     * Creates a highlight square for possible moves.
     *
     * @param color    Color of the highlight.
     * @return Rectangle representing the highlight.
     */
    private Rectangle createHighlightSquare(Color color) {
        Rectangle square = new Rectangle(tileSize, tileSize);
        square.setFill(color);
        return square;
    }

    /**
     * Promotes a pawn to a king if it reaches the opposite end.
     *
     * @param pawn       The pawn to check for promotion.
     * @param landingPos The landing position of the pawn.
     */
    private void promotePawnIfNeeded(Pawn pawn, Vector2i landingPos) {
        if ((pawn.isWhite() && landingPos.y == 0) || (!pawn.isWhite() && landingPos.y == tileamount.y - 1)) {
            pawn.setKing(true);
        }
    }

    /**
     * Clears any highlights from the board.
     */
    private void clearHighlights() {
        board.getChildren().removeIf(node -> node instanceof Rectangle);
        renderBoard();
    }

    /**
     * Removes a captured pawn from the board and the list of pawns.
     *
     * @param capturedPawn The pawn to remove.
     */
    private void removePawn(Pawn capturedPawn) {
        pawns.remove(capturedPawn);
        board.getChildren().removeIf(node -> {
            if (node instanceof ImageView imageView) {
                Integer colIndex = GridPane.getColumnIndex(imageView);
                Integer rowIndex = GridPane.getRowIndex(imageView);
                return colIndex != null && rowIndex != null &&
                        colIndex == capturedPawn.getPosition().x &&
                        rowIndex == capturedPawn.getPosition().y;
            }
            return false;
        });
    }

    /**
     * Switches the current player
     */
    public void switchTurn() {
        isWhiteTurn = !isWhiteTurn;
        gameInfo.playerTurn.set(isWhiteTurn ? 1 : 2);
        BiConsumer<Text, Boolean> setPlayerStyle = (player, isPlayerOne) -> {
            if (player != null) {
                boolean shouldBeBold = (isWhiteTurn && isPlayerOne) || (!isWhiteTurn && !isPlayerOne);
                player.setStyle("-fx-font-size: " + (shouldBeBold ? 20 : 15) + ";"
                                + "-fx-font-weight: " + (shouldBeBold ? "bold" : "normal"));
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

    /**
     * Method to undo the last move
     * @param move Move to be undone
     */
    public void undoMove(Move move) {
        // Retrieve the pawn that was moved
        Vector2i initialPos = move.getInitialPosition();
        Vector2i finalPos = move.getFinalPosition();

        // Find the pawn that needs to be moved back
        Pawn movedPawn = getPawnAtPosition(finalPos);
        if (movedPawn != null) {
            // Move the pawn back to its original position
            movedPawn.setPosition(initialPos);
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

            // Update the visual board to include the restored pawns
            renderPawns();
        }

        switchTurn();
        // Clear any highlights and re-render the board
        clearHighlights();
        renderPawns();
    }

    /**
     * @return Boolean indicating if white makes the next move
     */
    public boolean isWhiteTurn() {
        return isWhiteTurn;
    }

    public void undoLastMove() {
        if (moves.size() > 0) {
            undoMove(moves.getLast());
            moves.removeLast();
            updateMovesListUI();
        }
    }

    public List<Move> getMoves() {
        return moves;
    }

}