package com.um_project_game.board;

import java.util.Map;
import java.util.HashMap;
import java.util.Objects;

import org.joml.Vector2i;

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
}

