package com.frisian_draught.util;

import com.frisian_draught.board.Move;
import com.frisian_draught.board.Pawn;

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
    private String turn;
    private String result;
    private String isBot;
    private String isMultiplayer;
    private List<Move> moves;

    private List<Pawn> pawns;

    private String filePath;

    public PDNParser(String filePath) {
        // Initialize fields
        moves = new ArrayList<>();
        pawns = new ArrayList<>();

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
        this.turn = pdnData.getOrDefault("Turn", "");
        this.result = pdnData.getOrDefault("Result", "");
        this.isBot = pdnData.getOrDefault("isBot", "");
        this.isMultiplayer = pdnData.getOrDefault("isMultiplayer", "");

        // Load pawns from BoardPosition if it exists
        if (pdnData.containsKey("BoardPosition")) {
            parseBoardPosition(pdnData.get("BoardPosition"));
            moves.clear(); // Ensure moves are empty if BoardPosition is used
        }
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

            if ("1-0".equals(turn) || "0-1".equals(turn) || "1/2-1/2".equals(turn)) {
                continue;
            }

            if (turn.contains(".")) {
                continue;
            }

            boolean isCapture = turn.contains("x");
            String[] positions = isCapture ? turn.split("x") : turn.split("-");

            try {
                Vector2i startPosition = null;
                Vector2i endPosition = null;

                if (positions.length > 0) {
                    startPosition = TileConversion.getTileVector(Integer.parseInt(positions[0]));
                }
                if (positions.length > 1) {
                    endPosition = TileConversion.getTileVector(Integer.parseInt(positions[1]));
                }

                List<Vector2i> capturedPositions = new ArrayList<>();
                if (isCapture && endPosition != null) {
                    capturedPositions.add(endPosition);
                }

                Move move = new Move(startPosition, endPosition, capturedPositions);
                moves.add(move);

            } catch (NumberFormatException e) {
                System.err.println("Skipping invalid move format: " + turn);
                e.printStackTrace();
            }
        }
    }

    private void parseBoardPosition(String boardPosition) {
        // Ensure the BoardPosition string contains both "W:" and "B:"
        if (!boardPosition.contains("W:") || !boardPosition.contains("B:")) {
            throw new IllegalArgumentException("Invalid BoardPosition format: Missing W: or B:");
        }

        // Extract the white and black positions
        String whitePositions =
                boardPosition.substring(
                        boardPosition.indexOf("W:") + 2, boardPosition.indexOf(":B:"));
        String blackPositions = boardPosition.substring(boardPosition.indexOf("B:") + 2);

        // Parse white pawns
        for (String position : whitePositions.split(",")) {
            boolean isKing = position.endsWith("k"); // Check if the position ends with 'k'
            int numericPosition =
                    Integer.parseInt(
                            isKing ? position.substring(0, position.length() - 1) : position);
            Vector2i pos = TileConversion.getTileVector(numericPosition);
            pawns.add(new Pawn(pos, true, isKing)); // Pass the king status
        }

        // Parse black pawns
        for (String position : blackPositions.split(",")) {
            boolean isKing = position.endsWith("k"); // Check if the position ends with 'k'
            int numericPosition =
                    Integer.parseInt(
                            isKing ? position.substring(0, position.length() - 1) : position);
            Vector2i pos = TileConversion.getTileVector(numericPosition);
            pawns.add(new Pawn(pos, false, isKing)); // Pass the king status
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

    public List<Pawn> getPawns() {
        return pawns;
    }

    public String getIsBot() {
        return isBot;
    }

    public String getIsMultiplayer() {
        return isMultiplayer;
    }

    public String getTurn() {
        return turn;
    }
}
