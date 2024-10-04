package com.um_project_game.board;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.joml.Vector2i;

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

    private static final int BOARD_SIZE = 10;
    private Vector2i boardSize = new Vector2i(BOARD_SIZE, BOARD_SIZE);
    private Pawn focusedPawn;
    private List<Vector2i> possibleMoves = new ArrayList<>();
    private boolean isWhiteTurn = true; // White starts first
    private List<Vector2i> listMoves = new ArrayList<>();
    private boolean isActive = true;
    private Pane root;

    /**
     * Creates and returns the main board, rendering it to the root pane.
     *
     * @param root          Root pane of the window.
     * @param boardPixelSize Size of the board in pixels.
     * @param boardPosition Position where the board should be placed (x, y).
     * @return GridPane representing the board.
     */
    public GridPane getMainBoard(Pane root, float boardPixelSize, Vector2i boardPosition) {
        float tileSize = boardPixelSize / BOARD_SIZE;
        List<Pawn> pawns = new ArrayList<>();
        GridPane board = new GridPane();
        isWhiteTurn = true;
        this.root = root;

        board.setLayoutX(boardPosition.x);
        board.setLayoutY(boardPosition.y);

        setupBoard(pawns);
        renderBoard(tileSize, board);
        renderPawns(board, pawns, tileSize);

        return board;
    }

    public void resetGame(GridPane board, float boardPixelSize) {
        float tileSize = boardPixelSize / BOARD_SIZE;
        List<Pawn> pawns = new ArrayList<>();
        isWhiteTurn = true;
        board.getChildren().clear();
        renderBoard(tileSize, board);
        setupBoard(pawns);
        renderPawns(board, pawns, tileSize);
    }

    /**
     * Creates and returns a random board, suitable for non-unique layouts.
     *
     * @param root          Root pane of the window.
     * @param boardPixelSize Size of the board in pixels.
     * @return GridPane representing a random board.
     */
    public GridPane getRandomBoard(Pane root, float boardPixelSize) {
        float tileSize = boardPixelSize / BOARD_SIZE;
        List<Pawn> pawns = new ArrayList<>();
        GridPane board = new GridPane();

        isActive = false;

        setupBoard(pawns);
        renderBoard(tileSize, board);
        renderPawns(board, pawns, tileSize);
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
    }

    /**
     * Renders the board grid.
     *
     * @param tileSize Size of each tile.
     * @param board    GridPane to render the board onto.
     */
    private void renderBoard(float tileSize, GridPane board) {
        BiConsumer<Integer, Integer> renderTile = (x, y) -> {
            Rectangle square = new Rectangle(tileSize, tileSize);
            square.setFill((x + y) % 2 == 0 ? Color.WHITE : Color.BLACK);
            board.add(square, x, y);
        };

        for (int y = 0; y < boardSize.y; y++) {
            for (int x = 0; x < boardSize.x; x++) {
                renderTile.accept(x, y);
            }
        }
    }

    /**
     * Renders the pawns onto the board.
     *
     * @param board    GridPane representing the board.
     * @param pawns    List of pawns to render.
     * @param tileSize Size of each tile.
     */
    private void renderPawns(GridPane board, List<Pawn> pawns, float tileSize) {
        double scaleFactor = 0.8;
        
        pawns.forEach(pawn -> {
            ImageView pawnView = createPawnImageView(pawn, tileSize, scaleFactor);
            setupPawnInteractions(pawnView, pawn, board, pawns, tileSize);
            board.add(pawnView, pawn.getPosition().x, pawn.getPosition().y);
            GridPane.setHalignment(pawnView, HPos.CENTER);
            GridPane.setValignment(pawnView, VPos.CENTER);
        });
    }

    /**
     * Creates an ImageView for a pawn.
     *
     * @param pawn       The pawn for which to create the ImageView.
     * @param tileSize   Size of each tile.
     * @param scaleFactor Scaling factor for the pawn image.
     * @return ImageView representing the pawn.
     */
    private ImageView createPawnImageView(Pawn pawn, float tileSize, double scaleFactor) {
        ImageView pawnView = new ImageView(pawn.getImage());

        pawnView.setFitWidth(tileSize * scaleFactor);
        pawnView.setFitHeight(tileSize * scaleFactor);
        pawnView.setPreserveRatio(true);

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
    private void setupPawnInteractions(ImageView pawnView, Pawn pawn, GridPane board, List<Pawn> pawns, float tileSize) {
        pawnView.hoverProperty().addListener((observable, oldValue, newValue) -> {
            double scaleFactor = newValue ? 0.96 : 0.8; // Increase size on hover
            pawnView.setFitWidth(tileSize * scaleFactor);
            pawnView.setFitHeight(tileSize * scaleFactor);
        });

        if (!isActive) {
            return;
        }

        pawnView.setOnMouseClicked(event -> {
            clearHighlights(board, tileSize);
            focusedPawn = pawn;
            seePossibleMove(board, pawns, focusedPawn, tileSize);
            renderPawns(board, pawns, tileSize);
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
    private void seePossibleMove(GridPane board, List<Pawn> pawns, Pawn pawn, float tileSize) {
        possibleMoves.clear();
        Vector2i position = pawn.getPosition();
        int x = position.x;
        int y = position.y;

        BiPredicate<Integer, Integer> inBounds = (newX, newY) ->
                newX >= 0 && newX < boardSize.x && newY >= 0 && newY < boardSize.y;

        List<CapturePath> allPaths = new ArrayList<>();

        captureCheck(pawns, pawn, inBounds, x, y, new CapturePath(), allPaths);

        if (!allPaths.isEmpty()) {
            handleCaptureMoves(board, pawns, pawn, tileSize, allPaths);
        } else {
            handleNormalMoves(board, pawns, pawn, tileSize, inBounds, x, y);
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
    private void handleCaptureMoves(GridPane board, List<Pawn> pawns, Pawn pawn, float tileSize, List<CapturePath> allPaths) {
        int maxCaptures = allPaths.stream().mapToInt(CapturePath::getCaptureCount).max().orElse(0);

        List<CapturePath> maxCapturePaths = allPaths.stream()
                .filter(path -> path.getCaptureCount() == maxCaptures)
                .collect(Collectors.toList());

        BiConsumer<CapturePath, Vector2i> highlightCaptureMove = (path, landingPos) -> {
            Rectangle square = createHighlightSquare(tileSize, Color.RED);
            board.add(square, landingPos.x, landingPos.y);
            possibleMoves.add(landingPos);

            square.setOnMouseClicked(event -> {
                if (isWhiteTurn != pawn.isWhite()) {
                    System.out.println("Not your turn!");
                    return;
                }
                pawn.setPosition(landingPos);
                path.capturedPawns.forEach(capturedPawn -> removePawn(board, pawns, capturedPawn));
                promotePawnIfNeeded(pawn, landingPos);
                clearHighlights(board, tileSize);
                renderPawns(board, pawns, tileSize);
                switchTurn();
                System.out.println("Is white turn: " + isWhiteTurn);
                focusedPawn = null;
            });
        }; 

        maxCapturePaths.forEach(path -> {
            Vector2i landingPos = path.getLastPosition();
            highlightCaptureMove.accept(path, landingPos);
        });
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
    private void handleNormalMoves(GridPane board, List<Pawn> pawns, Pawn pawn, float tileSize,
                                   BiPredicate<Integer, Integer> inBounds, int x, int y) {
        BiConsumer<Integer, Integer> highlightMove = (newX, newY) -> {
            Rectangle square = createHighlightSquare(tileSize, Color.GREEN);
            board.add(square, newX, newY);
            possibleMoves.add(new Vector2i(newX, newY));

            square.setOnMouseClicked(event -> {
                if (isWhiteTurn != pawn.isWhite()) {
                    System.out.println("Not your turn!");
                    return;
                }
                pawn.setPosition(new Vector2i(newX, newY));
                clearHighlights(board, tileSize);
                promotePawnIfNeeded(pawn, new Vector2i(newX, newY));
                renderPawns(board, pawns, tileSize);
                switchTurn();
                System.out.println("Is white turn: " + isWhiteTurn);
                focusedPawn = null;
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
                    if (getPawnAtPosition(pawns, landingPos) == null) {
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
    private Rectangle createHighlightSquare(float tileSize, Color color) {
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
    private void clearHighlights(GridPane board, float tileSize) {
        board.getChildren().removeIf(node -> node instanceof Rectangle && !(node instanceof ImageView));
        renderBoard(tileSize, board);
    }

    /**
     * Removes a captured pawn from the board and the list of pawns.
     *
     * @param board        GridPane representing the board.
     * @param pawns        List of all pawns.
     * @param capturedPawn The pawn to remove.
     */
    private void removePawn(GridPane board, List<Pawn> pawns, Pawn capturedPawn) {
        pawns.remove(capturedPawn);
        board.getChildren().removeIf(node -> {
            if (node instanceof ImageView) {
                ImageView imageView = (ImageView) node;
                Integer colIndex = GridPane.getColumnIndex(imageView);
                Integer rowIndex = GridPane.getRowIndex(imageView);
                return colIndex != null && rowIndex != null &&
                        colIndex == capturedPawn.getPosition().x &&
                        rowIndex == capturedPawn.getPosition().y;
            }
            return false;
        });
    }

    public void switchTurn() {
        isWhiteTurn = !isWhiteTurn;
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
