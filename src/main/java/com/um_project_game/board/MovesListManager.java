package com.um_project_game.board;

import javafx.scene.text.Text;
import javafx.scene.layout.VBox;

import java.util.List;

public class MovesListManager {
    private VBox movesListVBox;

    public MovesListManager(VBox movesListVBox) {
        this.movesListVBox = movesListVBox;
    }

    public void setMovesListVBox(VBox movesListVBox) {
        this.movesListVBox = movesListVBox;
    }

    public void updateMovesListUI(List<Move> moves) {
        if (movesListVBox != null) {
            movesListVBox.getChildren().clear();
            StringBuilder formattedMoves = new StringBuilder();
            int count = 0;

            for (Move move : moves) {
                if (count > 0) {
                    formattedMoves.append(", ");
                }
                formattedMoves.append(move.toString());
                count++;

                // Add a newline after every five moves
                if (count % 5 == 0) {
                    formattedMoves.append("\n");
                }
            }

            // Create a single Text element for the formatted moves
            Text movesText = new Text(formattedMoves.toString());
            movesListVBox.getChildren().add(movesText);
        }
    }

    public void clearMovesList() {
        if (movesListVBox != null) {
            movesListVBox.getChildren().clear();
        }
    }

    public VBox getMovesListVBox() {
        return movesListVBox;
    }
}
