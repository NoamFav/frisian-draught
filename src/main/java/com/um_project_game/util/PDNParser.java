package com.um_project_game.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PDNParser {

    private String event;
    private String site;
    private String date;
    private String round;
    private String blackPlayer;
    private String whitePlayer;
    private String result;
    private String moves;

    public PDNParser(String filePath) {
        Map<String, String> pdnData = new HashMap<>();
        StringBuilder movesBuilder = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("[")) {
                    int keyStart = line.indexOf('[') + 1;
                    int keyEnd = line.indexOf(' ');
                    int valueStart = line.indexOf("\"") + 1;
                    int valueEnd = line.lastIndexOf("\"");

                    String key = line.substring(keyStart, keyEnd);
                    String value = line.substring(valueStart, valueEnd);
                    pdnData.put(key, value);
                } else if (!line.trim().isEmpty()) {
                    movesBuilder.append(line).append(" ");
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }

        this.event = pdnData.getOrDefault("Event", "");
        this.site = pdnData.getOrDefault("Site", "");
        this.date = pdnData.getOrDefault("Date", "");
        this.round = pdnData.getOrDefault("Round", "");
        this.whitePlayer = pdnData.getOrDefault("White", "");
        this.blackPlayer = pdnData.getOrDefault("Black", "");
        this.result = pdnData.getOrDefault("Result", "");
        this.moves = movesBuilder.toString().trim();
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

    public String getMoves() {
        return moves;
    }

    public Object parseFile(String filePath) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }
}
