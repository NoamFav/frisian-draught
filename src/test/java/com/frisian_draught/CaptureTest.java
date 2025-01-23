package com.frisian_draught;

import org.joml.Vector2i;
import org.junit.jupiter.api.Test;
import com.frisian_draught.board.Move;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CaptureTest {

    @Test
    public void testCapturingMoveInitialization() {
        // Arrange
        Vector2i start = new Vector2i(2, 3); // For example, position C4
        Vector2i end = new Vector2i(4, 5);   // For example, position E6
        List<Vector2i> captured = Arrays.asList(
                new Vector2i(3, 4), // Position D5
                new Vector2i(3, 5)  // Position D6 (if multiple captures are possible)
        );

        // Act
        Move capturingMove = new Move(start, end, captured);

        // Assert
        assertEquals(start, capturingMove.getStartPosition(), "Start position should be initialized correctly.");
        assertEquals(end, capturingMove.getEndPosition(), "End position should be initialized correctly.");
        assertEquals(captured, capturingMove.getCapturedPositions(), "Captured positions should be initialized correctly.");
        assertTrue(capturingMove.isCapture(), "isCapture() should return true for capturing moves.");
    }

    @Test
    public void testSingleCaptureMove() {
        // Arrange
        Vector2i start = new Vector2i(1, 1); // Position B2
        Vector2i end = new Vector2i(2, 2);   // Position C3
        List<Vector2i> captured = Arrays.asList(
                new Vector2i(1, 2) // Position B3
        );

        // Act
        Move capturingMove = new Move(start, end, captured);

        // Assert
        assertTrue(capturingMove.isCapture(), "Move should be identified as a capturing move.");
        assertEquals(1, capturingMove.getCapturedPositions().size(), "There should be exactly one captured position.");
        assertEquals(captured, capturingMove.getCapturedPositions(), "Captured positions should match the input.");
    }

    @Test
    public void testMultipleCapturesMove() {
        // Arrange
        Vector2i start = new Vector2i(0, 0); // Position A1
        Vector2i end = new Vector2i(4, 4);   // Position E5
        List<Vector2i> captured = Arrays.asList(
                new Vector2i(1, 1), // Position B2
                new Vector2i(2, 2), // Position C3
                new Vector2i(3, 3)  // Position D4
        );

        // Act
        Move capturingMove = new Move(start, end, captured);

        // Assert
        assertTrue(capturingMove.isCapture(), "Move should be identified as a capturing move.");
        assertEquals(3, capturingMove.getCapturedPositions().size(), "There should be three captured positions.");
        assertEquals(captured, capturingMove.getCapturedPositions(), "Captured positions should match the input.");
    }

    @Test
    public void testNonCapturingMoveIsNotCapture() {
        // Arrange
        Vector2i start = new Vector2i(0, 0); // Position A1
        Vector2i end = new Vector2i(1, 1);   // Position B2

        Move nonCapturingMove = new Move(start, end);

        // Assert
        assertFalse(nonCapturingMove.isCapture(), "Non-capturing move should not be identified as a capturing move.");
        assertTrue(nonCapturingMove.getCapturedPositions().isEmpty(), "Captured positions should be empty for non-capturing move.");
    }

    @Test
    public void testImmutabilityOfCapturedPositions() {
        // Arrange
        Vector2i start = new Vector2i(1, 1);
        Vector2i end = new Vector2i(2, 2);
        List<Vector2i> captured = Arrays.asList(new Vector2i(1, 2), new Vector2i(2, 1));

        Move move = new Move(start, end, captured);

        // Act
        // Attempt to modify the capturedPositions list
        Exception exception = assertThrows(UnsupportedOperationException.class, () -> {
            move.getCapturedPositions().add(new Vector2i(3, 3));
        }, "Captured positions list should be immutable.");

        // Assert
        // Exception is already asserted above
    }
}
