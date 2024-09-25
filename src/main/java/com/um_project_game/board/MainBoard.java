 package com.um_project_game.board;

import java.util.ArrayList;
import java.util.List;

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

      /**
       * Create the main board and render it to the root
       *
     * @param root root of the window
     * @param boardSize how big the board should be counting all the tiles
     * @param boardPosition where the board should be placed (x, y)
     */
    public GridPane getMainBoard(Pane root, float boardSize, Vector2i boardPosition) {
        float tileSize = boardSize / 9;
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
              for (int j = 0; j < 10; j++) {
                  if ((i + j) % 2 == 1) {
                      pawns.add(new Pawn(new Vector2i(j, i), false));
                  }
              }
          }

          // Black pawns
          for (int i = 6; i < 10; i++) {
              for (int j = 0; j < 10; j++) {
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
          for (int i = 0; i < 10; i++) {
              for (int j = 0; j < 10; j++) {
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

   public void movePawn(Vector2i newPosition, List<Pawn> pawns) {
        // Check if there is a pawn at the new position
       Pawn pawn = getPawnAtPosition(pawns, newPosition);
        if (pawn != null) {
            // Remove the pawn from the list
            pawns.remove(pawn);
        }
        pawn.setPosition(newPosition);
    }
}
