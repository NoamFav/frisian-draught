package com.um_project_game.board;

import javafx.animation.Interpolator;
import javafx.animation.ScaleTransition;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import org.joml.Vector2i;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class BoardRendered {

    private final BoardState boardState;
    private MoveManager moveManager;
    private final Map<Pawn, ScaleTransition> activeTransitions = new HashMap<>();

    public BoardRendered(BoardState boardState) {
        this.boardState = boardState;
    }

    public void setMoveManager(MoveManager moveManager) {
        this.moveManager = moveManager;
    }

    /**
     * Sets up the initial positions of the pawns on the board.
     *
     * @param pawns List to populate with the initial pawns.
     */
    public void setupBoard() {
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

    public void renderBoard() {
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
    public void renderPawns() {
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
    public ImageView createPawnImageView(Pawn pawn, double scaleFactor) {
        ImageView pawnView = new ImageView(pawn.getImage());

        pawnView.setFitWidth(boardState.getTileSize() * scaleFactor);
        pawnView.setFitHeight(boardState.getTileSize() * scaleFactor);
        pawnView.setPreserveRatio(true);
        pawnView.setUserData(pawn);

        pawnView.hoverProperty()
                .addListener(
                        (_, _, newValue) -> {
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
    public void setupPawnInteractions(ImageView pawnView, Pawn pawn) {
        pawnView.hoverProperty()
                .addListener(
                        (_, _, newValue) -> {
                            double scaleFactor = newValue ? 0.96 : 0.8; // Increase size on hover
                            pawnView.setFitWidth(boardState.getTileSize() * scaleFactor);
                            pawnView.setFitHeight(boardState.getTileSize() * scaleFactor);
                        });

        if (!boardState.isActive()) {
            return;
        }

        pawnView.setOnMouseClicked(
                _ -> {
                    clearHighlights();
                    boardState.setFocusedPawn(pawn);
                    moveManager.seePossibleMove(pawn);
                    renderPawns();
                });
    }

    /**
     * Creates a highlight square for possible moves.
     *
     * @param tileSize Size of each tile.
     * @param color Color of the highlight.
     * @return Rectangle representing the highlight.
     */
    public Rectangle createHighlightSquare(Color color) {
        Rectangle square = new Rectangle(boardState.getTileSize(), boardState.getTileSize());
        square.setFill(color);
        boardState.getHighlightNodes().add(square); // Keep track of highlights
        return square;
    }

    /**
     * Clears any highlights from the board.
     *
     * @param board GridPane representing the board.
     * @param tileSize Size of each tile.
     */
    public void clearHighlights() {
        boardState.getBoard().getChildren().removeAll(boardState.getHighlightNodes());
        boardState.getHighlightNodes().clear();
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

    public void highlightMovablePawns(List<Pawn> pawns) {

        for (ScaleTransition transition : activeTransitions.values()) {
            transition.stop();
        }
        activeTransitions.clear();

        // Clear existing highlights for all pawns
        for (Pawn pawn : boardState.getPawns()) {
            ImageView pawnView = boardState.getPawnViews().get(pawn);
            if (pawnView != null) {
                // Reset the pawn's scale
                pawnView.setScaleX(1.0);
                pawnView.setScaleY(1.0);
            }
        }

        for (Pawn pawn : pawns) {
            ImageView pawnView = boardState.getPawnViews().get(pawn);

            if (pawnView != null) {
                // Apply ScaleTransition for movable pawns
                ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(1000), pawnView);
                scaleTransition.setToX(1.2); // Slightly enlarge the pawn
                scaleTransition.setToY(1.2);
                scaleTransition.setInterpolator(Interpolator.EASE_BOTH);
                scaleTransition.setCycleCount(ScaleTransition.INDEFINITE);
                scaleTransition.setAutoReverse(true); // Make it pulse
                scaleTransition.play();

                activeTransitions.put(pawn, scaleTransition);
            } else if (pawnView != null) {
                // Reset the pawn's scale for non-movable pawns
                pawnView.setScaleX(1.0);
                pawnView.setScaleY(1.0);
            }
        }
    }

    private void applyHoverTransitionEffect(Pawn pawn) {
        ImageView pawnView = boardState.getPawnViews().get(pawn);

        if (pawnView != null) {
            pawnView.hoverProperty().addListener((observable, oldValue, isHovered) -> {
                if (isHovered) {
                    // Apply a scale effect on hover
                    ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(200), pawnView);
                    scaleTransition.setToX(1.1); // Scale up
                    scaleTransition.setToY(1.1);
                    scaleTransition.play();
                } else {
                    // Revert the scale effect when hover is removed
                    ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(200), pawnView);
                    scaleTransition.setToX(1.0); // Scale back to original size
                    scaleTransition.setToY(1.0);
                    scaleTransition.play();
                }
            });
        }
    }
}
