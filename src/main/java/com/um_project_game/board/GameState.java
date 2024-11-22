package com.um_project_game.board;

import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class GameState {
    Map<Vector2i, Pawn> boardState; // Tracks positions of pawns
    boolean isWhiteTurn;

    public GameState(Map<Vector2i, Pawn> currentState, boolean isWhiteTurn) {
        this.boardState = new HashMap<>(currentState);
        this.isWhiteTurn = isWhiteTurn;
    }

    public double[] toInputArray() {
        int boardSize = 10; // Assuming a 10x10 board
        int inputSize = boardSize * boardSize + 1; // Each tile + 1 for the current turn
        double[] inputArray = new double[inputSize];

        // Encode the board state
        for (Map.Entry<Vector2i, Pawn> entry : boardState.entrySet()) {
            Vector2i position = entry.getKey();
            Pawn pawn = entry.getValue();

            int index = position.y * boardSize + position.x; // 1D index for the board
            if (pawn.isWhite()) {
                inputArray[index] = 1.0; // White pawn
            } else {
                inputArray[index] = -1.0; // Black pawn
            }
        }

        // Encode the current turn (1.0 for white's turn, 0.0 for black's turn)
        inputArray[inputSize - 1] = isWhiteTurn ? 1.0 : 0.0;

        return inputArray;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GameState other = (GameState) o;
        return isWhiteTurn == other.isWhiteTurn && boardState.equals(other.boardState);
    }

    @Override
    public int hashCode() {
        return Objects.hash(boardState, isWhiteTurn);
    }

    public Map<Vector2i, Pawn> getBoardState() {
        return boardState;
    }

    public boolean isWhiteTurn() {
        return isWhiteTurn;
    }

    public List<Move> generateMoves() {
        List<Move> moves = new ArrayList<>();

        for (Map.Entry<Vector2i, Pawn> entry : boardState.entrySet()) {
            Vector2i position = entry.getKey();
            Pawn pawn = entry.getValue();

            if (pawn.isWhite() != isWhiteTurn) continue; // Skip opponent pawns

            // Add normal moves
            int[][] directions =
                    pawn.isKing()
                            ? new int[][] {
                                {-1, -1}, {1, -1}, {-1, 1}, {1, 1}
                            } // Kings move in all directions
                            : isWhiteTurn
                                    ? new int[][] {{-1, -1}, {1, -1}} // White pawns move upward
                                    : new int[][] {{-1, 1}, {1, 1}}; // Black pawns move downward

            for (int[] dir : directions) {
                Vector2i newPos = new Vector2i(position.x + dir[0], position.y + dir[1]);
                if (isValidMove(position, newPos)) {
                    moves.add(new Move(position, newPos));
                }
            }

            // Add capturing moves
            for (int[] dir : directions) {
                Vector2i capturePos = new Vector2i(position.x + dir[0], position.y + dir[1]);
                Vector2i landingPos = new Vector2i(capturePos.x + dir[0], capturePos.y + dir[1]);

                if (isValidCapture(position, capturePos, landingPos)) {
                    moves.add(new Move(position, landingPos, List.of(capturePos)));
                }
            }
        }

        return moves;
    }

    private boolean isValidMove(Vector2i startPosition, Vector2i endPosition) {
        // Ensure the end position is within board boundaries
        if (endPosition.x < 0 || endPosition.x >= 10 || endPosition.y < 0 || endPosition.y >= 10) {
            return false;
        }

        // Ensure the end position is not occupied
        if (boardState.containsKey(endPosition)) {
            return false;
        }

        // Ensure the move direction is correct for non-king pawns
        Pawn pawn = boardState.get(startPosition);
        if (pawn != null && !pawn.isKing()) {
            int direction = pawn.isWhite() ? -1 : 1; // White moves up, black moves down
            if (endPosition.y - startPosition.y != direction) {
                return false;
            }
        }

        // If all checks pass, it's a valid move
        return true;
    }

    private boolean isValidCapture(
            Vector2i startPosition, Vector2i capturePosition, Vector2i landingPosition) {
        // Ensure the landing position is within board boundaries
        if (landingPosition.x < 0
                || landingPosition.x >= 10
                || landingPosition.y < 0
                || landingPosition.y >= 10) {
            return false;
        }

        // Ensure the capture position is within board boundaries
        if (capturePosition.x < 0
                || capturePosition.x >= 10
                || capturePosition.y < 0
                || capturePosition.y >= 10) {
            return false;
        }

        // Ensure there's an opponent pawn at the capture position
        Pawn capturedPawn = boardState.get(capturePosition);
        Pawn movingPawn = boardState.get(startPosition);
        if (capturedPawn == null
                || movingPawn == null
                || capturedPawn.isWhite() == movingPawn.isWhite()) {
            return false;
        }

        // Ensure the landing position is empty
        if (boardState.containsKey(landingPosition)) {
            return false;
        }

        // If all checks pass, it's a valid capture
        return true;
    }

    public MoveResult applyMove(Move move) {
        Vector2i startPosition = move.getStartPosition();
        Vector2i endPosition = move.getEndPosition();
        List<Vector2i> capturedPositions = move.getCapturedPositions();

        // Check if there's a pawn at the start position
        Pawn movingPawn = boardState.get(startPosition);
        if (movingPawn == null) {
            throw new IllegalArgumentException("No pawn at the starting position!");
        }

        // Update the state
        boardState.remove(startPosition); // Remove the pawn from the old position
        boardState.put(endPosition, movingPawn); // Move the pawn to the new position

        // Process captured pawns
        double reward = 0.0;
        for (Vector2i capturedPosition : capturedPositions) {
            if (boardState.containsKey(capturedPosition)) {
                boardState.remove(capturedPosition);
                reward += 1.0; // Reward for each captured pawn
            }
        }

        // Handle promotion
        if ((movingPawn.isWhite() && endPosition.y == 0)
                || (!movingPawn.isWhite() && endPosition.y == 9)) {
            movingPawn.setKing(true);
        }

        // Create the updated game state
        GameState newState = new GameState(boardState, !isWhiteTurn);

        // Return the result
        return new MoveResult(newState, reward, newState.isTerminal());
    }

    public boolean isTerminal() {
        if (generateMoves().isEmpty()) {
            return true;
        }

        boolean whitePawnsExist = boardState.values().stream().anyMatch(pawn -> pawn.isWhite());
        boolean blackPawnsExist = boardState.values().stream().anyMatch(pawn -> !pawn.isWhite());

        return !whitePawnsExist || !blackPawnsExist;
    }
}
