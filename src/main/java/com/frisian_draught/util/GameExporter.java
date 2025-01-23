package com.frisian_draught.util;

import com.frisian_draught.board.Move;
import com.frisian_draught.board.Pawn;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class GameExporter {

    // Define a platform-independent directory for exports
    private static final Path EXPORT_DIRECTORY =
            Paths.get(
                    System.getProperty("user.home"), ".frisian-draught", "FrisianDraughtsExports");

    public GameExporter() {
        // Default constructor
    }

    public void exportGameToPDN(
            List<Pawn> pawns,
            List<Move> moves,
            String result,
            String isBot,
            String isMultiplayer,
            String turn) {

        // Build moves string in PDN format with turn numbers
        StringBuilder movesStringBuilder = new StringBuilder();
        for (int i = 0; i < moves.size(); i += 2) {
            int turnNumber = (i / 2) + 1;
            movesStringBuilder.append(turnNumber).append(". ");
            movesStringBuilder.append(moves.get(i).toString());
            if (i + 1 < moves.size()) {
                movesStringBuilder.append(" ").append(moves.get(i + 1).toString());
            }
            movesStringBuilder.append(" ");
        }
        String movesString = movesStringBuilder.toString().trim();

        // Build the BoardPosition string
        String boardPosition = generateBoardPositionTag(pawns);

        // PDNWriter instance
        PDNWriter pdnWriter =
                new PDNWriter(
                        PDNConstraints.DEFAULT_EVENT,
                        PDNConstraints.DEFAULT_SITE,
                        String.valueOf(moves.size() / 2 + 1),
                        LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd")),
                        PDNConstraints.DEFAULT_WHITE_PLAYER,
                        PDNConstraints.DEFAULT_BLACK_PLAYER,
                        turn,
                        isBot,
                        isMultiplayer,
                        result,
                        boardPosition,
                        movesString);

        // File path for the export
        String timestamp =
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        Path exportPath = EXPORT_DIRECTORY.resolve(timestamp + ".pdn");

        try {
            // Ensure the export directory exists
            Files.createDirectories(EXPORT_DIRECTORY);

            // Open an OutputStream and pass it to the PDNWriter
            try (FileOutputStream outputStream = new FileOutputStream(exportPath.toFile())) {
                pdnWriter.generatePDNFile(outputStream);
            }

            System.out.println("Game exported successfully to: " + exportPath.toAbsolutePath());
        } catch (IOException e) {
            System.err.println("Error exporting game to PDN: " + e.getMessage());
        }
    }

    private String generateBoardPositionTag(List<Pawn> pawns) {
        // Separate pawns into white and black groups
        List<String> whitePawns =
                pawns.stream()
                        .filter(Pawn::isWhite)
                        .map(
                                pawn ->
                                        Integer.toString(
                                                TileConversion.getTileNotation(
                                                        pawn.getPosition()))) // Convert to String
                        .collect(Collectors.toList());

        List<String> blackPawns =
                pawns.stream()
                        .filter(pawn -> !pawn.isWhite())
                        .map(
                                pawn ->
                                        Integer.toString(
                                                TileConversion.getTileNotation(
                                                        pawn.getPosition()))) // Convert to String
                        .collect(Collectors.toList());

        // Construct the BoardPosition tag explicitly
        String whitePart = "W:" + String.join(",", whitePawns);
        String blackPart = "B:" + String.join(",", blackPawns);

        return String.format("%s:%s", whitePart, blackPart);
    }
}
