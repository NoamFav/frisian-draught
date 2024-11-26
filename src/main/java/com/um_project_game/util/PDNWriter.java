package com.um_project_game.util;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

public class PDNWriter {

    private String event; // Name of the tournament / event
    private String site; // Name of Site / Location
    private String date; // Date of game2
    private String round; // Round / "-"
    private String blackPlayer; // Name of black

    private String whitePlayer; // Name of white

    private String turn; // B / W

    private String result; // 1-0 / 1/2-1/2 / 0-1
    private static final String GAMETYPE = "40"; // 40 - Frisian
    private String isBot; // 0 / 1
    private String isMultiplayer; // 0 / 1
    private String moves; // Played moves in draughts notation

    // Additional Tags
    //
    // FEN -> Starting a game from a different starting position,
    // Notation -> Different notational styles
    // Time
    //

    public PDNWriter(
            String event,
            String site,
            String round,
            String date,
            String whitePlayer,
            String blackPlayer,
            String turn,
            String isBot,
            String isMultiplayer,
            String result,
            String moves) {
        this.event = event;
        this.site = site;
        this.round = round;
        this.date = date;
        this.whitePlayer = whitePlayer;
        this.blackPlayer = blackPlayer;
        this.turn = turn;
        this.result = result;
        this.isBot = isBot;
        this.isMultiplayer = isMultiplayer;
        this.moves = moves;
    }

    public void generatePDNFile(OutputStream outputStream) {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream))) {
            writer.write("[Event \"" + event + "\"]\n");
            writer.write("[Site \"" + site + "\"]\n");
            writer.write("[Round \"" + round + "\"]\n");
            writer.write("[Date \"" + date + "\"]\n");
            writer.write("[White \"" + whitePlayer + "\"]\n");
            writer.write("[Black \"" + blackPlayer + "\"]\n");
            writer.write("[Turn \"" + turn + "\"]\n");
            writer.write("[isBot \"" + isBot + "\"]\n");
            writer.write("[isMultiplayer \"" + isMultiplayer + "\"]\n");
            if (result != null) {
                writer.write("[Result \"" + result + "\"]\n");
            }
            writer.write("[GameType \"" + GAMETYPE + "\"]\n\n");
            writer.write(moves.trim() + "\n");
            if (result != null) {
                writer.write(result + "\n");
            }
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
        }
    }
}
