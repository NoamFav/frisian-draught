package com.um_project_game.util;

import com.um_project_game.board.Move;

import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class GameExporter {

    public GameExporter() {
    }

    public void exportGameToPDN(List<Move> moves, String result) {

        StringBuilder movesStringBuilder = new StringBuilder();

        // Build moves string in PDN format with turn numbers
        for (int i = 0; i < moves.size(); i += 2) {
            int turnNumber = (i / 2) + 1;
            movesStringBuilder.append(turnNumber).append(". ");

            // White's move
            movesStringBuilder.append(moves.get(i).toString());

            // Black's move
            if (i + 1 < moves.size()) {
                movesStringBuilder.append(" ").append(moves.get(i + 1).toString());
            }

            movesStringBuilder.append(" ");
        }

        String movesString = movesStringBuilder.toString().trim();

        // Create an instance of PDNWriter
        PDNWriter pdnWriter = new PDNWriter(
                PDNConstraints.DEFAULT_EVENT,
                PDNConstraints.DEFAULT_SITE,
                String.valueOf(moves.size()/2+1),
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd")),
                PDNConstraints.DEFAULT_WHITE_PLAYER,
                PDNConstraints.DEFAULT_BLACK_PLAYER,
                result,
                movesString
        );

        // Generate the PDN file
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filePath = Paths.get("exports", timestamp + ".pdn").toString();
        pdnWriter.generatePDNFile(filePath);
    }

}
