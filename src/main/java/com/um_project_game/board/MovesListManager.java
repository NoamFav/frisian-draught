package com.um_project_game.board;

import javafx.scene.text.Text;
import javafx.scene.layout.GridPane; // Ensure this is imported
import java.util.List;

public class MovesListManager {
    private GridPane movesListGridPane; // Use GridPane for three columns

    public MovesListManager(GridPane movesListGridPane) {
        this.movesListGridPane = movesListGridPane;
    }

    public void setMovesListGridPane(GridPane movesListGridPane) {
        this.movesListGridPane = movesListGridPane;
    }

    public void updateMovesListUI(List<Move> moves) {
        if (movesListGridPane != null) {
            movesListGridPane.getChildren().clear(); // Clear previous moves
            int row = 0; // Initialize row index

            for (int i = 0; i < moves.size(); i++) {
                Move move = moves.get(i);
                Text moveText = new Text(move.toString()); // Create Text for the move

                // Determine column based on whether it's an odd/even index
                int column = (i % 2) + 1; // 1 for Player 1, 2 for Player 2

                // Add the move text to the corresponding column
                movesListGridPane.add(moveText, column, row); // Add Text to GridPane

                // Calculate turn number (1-based index)
                int turnNumber = (i / 2) + 1; // Increment turn number for each pair of moves

                // Add turn number to the first column if it's the first move of a turn
                if (column == 1) {
                    Text turnText = new Text(String.valueOf(turnNumber)); // Create Text for turn number
                    movesListGridPane.add(turnText, 0, row); // Add turn number to first column
                }

                // Move to the next row after every two moves
                if (column == 2) { // After adding Player 2's move
                    row++; // Increment the row index
                }
            }
        }
    }

    public void clearMovesList() {
        if (movesListGridPane != null) {
            movesListGridPane.getChildren().clear();
        }
    }

    public GridPane getMovesListGridPane() {
        return movesListGridPane;
    }
}
