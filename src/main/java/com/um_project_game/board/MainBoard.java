package com.um_project_game.board;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

import org.apache.logging.log4j.util.TriConsumer;
import org.joml.Vector2i;

import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
* main_board
*/
public class MainBoard {

    private Vector2i boardSize = new Vector2i(10,10);
    private Pawn focusedPawn;
    private List<Vector2i> possibleMoves = new ArrayList<>();

    /**
    * Create the main board and render it to the root
    *
    * @param root root of the window
    * @param boardSize how big the board should be counting all the tiles
    * @param boardPosition where the board should be placed (x, y)
    */
    public GridPane getMainBoard(Pane root, float boardSize, Vector2i boardPosition) {
        float tileSize = boardSize / 10;
        List<Pawn> pawns = new ArrayList<>();

        GridPane board = new GridPane();

        board.setLayoutX(boardPosition.x);
        board.setLayoutY(boardPosition.y);
        setupBoard(pawns);
        renderBoard(root, tileSize, board);
        renderPawn(board, pawns, tileSize);
        return board;
    }

      /**
       * made for non unique boards, aka VBox and other
     * @param root root of the window
     * @return a random board
     */
    public GridPane getRandomBoard(Pane root, float boardSize) {
        float tileSize = boardSize / 10;
        List<Pawn> pawns = new ArrayList<>();
        GridPane board = new GridPane();
        setupBoard(pawns);
        renderBoard(root, tileSize, board);
        renderPawn(board, pawns, tileSize);
        board.getStyleClass().add("board");
        return board;

      }

      /**
     * setup the board with pawns
     */
    public void setupBoard(List<Pawn> pawns) {
          
          // White pawns
          for (int i = 0; i < 4; i++) {
              for (int j = 0; j < boardSize.x; j++) {
                  if ((i + j) % 2 == 1) {
                      pawns.add(new Pawn(new Vector2i(j, i), false));
                  }
              }
          }

          // Black pawns
          for (int i = 6; i < 10; i++) {
              for (int j = 0; j < boardSize.x; j++) {
                  if ((i + j) % 2 == 1) {
                      pawns.add(new Pawn(new Vector2i(j, i), true));
                  }
              }
          }
    }
      /**
     * @param root root of the window
     * @param tileSize size of each tile
     * @param board the board to render
     * @return a gridpane as the board
     */
    public GridPane renderBoard(Pane root, float tileSize, GridPane board) {
          for (int i = 0; i < boardSize.x; i++) {
              for (int j = 0; j < boardSize.y; j++) {
                  Rectangle square = new Rectangle(tileSize, tileSize);

                  if ((i + j) % 2 == 0) {
                      square.setFill(Color.WHITE);
                  } else {
                      square.setFill(Color.BLACK);
                  }
                  board.add(square, i, j);
              }
          }
          return board;
      }

