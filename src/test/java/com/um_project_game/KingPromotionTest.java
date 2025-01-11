package com.um_project_game;

import org.joml.Vector2i;
import org.junit.jupiter.api.Test;
import com.um_project_game.board.Pawn;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for the Pawn class focusing on king promotion logic.
 */
public class KingPromotionTest {


    @Test
    public void testWhitePawnPromotion() {
        // Arrange
        Vector2i initialPosition = new Vector2i(5, 8); // Starting near promotion row for white
        Pawn whitePawn = new Pawn(initialPosition, true);

        // Act & Assert
        assertFalse(whitePawn.isKing(), "Pawn should not be a king initially");

        // Move pawn to promotion row (y = 9 for white)
        Vector2i promotionPosition = new Vector2i(5, 9);
        whitePawn.setPosition(promotionPosition);

        // Simulate promotion logic
        if (shouldPromote(whitePawn)) {
            whitePawn.setKing(true);
        }

        // Assert
        assertTrue(whitePawn.isKing(), "Pawn should be promoted to king upon reaching promotion row");
        assertEquals(promotionPosition, whitePawn.getPosition(), "Pawn should be at the promotion position");
    }

    @Test
    public void testBlackPawnPromotion() {
        // Arrange
        Vector2i initialPosition = new Vector2i(4, 1); // Starting near promotion row for black
        Pawn blackPawn = new Pawn(initialPosition, false);

        // Act & Assert
        assertFalse(blackPawn.isKing(), "Pawn should not be a king initially");

        // Move pawn to promotion row (y = 0 for black)
        Vector2i promotionPosition = new Vector2i(4, 0);
        blackPawn.setPosition(promotionPosition);

        // Simulate promotion logic
        if (shouldPromote(blackPawn)) {
            blackPawn.setKing(true);
        }

        // Assert
        assertTrue(blackPawn.isKing(), "Pawn should be promoted to king upon reaching promotion row");
        assertEquals(promotionPosition, blackPawn.getPosition(), "Pawn should be at the promotion position");
    }

    @Test
    public void testPawnDoesNotPromoteBeforePromotionRow() {
        // Test for white pawn
        Vector2i initialPositionWhite = new Vector2i(5, 7);
        Pawn whitePawn = new Pawn(initialPositionWhite, true);

        whitePawn.setPosition(new Vector2i(5, 8)); // Not yet at promotion row

        // Simulate promotion logic
        if (shouldPromote(whitePawn)) {
            whitePawn.setKing(true);
        }

        assertFalse(whitePawn.isKing(), "Pawn should not be promoted to king before reaching promotion row");

        // Test for black pawn
        Vector2i initialPositionBlack = new Vector2i(4, 2);
        Pawn blackPawn = new Pawn(initialPositionBlack, false);

        blackPawn.setPosition(new Vector2i(4, 1)); // Not yet at promotion row

        // Simulate promotion logic
        if (shouldPromote(blackPawn)) {
            blackPawn.setKing(true);
        }

        assertFalse(blackPawn.isKing(), "Pawn should not be promoted to king before reaching promotion row");
    }

    /**
     * Helper method to determine if a pawn should be promoted based on its position.
     * This mimics the promotion logic that would exist in the game controller or board logic.
     *
     * @param pawn The pawn to check for promotion.
     * @return true if the pawn is on the promotion row, false otherwise.
     */
    private boolean shouldPromote(Pawn pawn) {
        if (pawn.isWhite()) {
            return pawn.getPosition().y == 9;
        } else {
            return pawn.getPosition().y == 0;
        }
    }
}
