package com.um_project_game.board;

import org.joml.Vector2i;

import javafx.scene.image.Image;

public class Pawn {

    private Vector2i position;
    private boolean isKing;
    private Image image;

    public Pawn(Vector2i position) {
        this.position = position;
        this.isKing = false;
        this.image = new Image(getClass().getResourceAsStream("/pawn.png"));
    }

    public Vector2i getPosition() {
        return position;
    }

    public void setPosition(Vector2i position) {
        this.position = position;
    }

    public boolean isKing() {
        return isKing;
    }

    public void setKing(boolean king) {
        isKing = king;
    }
}
