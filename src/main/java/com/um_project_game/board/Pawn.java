package com.um_project_game.board;

import org.joml.Vector2i;

import javafx.scene.image.Image;

import com.um_project_game.util.PawnImages;

public class Pawn {

    private Vector2i position;
    private boolean isKing;
    private boolean isWhite;
    private Image image;

    private PawnImages pawnImages = PawnImages.getPawnImage();

    public Pawn(Vector2i position, boolean isWhite) {
        this.position = position;
        this.isKing = false;
        this.isWhite = isWhite;
        this.image = isWhite ? pawnImages.whitePawn() : pawnImages.blackPawn();
    }

    public Vector2i getPosition() {
        return position;
    }

    public void setPosition(Vector2i position) {
        this.position = position;
    }

    public boolean isKing() {
        image = isWhite ? pawnImages.whiteKing() : pawnImages.blackKing();
        return isKing;
    }

    public void setKing(boolean king) {
        isKing = king;
    }

    public boolean isWhite() {
        return isWhite;
    }

    public Image getImage() {
        return image;
    }
}
