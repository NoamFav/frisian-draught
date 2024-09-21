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

      private GridPane board = new GridPane();

      public MainBoard(Pane root, float boardSize, Vector2i boardPosition) {

          float tileSize = boardSize / 10;

          board.setLayoutX(boardPosition.x);
          board.setLayoutY(boardPosition.y);
          setupBoard();
          renderBoard(root, tileSize);
      }

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

      public void renderBoard(Pane root, float tileSize) {
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
          root.getChildren().add(board);
      }
  }
