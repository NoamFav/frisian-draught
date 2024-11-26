package com.um_project_game.util;

import com.um_project_game.board.Move;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class GameExporter {

    // Define a platform-independent directory for exports
    private static final Path EXPORT_DIRECTORY =
            Paths.get(System.getProperty("user.home"), "FrisianDraughtsExports");

    public GameExporter() {
        // Default constructor
    }

    public void exportGameToPDN(List<Move> moves, String result) {
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

        // PDNWriter instance
        PDNWriter pdnWriter =
                new PDNWriter(
                        PDNConstraints.DEFAULT_EVENT,
                        PDNConstraints.DEFAULT_SITE,
                        String.valueOf(moves.size() / 2 + 1),
                        LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd")),
                        PDNConstraints.DEFAULT_WHITE_PLAYER,
                        PDNConstraints.DEFAULT_BLACK_PLAYER,
                        result,
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
}
