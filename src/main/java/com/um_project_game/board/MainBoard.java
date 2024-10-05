package com.um_project_game.board;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

import org.joml.Vector2i;

import com.um_project_game.util.SoundPlayer;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;

/**
 * Represents the main board of the game.
 */
public class MainBoard {

    // Constants
private static final int BOARD_SIZE = 10;

    // Game state variables
    private boolean isWhiteTurn = true; // White starts first
    private boolean isActive = true;
    private boolean boardInitialized = false;
    private boolean isAnimating = false;

    // Board-related fields
    private Vector2i boardSize = new Vector2i(BOARD_SIZE, BOARD_SIZE);
    private float tileSize;
    private GridPane board;
    private Node[][] boardTiles = new Node[BOARD_SIZE][BOARD_SIZE];
    private Pane root;

    // Pawn and move management
    private Pawn focusedPawn;
    private List<Pawn> allPawns = new ArrayList<>();
    private List<Vector2i> possibleMoves = new ArrayList<>();
    private List<Vector2i> listMoves = new ArrayList<>();
    private Map<Pawn, ImageView> pawnViews = new HashMap<>();
    private List<Node> highlightNodes = new ArrayList<>();

    // Sound and game info
    private SoundPlayer soundPlayer = new SoundPlayer();
    private GameInfo gameInfo;

    /**
     * Creates and returns the main board, rendering it to the root pane.
     *
     * @param root          Root pane of the window.
     * @param boardPixelSize Size of the board in pixels.
     * @param boardPosition Position where the board should be placed (x, y).
     * @return GridPane representing the board.
     */
    public GridPane getMainBoard(Pane root, float boardPixelSize, Vector2i boardPosition, GameInfo gameInfo) {
        tileSize = boardPixelSize / BOARD_SIZE;
        List<Pawn> pawns = new ArrayList<>();
        board = new GridPane();
        isWhiteTurn = true;
        this.root = root;
        this.gameInfo = gameInfo;
        gameInfo.playerTurn.set(1);

        board.setLayoutX(boardPosition.x);
        board.setLayoutY(boardPosition.y);

        setupBoard(pawns);
        renderBoard();
        renderPawns(pawns);

        return board;
    }

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

    public void resetGame(float boardPixelSize) {
        tileSize = boardPixelSize / BOARD_SIZE;
        isWhiteTurn = true;
        gameInfo.scorePlayerOne.set(0);
        gameInfo.scorePlayerTwo.set(0);
        gameInfo.playerTurn.set(1);

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

        // Reset the turn indicator
        switchTurn();
    }

    /**
     * Creates and returns a random board, suitable for non-unique layouts.
     *
     * @param root          Root pane of the window.
     * @param boardPixelSize Size of the board in pixels.
     * @return GridPane representing a random board.
     */
    public GridPane getRandomBoard(Pane root, float boardPixelSize) {
        tileSize = boardPixelSize / BOARD_SIZE;
        List<Pawn> pawns = new ArrayList<>();
        board = new GridPane();

        isActive = false;

        setupBoard(pawns);
        renderBoard();
        renderPawns(pawns);
        board.getStyleClass().add("board");

        return board;
    }

