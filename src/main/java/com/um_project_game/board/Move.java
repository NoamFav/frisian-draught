package com.um_project_game.board;

import com.um_project_game.util.TileConversion;

import org.joml.Vector2i;

import java.util.Collections;
import java.util.List;

public class Move {

    private Vector2i startPosition;
    private Vector2i endPosition;
    private List<Vector2i> capturedPositions;

    // Primary constructor with capturing positions
    public Move(Vector2i startPosition, Vector2i endPosition, List<Vector2i> capturedPositions) {
        this.startPosition = startPosition;
        this.endPosition = endPosition;
        this.capturedPositions = capturedPositions;
    }

    // Secondary constructor for non-capturing moves
    public Move(Vector2i startPosition, Vector2i endPosition) {
        this(startPosition, endPosition, Collections.emptyList());
    }

    // Getter for the start position as a Vector2i
    public Vector2i getStartPosition() {
        return startPosition;
    }

    // Getter for the end position as a Vector2i
    public Vector2i getEndPosition() {
        return endPosition;
    }

    // Getter for captured positions
    public List<Vector2i> getCapturedPositions() {
        return capturedPositions;
    }

    // Checks if the move is a capturing move
    public boolean isCapture() {
        return !capturedPositions.isEmpty();
    }

    // PGN Notation used for Export/Import and Visualization in MovesList
    @Override
    public String toString() {

        return TileConversion.getTileNotation(startPosition)
                + (capturedPositions.isEmpty() ? "-" : "x")
                + TileConversion.getTileNotation(endPosition);
    }
}
