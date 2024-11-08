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
            if (pawn.isWhite() == isWhiteTurn) {
                // TODO: Add logic to generate moves for each piece based on its type and position
            }
        }
        return moves;
    }

    public MoveResult applyMove(Vector2i startPosition, Vector2i endPosition) {
        // TODO: Implement logic to apply a move and return the resulting game state and reward
        return new MoveResult(this, 0, false); // Placeholder; update with actual logic
    }

    public MoveResult applyMove(Move move) {
        // TODO: Implement logic to apply a move and return the resulting game state and reward
        return applyMove(move.getStartPosition(), move.getEndPosition());
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
