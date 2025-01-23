package com.frisian_draught;

import org.joml.Vector2i;
import org.junit.jupiter.api.*;
import com.frisian_draught.board.Move;
import java.util.Collections;
import static org.junit.jupiter.api.Assertions.*;


/**
 * Test class for the Move class focusing exclusively on non-capturing move logic.
 */
public class PieceMovementTest {

    

    @Test
    @DisplayName("Test non-capturing move creation and properties")
    public void testNonCapturingMove() {
        // Arrange
        Vector2i start = new Vector2i(0, 0); // A1
        Vector2i end = new Vector2i(1, 1);   // B2

        // Act
        Move move = new Move(start, end);

        // Assert
        assertEquals(start, move.getStartPosition(), "Start position should match the initial position");
        assertEquals(end, move.getEndPosition(), "End position should match the target position");
        assertTrue(move.getCapturedPositions().isEmpty(), "Captured positions should be empty for non-capturing move");
        assertFalse(move.isCapture(), "isCapture should return false for non-capturing move");
    }


    @Test
    @DisplayName("Test that captured positions are empty and immutable for non-capturing moves")
    public void testCapturedPositionsImmutabilityNonCapturing() {
        // Arrange
        Vector2i start = new Vector2i(4, 4); // E5
        Vector2i end = new Vector2i(5, 5);   // F6

        Move move = new Move(start, end);

        // Act & Attempted Modification
        // Since capturedPositions should be empty, attempting to modify should have no effect
        assertThrows(UnsupportedOperationException.class, () -> {
            move.getCapturedPositions().add(new Vector2i(6, 6));
        }, "Attempting to modify capturedPositions should throw UnsupportedOperationException");
    }

    @Test
    @DisplayName("Test Move constructor with empty captured positions list")
    public void testMoveConstructorWithEmptyCapturedPositions() {
        // Arrange
        Vector2i start = new Vector2i(1, 1); // B2
        Vector2i end = new Vector2i(2, 2);   // C3

        // Act
        Move move = new Move(start, end, Collections.emptyList());

        // Assert
        assertFalse(move.isCapture(), "isCapture should return false when capturedPositions is empty");
        assertTrue(move.getCapturedPositions().isEmpty(), "Captured positions should be empty");
    }

    @Test
    @DisplayName("Test getter methods for non-capturing move")
    public void testGetterMethodsNonCapturing() {
        // Arrange
        Vector2i start = new Vector2i(3, 3); // D4
        Vector2i end = new Vector2i(4, 4);   // E5

        Move move = new Move(start, end);

        // Act
        Vector2i retrievedStart = move.getStartPosition();
        Vector2i retrievedEnd = move.getEndPosition();
        var retrievedCaptured = move.getCapturedPositions();

        // Assert
        assertEquals(start, retrievedStart, "getStartPosition should return the correct start position");
        assertEquals(end, retrievedEnd, "getEndPosition should return the correct end position");
        assertTrue(retrievedCaptured.isEmpty(), "getCapturedPositions should return an empty list for non-capturing move");
    }

    
}
