package com.um_project_game.util;

import java.util.Objects;

import javafx.scene.image.Image;

public record PawnImages(Image whitePawn, Image blackPawn, Image whiteKing, Image blackKing, Image whitePawnHover, Image blackPawnHover) {

    public static PawnImages getPawnImage() {
        Image whitePawn = new Image(Objects.requireNonNull(PawnImages.class.getResourceAsStream("/pawns/white_pawn.png")));
        Image blackPawn = new Image(Objects.requireNonNull(PawnImages.class.getResourceAsStream("/pawns/black_pawn.png")));
        Image whiteKing = new Image(Objects.requireNonNull(PawnImages.class.getResourceAsStream("/pawns/white_king.png")));
        Image blackKing = new Image(Objects.requireNonNull(PawnImages.class.getResourceAsStream("/pawns/black_king.png")));
        Image whitePawnHover = new Image(Objects.requireNonNull(PawnImages.class.getResourceAsStream("/pawns/white_focused_pawn.png")));
        Image blackPawnHover = new Image(Objects.requireNonNull(PawnImages.class.getResourceAsStream("/pawns/black_focused_pawn.png")));

        return new PawnImages(whitePawn, blackPawn, whiteKing, blackKing, whitePawnHover, blackPawnHover);
    }
}
