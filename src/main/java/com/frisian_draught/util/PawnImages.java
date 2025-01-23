package com.frisian_draught.util;

import javafx.scene.image.Image;

import java.util.Objects;

public record PawnImages(
        Image whitePawn,
        Image blackPawn,
        Image whiteKing,
        Image blackKing,
        Image whitePawnHover,
        Image blackPawnHover,
        Image whiteKingHover,
        Image blackKingHover) {

    private static Image cachedWhitePawn;
    private static Image cachedBlackPawn;
    private static Image cachedWhiteKing;
    private static Image cachedBlackKing;
    private static Image cachedWhitePawnHover;
    private static Image cachedBlackPawnHover;
    private static Image cachedWhiteKingHover;
    private static Image cachedBlackKingHover;

    public static PawnImages getPawnImage() {
        if (cachedWhitePawn == null) {
            cachedWhitePawn =
                    new Image(
                            Objects.requireNonNull(
                                    PawnImages.class.getResourceAsStream("/pawns/white_pawn.png")));
            cachedBlackPawn =
                    new Image(
                            Objects.requireNonNull(
                                    PawnImages.class.getResourceAsStream("/pawns/black_pawn.png")));
            cachedWhiteKing =
                    new Image(
                            Objects.requireNonNull(
                                    PawnImages.class.getResourceAsStream("/pawns/white_king.png")));
            cachedBlackKing =
                    new Image(
                            Objects.requireNonNull(
                                    PawnImages.class.getResourceAsStream("/pawns/black_king.png")));
            cachedWhitePawnHover =
                    new Image(
                            Objects.requireNonNull(
                                    PawnImages.class.getResourceAsStream(
                                            "/pawns/white_focused_pawn.png")));
            cachedBlackPawnHover =
                    new Image(
                            Objects.requireNonNull(
                                    PawnImages.class.getResourceAsStream(
                                            "/pawns/black_focused_pawn.png")));
            cachedWhiteKingHover =
                    new Image(
                            Objects.requireNonNull(
                                    PawnImages.class.getResourceAsStream(
                                            "/pawns/white_focused_king.png")));
            cachedBlackKingHover =
                    new Image(
                            Objects.requireNonNull(
                                    PawnImages.class.getResourceAsStream(
                                            "/pawns/black_focused_king.png")));
        }
        return new PawnImages(
                cachedWhitePawn,
                cachedBlackPawn,
                cachedWhiteKing,
                cachedBlackKing,
                cachedWhitePawnHover,
                cachedBlackPawnHover,
                cachedWhiteKingHover,
                cachedBlackKingHover);
    }
}
