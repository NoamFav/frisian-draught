package com.frisian_draught.board;

import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.List;

/** Represents a capture path in the game. */
public class CapturePath {
    public List<Vector2i> positions; // Positions along the path
    public List<Pawn> capturedPawns; // Pawns captured along the path
    public Pawn initialPawn; // Initial pawn that made the capture
    double score;

    public CapturePath() {
        positions = new ArrayList<>();
        capturedPawns = new ArrayList<>();
        score = 0;
    }

    public CapturePath(CapturePath other) {
        positions = new ArrayList<>(other.positions);
        capturedPawns = new ArrayList<>(other.capturedPawns);
        score = other.score;
    }

    public void addMove(Vector2i position, Pawn capturedPawn) {
        positions.add(position);
        if (capturedPawn != null) {
            capturedPawns.add(capturedPawn);

            // Update score
            if (capturedPawn.isKing()) {
                score += 1.5;
            } else {
                score += 1;
            }
        }
    }

    public int getCaptureCount() {
        return capturedPawns.size();
    }

    public double getCaptureValue() {
        return score;
    }

    public Vector2i getLastPosition() {
        return positions.get(positions.size() - 1);
    }
}
