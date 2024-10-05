package com.um_project_game.board;

import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a capture path in the game.
 */
class CapturePath {
    List<Vector2i> positions;       // Positions along the path
    List<Pawn> capturedPawns;       // Pawns captured along the path

    public CapturePath() {
        positions = new ArrayList<>();
        capturedPawns = new ArrayList<>();
    }

    public CapturePath(CapturePath other) {
        positions = new ArrayList<>(other.positions);
        capturedPawns = new ArrayList<>(other.capturedPawns);
    }

    public void addMove(Vector2i position, Pawn capturedPawn) {
        positions.add(position);
        if (capturedPawn != null) {
            capturedPawns.add(capturedPawn);
        }
    }

    public int getCaptureCount() {
        return capturedPawns.size();
    }

    public Vector2i getLastPosition() {
        return positions.get(positions.size() - 1);
    }
}
