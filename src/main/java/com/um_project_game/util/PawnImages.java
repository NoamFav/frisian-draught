package com.um_project_game.util;

import java.util.Objects;

import javafx.scene.image.Image;

public record PawnImages(Image whitePawn, Image blackPawn, Image whiteKing, Image blackKing) {

    public static PawnImages getPawnImage() {
        Image whitePawn = new Image(Objects.requireNonNull(PawnImages.class.getResourceAsStream("/pawns/white_pawn.svg")));
        Image blackPawn = new Image(Objects.requireNonNull(PawnImages.class.getResourceAsStream("/pawns/black_pawn.svg")));
        Image whiteKing = new Image(Objects.requireNonNull(PawnImages.class.getResourceAsStream("/pawns/white_king.svg")));
        Image blackKing = new Image(Objects.requireNonNull(PawnImages.class.getResourceAsStream("/pawns/black_king.svg")));

        return new PawnImages(whitePawn, blackPawn, whiteKing, blackKing);
    }
}
