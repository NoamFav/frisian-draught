package com.um_project_game.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javafx.scene.image.Image;

public class PawnImages {
    private static final Map<String, Image> imageCache = new HashMap<>();

    // Define keys for each image
    private static final String WHITE_PAWN_KEY = "/pawns/white_pawn.png";
    private static final String BLACK_PAWN_KEY = "/pawns/black_pawn.png";
    private static final String WHITE_KING_KEY = "/pawns/white_king.png";
    private static final String BLACK_KING_KEY = "/pawns/black_king.png";
    private static final String WHITE_PAWN_HOVER_KEY = "/pawns/white_focused_pawn.png";
    private static final String BLACK_PAWN_HOVER_KEY = "/pawns/black_focused_pawn.png";
    private static final String WHITE_KING_HOVER_KEY = "/pawns/white_focused_king.png";
    private static final String BLACK_KING_HOVER_KEY = "/pawns/black_focused_king.png";

    // Instance variables for the images
    private final Image whitePawn;
    private final Image blackPawn;
    private final Image whiteKing;
    private final Image blackKing;
    private final Image whitePawnHover;
    private final Image blackPawnHover;
    private final Image whiteKingHover;
    private final Image blackKingHover;

    // Constructor accepting 8 Image parameters
    public PawnImages(Image whitePawn, Image blackPawn, Image whiteKing, Image blackKing,
                      Image whitePawnHover, Image blackPawnHover, Image whiteKingHover, Image blackKingHover) {
        this.whitePawn = whitePawn;
        this.blackPawn = blackPawn;
        this.whiteKing = whiteKing;
        this.blackKing = blackKing;
        this.whitePawnHover = whitePawnHover;
        this.blackPawnHover = blackPawnHover;
        this.whiteKingHover = whiteKingHover;
        this.blackKingHover = blackKingHover;
    }

    public static PawnImages getPawnImages() {  // Correct method name
        return new PawnImages(
                getImage(WHITE_PAWN_KEY),
                getImage(BLACK_PAWN_KEY),
                getImage(WHITE_KING_KEY),
                getImage(BLACK_KING_KEY),
                getImage(WHITE_PAWN_HOVER_KEY),
                getImage(BLACK_PAWN_HOVER_KEY),
                getImage(WHITE_KING_HOVER_KEY),
                getImage(BLACK_KING_HOVER_KEY)
        );
    }

    private static Image getImage(String path) {
        // Return the cached image or load it if not already cached
        return imageCache.computeIfAbsent(path, key -> {
            System.out.println("Loading image: " + key);
            return new Image(Objects.requireNonNull(PawnImages.class.getResourceAsStream(key)));
        });
    }

    // Getters for the images
    public Image whitePawn() {
        return whitePawn;
    }

    public Image blackPawn() {
        return blackPawn;
    }

    public Image whiteKing() {
        return whiteKing;
    }

    public Image blackKing() {
        return blackKing;
    }

    public Image whitePawnHover() {
        return whitePawnHover;
    }

    public Image blackPawnHover() {
        return blackPawnHover;
    }

    public Image whiteKingHover() {
        return whiteKingHover;
    }

    public Image blackKingHover() {
        return blackKingHover;
    }
}
