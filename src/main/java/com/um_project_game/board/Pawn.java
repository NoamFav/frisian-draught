package com.um_project_game.board;

import org.joml.Vector2i;

import javafx.scene.image.Image;

import com.um_project_game.util.PawnImages;

public class Pawn {

    private Vector2i position;
    private Vector2i initialPosition;

	private boolean isKing;
    private boolean isWhite;
    private Image image;

    private PawnImages pawnImages = PawnImages.getPawnImage();

    public Pawn(Vector2i position, boolean isWhite) {
        this.position = position;
        this.initialPosition = new Vector2i(position);
        this.isKing = false;
        this.isWhite = isWhite;
        this.image = isWhite ? pawnImages.whitePawn() : pawnImages.blackPawn();
    }

    public Image onHover() {
        if (isKing) {
            return isWhite ? pawnImages.whiteKingHover() : pawnImages.blackKingHover();
        } else {
            return isWhite ? pawnImages.whitePawnHover() : pawnImages.blackPawnHover();
        }
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
        image = isWhite ? pawnImages.whiteKing() : pawnImages.blackKing();
        isKing = king;
    }

    public boolean isWhite() {
        return isWhite;
    }

    public Image getImage() {
        return image;
    }

    public Vector2i getInitialPosition() {
		return initialPosition;
	}
}
