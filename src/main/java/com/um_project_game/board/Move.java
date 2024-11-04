package com.um_project_game.board;

import com.um_project_game.util.TileConversion;
import org.joml.Vector2i;

import java.util.Collections;
import java.util.List;


public record Move(int initial, int next, List<Vector2i> capturedPositions) {

    // Primary constructor with all fields
    public Move(Vector2i initialPosition, Vector2i nextPosition, List<Vector2i> capturedPositions) {
        this(TileConversion.getTileNotation(initialPosition), TileConversion.getTileNotation(nextPosition), capturedPositions);
    }

    // Private constructor to handle non-capturing moves
    public Move(Vector2i initialPosition, Vector2i nextPosition) {
        this(TileConversion.getTileNotation(initialPosition), TileConversion.getTileNotation(nextPosition), Collections.emptyList());
    }


    // Getter for the initial position as a Vector2i
    public Vector2i getInitialPosition() {
        return TileConversion.getTileVector(initial);
    }

    // Getter for the next position as a Vector2i
    public Vector2i getFinalPosition() {
        return TileConversion.getTileVector(next);
    }

    // Getter for the captured position (returns null for non-capturing moves)
    public List<Vector2i> getCapturedPositions() {
        return capturedPositions;
    }

    @Override
    public String toString() {
        return initial + "-" + next;
    }

    public boolean isCapture() {
        return capturedPositions.size() == 0;
    }
}