    /**
     * Sets up the initial positions of the pawns on the board.
     *
     * @param pawns List to populate with the initial pawns.
     */
    private void setupBoard(List<Pawn> pawns) {
        // Function to add pawns to the board
        BiConsumer<Integer, Boolean> addPawns = (startRow, isWhite) -> {
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
     * @param board    GridPane to render the board onto.
     */
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
     * @param board    GridPane representing the board.
     * @param pawns    List of pawns to render.
     * @param tileSize Size of each tile.
     */
    private void renderPawns(List<Pawn> pawns) {
        // For new pawns, create ImageViews and add them to the board
        for (Pawn pawn : pawns) {
            if (!pawnViews.containsKey(pawn)) {
                ImageView pawnView = createPawnImageView(pawn, 0.8);
                setupPawnInteractions(pawnView, pawn, pawns);
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
     * @param pawn       The pawn for which to create the ImageView.
     * @param tileSize   Size of each tile.
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
        pawnView.hoverProperty().addListener((observable, oldValue, newValue) -> {
            double endScale = newValue ? 1.1 : 1.0;

            ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(200), pawnView);
            scaleTransition.setToX(endScale);
            scaleTransition.setToY(endScale);
            scaleTransition.play();
        });

        return pawnView;
    }

    /**
     * Sets up the interactions for a pawn ImageView.
     *
     * @param pawnView The ImageView of the pawn.
     * @param pawn     The pawn object.
     * @param board    GridPane representing the board.
     * @param pawns    List of all pawns.
     * @param tileSize Size of each tile.
     */
    private void setupPawnInteractions(ImageView pawnView, Pawn pawn, List<Pawn> pawns) {
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
            seePossibleMove(pawns, focusedPawn);
            renderPawns(pawns);
        });
    }

    /**
     * Retrieves a pawn at a given position.
     *
     * @param pawns    List of all pawns.
     * @param position Position to check.
     * @return Pawn at the position, or null if none.
     */
    private Pawn getPawnAtPosition(List<Pawn> pawns, Vector2i position) {
        return pawns.stream()
                .filter(p -> p.getPosition().equals(position))
                .findFirst()
                .orElse(null);
    }

    /**
     * Highlights possible moves for the selected pawn.
     *
     * @param board    GridPane representing the board.
     * @param pawns    List of all pawns.
     * @param pawn     The selected pawn.
     * @param tileSize Size of each tile.
     */
    private void seePossibleMove(List<Pawn> pawns, Pawn pawn) {
        possibleMoves.clear();
        Vector2i position = pawn.getPosition();
        int x = position.x;
        int y = position.y;

        BiPredicate<Integer, Integer> inBounds = (newX, newY) ->
                newX >= 0 && newX < boardSize.x && newY >= 0 && newY < boardSize.y;

        List<CapturePath> allPaths = new ArrayList<>();

        captureCheck(pawns, pawn, inBounds, x, y, new CapturePath(), allPaths);

        if (!allPaths.isEmpty()) {
            handleCaptureMoves(pawns, pawn,allPaths);
        } else {
            handleNormalMoves(pawns, pawn, inBounds, x, y);
        }
    }

    /**
     * Handles capture moves by highlighting them and setting up interactions.
     *
     * @param board    GridPane representing the board.
     * @param pawns    List of all pawns.
     * @param pawn     The selected pawn.
     * @param tileSize Size of each tile.
     * @param allPaths List of all possible capture paths.
     */
    private void handleCaptureMoves(List<Pawn> pawns, Pawn pawn, List<CapturePath> allPaths) {
        int maxCaptures = allPaths.stream().mapToInt(CapturePath::getCaptureCount).max().orElse(0);

        List<CapturePath> maxCapturePaths = allPaths.stream()
                .filter(path -> path.getCaptureCount() == maxCaptures)
                .collect(Collectors.toList());

        for (CapturePath path : maxCapturePaths) {
            Vector2i landingPos = path.getLastPosition();

            Rectangle square = createHighlightSquare(Color.RED);
            board.add(square, landingPos.x, landingPos.y);
            possibleMoves.add(landingPos);

            square.setOnMouseClicked(event -> {
                if (isWhiteTurn != pawn.isWhite() || isAnimating) {
                    System.out.println("Not your turn!");
                    return;
                }
                soundPlayer.playMoveSound();

                // Clear highlights before starting the animation
                clearHighlights();

                // Animate pawn movement along the capture path
                animatePawnCaptureMovement(pawn,pawns, path, () -> {
                    promotePawnIfNeeded(pawn, path.getLastPosition());
                    switchTurn();
                    focusedPawn = null;

                    // Update scores
                    int captures = path.getCaptureCount();
                    if (pawn.isWhite()) {
                        gameInfo.scorePlayerOne.set(gameInfo.scorePlayerOne.get() + captures);
                    } else {
                        gameInfo.scorePlayerTwo.set(gameInfo.scorePlayerTwo.get() + captures);
                    }
                });
            });
        }
    }

    /**
     * Handles normal moves when no captures are available.
     *
     * @param board    GridPane representing the board.
     * @param pawns    List of all pawns.
     * @param pawn     The selected pawn.
     * @param tileSize Size of each tile.
     * @param inBounds Predicate to check if a position is within bounds.
     * @param x        Current x-coordinate of the pawn.
     * @param y        Current y-coordinate of the pawn.
     */
    private void handleNormalMoves(List<Pawn> pawns, Pawn pawn, 
                                   BiPredicate<Integer, Integer> inBounds, int x, int y) {
        BiConsumer<Integer, Integer> highlightMove = (newX, newY) -> {
            Rectangle square = createHighlightSquare(Color.GREEN);
            board.add(square, newX, newY);
            possibleMoves.add(new Vector2i(newX, newY));

            square.setOnMouseClicked(event -> {
                if (isWhiteTurn != pawn.isWhite()) {
                    System.out.println("Not your turn!");
                    return;
                }
                if (isAnimating) {
                    System.out.println("Please wait for the current move to finish.");
                    return;
                }
                soundPlayer.playMoveSound();
                Vector2i landingPos = new Vector2i(newX, newY);

                // Animate pawn movement
                animatePawnMovement(pawn, landingPos, () -> {
                    // Update pawn position after animation
                    pawn.setPosition(landingPos);
                    ImageView pawnView = pawnViews.get(pawn);
                    GridPane.setColumnIndex(pawnView, landingPos.x);
                    GridPane.setRowIndex(pawnView, landingPos.y);

                    promotePawnIfNeeded(pawn, landingPos);
                    clearHighlights();
                    switchTurn();
                    focusedPawn = null;
                });
            }); 
        };

        if (!pawn.isKing()) {
            int direction = pawn.isWhite() ? -1 : 1;
            int[][] moveDirections = {{-1, direction}, {1, direction}};

            for (int[] dir : moveDirections) {
                int newX = x + dir[0];
                int newY = y + dir[1];

                if (inBounds.test(newX, newY) && getPawnAtPosition(pawns, new Vector2i(newX, newY)) == null) {
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

                while (inBounds.test(newX, newY) && getPawnAtPosition(pawns, new Vector2i(newX, newY)) == null) {
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
     * @param pawns            List of all pawns.
     * @param pawn             The pawn to move.
     * @param inBounds         Predicate to check if a position is within bounds.
     * @param x                Current x-coordinate.
     * @param y                Current y-coordinate.
     * @param currentPath      The current capture path.
     * @param allPaths         List to collect all capture paths.
     */
    private void captureCheck(List<Pawn> pawns, Pawn pawn,
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

        int maxSteps = pawn.isKing() ? boardSize.x : 1;

        for (int i = 1; i <= maxSteps; i++) {
            int captureX = x + dx * i;
            int captureY = y + dy * i;

            // Check if capture position is within bounds
            if (!inBounds.test(captureX, captureY)) {
                break; // Stop if out of bounds
            }

            Vector2i capturePos = new Vector2i(captureX, captureY);
            Pawn capturedPawn = getPawnAtPosition(pawns, capturePos);

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
                    if (getPawnAtPosition(pawns, landingPos) == null || landingPos.equals(pawn.getPosition())) {
                        foundCapture = true;
                        CapturePath newPath = new CapturePath(currentPath);
                        newPath.addMove(landingPos, capturedPawn);

                        // Recursively check for further captures
                        captureCheck(pawns, pawn, inBounds, landingX, landingY, newPath, allPaths);
                    } else {
                        break; // Stop if the landing position is blocked
                    }
                }

                // **Stop if two pawns are adjacent**: Check for a pawn directly next to the first one
                int nextX = captureX + dx;
                int nextY = captureY + dy;
                if (inBounds.test(nextX, nextY) && getPawnAtPosition(pawns, new Vector2i(nextX, nextY)) != null) {
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
     * @param tileSize Size of each tile.
     * @param color    Color of the highlight.
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
     * @param pawn       The pawn to check for promotion.
     * @param landingPos The landing position of the pawn.
     */
    private void promotePawnIfNeeded(Pawn pawn, Vector2i landingPos) {
        if ((pawn.isWhite() && landingPos.y == 0) || (!pawn.isWhite() && landingPos.y == boardSize.y - 1)) {
            pawn.setKing(true);
        }
    }

    /**
     * Clears any highlights from the board.
     *
     * @param board    GridPane representing the board.
     * @param tileSize Size of each tile.
     */
    private void clearHighlights() {
        board.getChildren().removeAll(highlightNodes);
        highlightNodes.clear();
    }

    /**
     * Removes a captured pawn from the board and the list of pawns.
     *
     * @param board        GridPane representing the board.
     * @param pawns        List of all pawns.
     * @param capturedPawn The pawn to remove.
     */
    private void removePawn(List<Pawn> pawns, Pawn capturedPawn) {
        ImageView capturedPawnView = pawnViews.get(capturedPawn);

        // Create a FadeTransition to fade out the captured pawn
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(300), capturedPawnView);
        fadeTransition.setFromValue(1.0);
        fadeTransition.setToValue(0.0);

        // After the fade-out, remove the pawn from the board
        fadeTransition.setOnFinished(e -> {
            pawns.remove(capturedPawn);
            board.getChildren().remove(capturedPawnView);
            pawnViews.remove(capturedPawn);
        });

        // Play the animation
        fadeTransition.play();
    }


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

    public boolean isWhiteTurn() {
        return isWhiteTurn;
    }

    private void resetPawnsToInitialPositions() {
        // Re-add any missing pawns
        for (Pawn pawn : allPawns) { // allPawns is a list of all pawns at the start
            if (!pawnViews.containsKey(pawn)) {
                ImageView pawnView = createPawnImageView(pawn, 0.8);
                setupPawnInteractions(pawnView, pawn, allPawns);
                board.add(pawnView, pawn.getInitialPosition().x, pawn.getInitialPosition().y);
                GridPane.setHalignment(pawnView, HPos.CENTER);
                GridPane.setValignment(pawnView, VPos.CENTER);
                pawnViews.put(pawn, pawnView);
            }

            // Reset pawn properties
            pawn.setKing(false);
            pawn.setPosition(pawn.getInitialPosition());

            // Update the pawn's ImageView position
            ImageView pawnView = pawnViews.get(pawn);
            GridPane.setColumnIndex(pawnView, pawn.getPosition().x);
            GridPane.setRowIndex(pawnView, pawn.getPosition().y);
        }
    }

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
        transition.setOnFinished(e -> {
            // Reset translation
            pawnView.setTranslateX(0);
            pawnView.setTranslateY(0);

            // Update the pawn's position in the GridPane
            GridPane.setColumnIndex(pawnView, landingPos.x);
            GridPane.setRowIndex(pawnView, landingPos.y);

            // Optionally, bring the pawnView back to its original z-order
            // (Not necessary unless you have specific z-order requirements)

            isAnimating = false;

            // Call the onFinished callback
            onFinished.run();
        });

        // Play the animation
        transition.play();
    }

    private void animatePawnCaptureMovement(Pawn pawn, List<Pawn> pawns, CapturePath path, Runnable onFinished) {
        ImageView pawnView = pawnViews.get(pawn);
        List<Vector2i> positions = path.positions;
        List<Pawn> capturedPawns = path.capturedPawns;

        List<Animation> animations = new ArrayList<>();

        Vector2i currentPos = pawn.getPosition();
        for (int i = 0; i < positions.size(); i++) {
            Vector2i nextPos = positions.get(i);

            double deltaX = (nextPos.x - currentPos.x) * tileSize;
            double deltaY = (nextPos.y - currentPos.y) * tileSize;

            TranslateTransition transition = new TranslateTransition(Duration.millis(300), pawnView);
            transition.setByX(deltaX);
            transition.setByY(deltaY);
            transition.setInterpolator(Interpolator.EASE_BOTH);

            // Create a final index variable for use in the lambda
            int index = i;

            transition.setOnFinished(e -> {
                // Reset translation
                pawnView.setTranslateX(0);
                pawnView.setTranslateY(0);

                // Update the pawn's position in the GridPane
                GridPane.setColumnIndex(pawnView, nextPos.x);
                GridPane.setRowIndex(pawnView, nextPos.y);

                // Update the pawn's position
                pawn.setPosition(nextPos);

                // Remove captured pawn if any
                if (index < capturedPawns.size()) {
                    Pawn capturedPawn = capturedPawns.get(index);
                    removePawn(pawns, capturedPawn);
                }
            });

            animations.add(transition);

            currentPos = nextPos;
        }

        // Create a SequentialTransition
        SequentialTransition sequentialTransition = new SequentialTransition();
        sequentialTransition.getChildren().addAll(animations);

        sequentialTransition.setOnFinished(e -> {
            isAnimating = false;
            onFinished.run();
        });

        isAnimating = true;

        // Bring pawnView to front
        board.getChildren().remove(pawnView);
        board.getChildren().add(pawnView);

        sequentialTransition.play();
    }
}

/**
 * Represents a capture path in the game.
 */
class CapturePath {
    List<Vector2i> positions;       // Positions along the path
    List<Pawn> capturedPawns;       // Pawns captured along the path

    public CapturePath() {
        positions = new ArrayList<>();
        capturedPawns = new ArrayList<>();
    }

    public CapturePath(CapturePath other) {
        positions = new ArrayList<>(other.positions);
        capturedPawns = new ArrayList<>(other.capturedPawns);
    }

    public void addMove(Vector2i position, Pawn capturedPawn) {
        positions.add(position);
        if (capturedPawn != null) {
            capturedPawns.add(capturedPawn);
        }
    }

    public int getCaptureCount() {
        return capturedPawns.size();
    }

    public Vector2i getLastPosition() {
        return positions.get(positions.size() - 1);
    }
}
