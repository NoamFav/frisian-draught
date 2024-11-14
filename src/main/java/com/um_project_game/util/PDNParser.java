package com.um_project_game.util;

import com.um_project_game.board.Move;
import org.joml.Vector2i;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PDNParser {

    private String event;
    private String site;
    private String date;
    private String round;
    private String blackPlayer;
    private String whitePlayer;
    private String result;
    private List<Move> moves;

    private String filePath;

    /**
     * @param filePath
     */
    public PDNParser(String filePath) {
        // Initialize fields
        moves = new ArrayList<>();
        this.filePath = filePath;
    }

    public void parseFile() {
        Map<String, String> pdnData = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("[")) {
                    parseMetadata(line, pdnData);
                } else if (!line.trim().isEmpty()) {
                    parseMoves(line);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }

        // Set metadata after parsing
        this.event = pdnData.getOrDefault("Event", "");
        this.site = pdnData.getOrDefault("Site", "");
        this.date = pdnData.getOrDefault("Date", "");
        this.round = pdnData.getOrDefault("Round", "");
        this.whitePlayer = pdnData.getOrDefault("White", "");
        this.blackPlayer = pdnData.getOrDefault("Black", "");
        this.result = pdnData.getOrDefault("Result", "");
    }

    private void parseMetadata(String line, Map<String, String> pdnData) {
        int keyStart = line.indexOf('[') + 1;
        int keyEnd = line.indexOf(' ');
        int valueStart = line.indexOf("\"") + 1;
        int valueEnd = line.lastIndexOf("\"");

        String key = line.substring(keyStart, keyEnd);
        String value = line.substring(valueStart, valueEnd);
        pdnData.put(key, value);
    }

    private void parseMoves(String line) {
        String[] turnMoves = line.split("\\s+");
        for (String turn : turnMoves) {
            // For now: Skip known game result lines explicitly
            //TODO: Add handling for when you load in a game that is already finished
            if ("1-0".equals(turn) || "0-1".equals(turn) || "1/2-1/2".equals(turn)) {
                continue;
            }

            if (turn.contains(".")) {
                // Skip move number
                continue;
            }

            // Determine if the move is a capture
            boolean isCapture = turn.contains("x");

            // Extract positions based on capture or non-capture
            String[] positions = isCapture ? turn.split("x") : turn.split("-");

            Vector2i startPosition = null;
            Vector2i endPosition = null;

            try {
                if (positions.length > 0) {
                    startPosition = TileConversion.getTileVector(Integer.parseInt(positions[0]));
                }
                if (positions.length > 1) {
                    endPosition = TileConversion.getTileVector(Integer.parseInt(positions[1]));
                }

                List<Vector2i> capturedPositions = new ArrayList<>();
                if (isCapture && endPosition != null) {
                    capturedPositions.add(endPosition); // Replace with actual capture logic as needed
                }

                Move move = new Move(startPosition, endPosition, capturedPositions);
                moves.add(move);

            } catch (NumberFormatException e) {
                System.err.println("Skipping invalid move format: " + turn);
                e.printStackTrace();
            }
        }

        // Print all moves after parsing
        for (Move move : moves) {
            System.out.println(move);
        }
    }


    public String getEvent() {
        return event;
    }

    public String getSite() {
        return site;
    }

    public String getDate() {
        return date;
    }

    public String getRound() {
        return round;
    }

    public String getBlackPlayer() {
        return blackPlayer;
    }

    public String getWhitePlayer() {
        return whitePlayer;
    }

    public String getResult() {
        return result;
    }

    public List<Move> getMoves() {
        return moves;
    }
}
