package com.um_project_game.board;

import javafx.scene.layout.GridPane; // Ensure this is imported
import javafx.scene.text.Text;

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
            // Add header row
            Text turnHeader = new Text("Turn");
            Text whiteHeader = new Text("White");
            Text blackHeader = new Text("Black");

            turnHeader.getStyleClass().add("label");
            whiteHeader.getStyleClass().add("label");
            blackHeader.getStyleClass().add("label");

            movesListGridPane.add(turnHeader, 0, 0); // Column 0, Row 0
            movesListGridPane.add(whiteHeader, 1, 0); // Column 1, Row 0
            movesListGridPane.add(blackHeader, 2, 0); // Column 2, Row 0

            int row = 1; // Start with row 1 for moves, since row 0 is the header

            for (int i = 0; i < moves.size(); i++) {
                Move move = moves.get(i);
                Text moveText = new Text(move.toString()); // Create Text for the move
                moveText.getStyleClass().add("label");

                // Determine column based on whether it's an odd/even index
                int column = (i % 2) + 1; // 1 for Player 1, 2 for Player 2

                // Add the move text to the corresponding column
                movesListGridPane.add(moveText, column, row); // Add Text to GridPane

                // Calculate turn number (1-based index)
                int turnNumber = (i / 2) + 1; // Increment turn number for each pair of moves

                // Add turn number to the first column if it's the first move of a turn
                if (column == 1) {
                    Text turnText =
                            new Text(String.valueOf(turnNumber)); // Create Text for turn number
                    turnText.getStyleClass().add("label");
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
