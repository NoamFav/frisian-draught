package com.frisian_draught.board;

import com.frisian_draught.util.PawnImages;

import javafx.scene.image.Image;

import org.joml.Vector2i;

public class Pawn implements Cloneable {

    private Vector2i position;
    private Vector2i initialPosition;

    private boolean isKing;
    private boolean isWhite;
    private Image image;

    private int numberOfNonCapturingMoves = 0;

    private PawnImages pawnImages = PawnImages.getPawnImage();

    public Pawn(Vector2i position, boolean isWhite) {

        this(position, isWhite, false);
    }

    public Pawn(Vector2i position, boolean isWhite, boolean isKing) {

        this.position = position;
        this.initialPosition = new Vector2i(position);
        this.isWhite = isWhite;
        setKing(isKing);
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
        if (king) {
            this.image = isWhite ? pawnImages.whiteKing() : pawnImages.blackKing();
        } else {
            this.image = isWhite ? pawnImages.whitePawn() : pawnImages.blackPawn();
        }
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

    public int getNumberOfNonCapturingMoves() {
        return numberOfNonCapturingMoves;
    }

    public void incrementNumberOfNonCapturingMoves() {
        numberOfNonCapturingMoves++;
    }

    public void resetNumberOfNonCapturingMoves() {
        numberOfNonCapturingMoves = 0;
    }

    @Override
    public Pawn clone() {
        try {
            return (Pawn) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(); // Should never happen
        }
    }
}
