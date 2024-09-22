 package com.um_project_game.board;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector2i;

import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
   * main_board
   */
  public class MainBoard {

      private Vector2i boardSize = new Vector2i(10,10);

      private List<Pawn> pawns = new ArrayList<>();

      /**
       * Create the main board and render it to the root
       *
     * @param root root of the window
     * @param boardSize how big the board should be counting all the tiles
     * @param boardPosition where the board should be placed (x, y)
     */
    public GridPane getMainBoard(Pane root, float boardSize, Vector2i boardPosition) {
        float tileSize = boardSize / 10;

        GridPane board = new GridPane();

        board.setLayoutX(boardPosition.x);
        board.setLayoutY(boardPosition.y);
        setupBoard();
        renderBoard(root, tileSize, board);
        return board;
    }

      /**
       * made for non unique boards, aka VBox and other
     * @param root root of the window
     * @return a random board
     */
    public GridPane getRandomBoard(Pane root, float boardSize) {
        float tileSize = boardSize / 10;
        GridPane board = new GridPane();
        renderBoard(root, tileSize, board);
        return board;

      }

      /**
     * setup the board with pawns
     */
    public void setupBoard() {
          
          // White pawns
          for (int i = 0; i < 4; i++) {
              for (int j = 0; j < 10; j++) {
                  if ((i + j) % 2 == 0) {
                      pawns.add(new Pawn(new Vector2i(i, j), true));
                  }
              }
          }

          // Black pawns
          for (int i = 6; i < 10; i++) {
              for (int j = 0; j < 10; j++) {
                  if ((i + j) % 2 == 0) {
                      pawns.add(new Pawn(new Vector2i(i, j), false));
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
  }