    public void renderPawn(GridPane board, List<Pawn> pawns, float tileSize) {
    // Define a scaling factor (e.g., 0.8 means the pawn will be 80% of the tile size)
    double scaleFactor = 0.8;

    // Iterate through your pawn objects
    for (Pawn pawn : pawns) {
        // Load the appropriate image for each pawn
        ImageView pawnView = new ImageView(pawn.getImage());
        pawnView.hoverProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                // Change the image on hover
                pawnView.setImage(pawn.onHover());

                // Increase the size on hover
                pawnView.setFitWidth(tileSize * scaleFactor * 1.2);
                pawnView.setFitHeight(tileSize * scaleFactor * 1.2);
            } else {
                // Revert to the original image when hover ends
                pawnView.setImage(pawn.getImage());

                // Revert to the original size
                pawnView.setFitWidth(tileSize * scaleFactor);
                pawnView.setFitHeight(tileSize * scaleFactor);
            }
        });
        pawnView.setOnMouseClicked(event -> {
            // Clear previous highlights before showing new possible moves
            clearHighlights(board, tileSize);

            // Set the selected pawn
            focusedPawn = pawn;

            // Move the pawn to a new position and show possible moves
            seePossibleMove(board, pawns, focusedPawn, tileSize);

            // Re-render the board
            renderPawn(board, pawns, tileSize);
        });

        // Set the pawn size to be smaller than the tile size
        pawnView.setFitWidth(tileSize * scaleFactor);
        pawnView.setFitHeight(tileSize * scaleFactor);

        // Preserve the aspect ratio
        pawnView.setPreserveRatio(true);

        // Add the pawn image to the GridPane at the pawn's position
        board.add(pawnView, pawn.getPosition().x, pawn.getPosition().y);

        // Center the pawn within the tile
        GridPane.setHalignment(pawnView, HPos.CENTER);
        GridPane.setValignment(pawnView, VPos.CENTER);
        }
    }
   public Pawn getPawnAtPosition(List<Pawn> pawns, Vector2i position) {
        for (Pawn pawn : pawns) {
            if (pawn.getPosition().equals(position)) {
                return pawn;
            }
        }
        return null;
    }

    public void seePossibleMove(GridPane board, List<Pawn> pawns, Pawn pawn, float tileSize) {
        possibleMoves.clear();
        Vector2i position = pawn.getPosition();
        int x = position.x;
        int y = position.y;
        int direction = pawn.isWhite() ? -1 : 1;

        // Lambda to check if a position is within bounds
        BiPredicate<Integer, Integer> inBounds = (newX, newY) -> newX >= 0 && newX < boardSize.x && newY >= 0 && newY < boardSize.y;

        // Lambda to handle highlighting and adding click handler to the tile
        BiConsumer<Integer, Integer> highlightMove = (newX, newY) -> {
            Rectangle square = new Rectangle(tileSize, tileSize);
            square.setFill(Color.GREEN);
            board.add(square, newX, newY);
            possibleMoves.add(new Vector2i(newX, newY));
            
            // Pass `null` for captured pawn in a regular move
            movePawnOnClick(square, board, pawns, pawn, tileSize, null);
        };
        // Lambda to check capturing logic
        TriConsumer<Integer, Integer, Integer> checkCapture = (dx, dy, distance) -> {
            int newX = x + dx * distance;
            int newY = y + dy * distance;

            // Check if the move is within bounds
            if (inBounds.test(newX, newY)) {
                Vector2i capturePos = new Vector2i(x + dx, y + dy);
                Pawn capturedPawn = getPawnAtPosition(pawns, capturePos);
                if (capturedPawn != null && capturedPawn.isWhite() != pawn.isWhite()) {
                    Vector2i jumpPos = new Vector2i(newX, newY);
                    if (getPawnAtPosition(pawns, jumpPos) == null) {
                        // Capture move is possible
                        Rectangle square = new Rectangle(tileSize, tileSize);
                        square.setFill(Color.GREEN);
                        board.add(square, newX, newY);
                        possibleMoves.add(jumpPos);

                        // Pass the captured pawn to movePawnOnClick
                        movePawnOnClick(square, board, pawns, pawn, tileSize, capturedPawn);
                    }
                }
            }
        };

        // Regular pawn moves (one diagonal step)
        if (!pawn.isKing()) {
            int[] moveDeltas = {-1, 1};

            for (int dx : moveDeltas) {
                for (int dy : moveDeltas) {
                    int newX = x + dx;
                    int newY = y + direction;
                    if (inBounds.test(newX, newY) && getPawnAtPosition(pawns, new Vector2i(newX, newY)) == null) {
                        // Normal move
                        highlightMove.accept(newX, newY);
                    }
                }
            }

            // Check capturing moves for normal pawns
            for (int dx : moveDeltas) {
                for (int dy : moveDeltas) {
                    checkCapture.accept(dx, dy, 2); // Check two squares for capturing
                }
            }
        } else {
            // King moves (can move multiple squares in any diagonal direction)
            int[] moveDeltas = {-1, 1};

            for (int dx : moveDeltas) {
                for (int dy : moveDeltas) {
                    // Check all possible positions along the diagonal
                    for (int i = 1; i < boardSize.x; i++) {
                        int newX = x + dx * i;
                        int newY = y + dy * i;
                        if (!inBounds.test(newX, newY)) {
                            break; // Out of bounds, stop
                        }

                        // Check if the position is empty
                        if (getPawnAtPosition(pawns, new Vector2i(newX, newY)) == null) {
                            highlightMove.accept(newX, newY); // Valid move
                        } else {
                            break; // Stop further moves if a pawn is blocking
                        }
                    }

                    // Check for captures for king
                    for (int i = 1; i < boardSize.x - 1; i++) {
                        checkCapture.accept(dx, dy, i + 2); // Check multiple squares for capturing
                    }
                }
            }
        }
    }

    public void clearHighlights(GridPane board, float tileSize) {
        for (int i = 0; i < boardSize.x; i++) {
            for (int j = 0; j < boardSize.y; j++) {
                Rectangle square = new Rectangle(tileSize, tileSize);
                if ((i + j) % 2 == 0) {
                    square.setFill(Color.WHITE);
                } else {
                    square.setFill(Color.BLACK);
                }
                board.add(square, i, j);
            }
        }
    }

    public void removePawn(GridPane board, List<Pawn> pawns, Pawn capturedPawn) {
        // Remove the captured pawn from the list
        pawns.remove(capturedPawn);

        // Remove the captured pawn from the board visually
        for (var node : board.getChildren()) {
            if (node instanceof ImageView) {
                ImageView imageView = (ImageView) node;
                if (GridPane.getColumnIndex(imageView) == capturedPawn.getPosition().x
                        && GridPane.getRowIndex(imageView) == capturedPawn.getPosition().y) {
                    board.getChildren().remove(imageView); // Remove the pawn image from the board
                    break;
                }
            }
        }
    }

    private void movePawnOnClick(Rectangle square, GridPane board, List<Pawn> pawns, Pawn pawn, float tileSize, Pawn capturedPawn) {
        square.setOnMouseClicked(event -> {
            Vector2i position = new Vector2i(GridPane.getColumnIndex(square), GridPane.getRowIndex(square));
            if (possibleMoves.contains(position)) {
                // Move the selected pawn
                pawn.setPosition(position);
                clearHighlights(board, tileSize);
                
                // If a capture occurred, remove the captured pawn
                if (capturedPawn != null) {
                    removePawn(board, pawns, capturedPawn);
                }
                
                renderPawn(board, pawns, tileSize);
            }
        });
    }
}
