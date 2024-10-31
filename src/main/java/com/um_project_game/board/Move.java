package com.um_project_game.board;

import org.joml.Vector2i;

public class Move {
    private Vector2i startPosition;
    private Vector2i endPosition;

    public Move(Vector2i startPosition, Vector2i endPosition) {
        this.startPosition = startPosition;
        this.endPosition = endPosition;
    }

    public Vector2i getStartPosition() {
        return startPosition;
    }

    public Vector2i getEndPosition() {
        return endPosition;
    }
}
