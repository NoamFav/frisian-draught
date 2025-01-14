package com.um_project_game.Server;

import com.um_project_game.Game;
import com.um_project_game.Launcher;
import com.um_project_game.board.MainBoard;
import com.um_project_game.board.Pawn;

import javafx.application.Platform;

import org.joml.Vector2i;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class NetworkClient {
    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;
    private Thread listenerThread;
    private Game gameReference;
    private MainBoard mainBoard;

    public NetworkClient(String host, int port, Game game) {
        this.gameReference = game;
        this.mainBoard = game.getMainBoard();
        try {
            socket = new Socket(host, port);
            writer = new PrintWriter(socket.getOutputStream(), true);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Start a separate thread to listen for messages from server
            listenerThread = new Thread(this::listenForServerMessages);
            listenerThread.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void listenForServerMessages() {
        try {
            String message;
            while ((message = reader.readLine()) != null) {
                handleServerMessage(message);
            }
        } catch (IOException e) {
            // Connection error or server closed
            e.printStackTrace();
        }
    }

    // Example: parse and handle server messages
    private void handleServerMessage(String message) {
        // Typical messages:
        // 1) "CHAT Hello everyone!"
        // 2) "UPDATE White moved from 3,5 to 4,6"
        // 3) "You are White" (role assignment)

        String name = Launcher.user.getName();

        if (message.startsWith("CHAT")) {
            String chatText = message.substring(4).trim();
            // Update your chatUI in Game
            Platform.runLater(
                    () -> {
                        gameReference.appendChatMessage(chatText, name);
                    });

        } else if (message.startsWith("MOVE")) {
            Platform.runLater(
                    () -> {
                        processUpdateMove(message);
                    });

        } else if (message.startsWith("You are")) {
            // e.g. "You are White"
            Platform.runLater(
                    () -> {
                        gameReference.setPlayerRole(
                                message.substring("You are".length()).trim(), name);
                    });
        }
    }

    private void processUpdateMove(String details) {
        System.out.println("[DEBUG] Received move command: " + details);

        // Example message: "MOVE 3,5 TO 9,7 CAPTURED 4,6 5,7 6,8"
        String[] parts = details.split(" ");
        if (parts.length < 4) {
            System.out.println(
                    "[ERROR] Invalid move format. Expected at least 4 parts, got: " + parts.length);
            return;
        }

        // Extract the start and end positions
        String fromCoords = parts[1];
        String toCoords = parts[3];
        System.out.println("[DEBUG] From: " + fromCoords + ", To: " + toCoords);

        // Convert coordinates to Vector2i objects
        Vector2i from = convertToVector2i(fromCoords);
        Vector2i to = convertToVector2i(toCoords);
        System.out.println("[DEBUG] Converted From: " + from + ", To: " + to);

        // Extract captured positions if present
        List<Vector2i> capturedPositions = new ArrayList<>();
        if (details.contains("CAPTURED")) {
            System.out.println("[DEBUG] Captures detected. Extracting captured positions...");
            for (int i = 5; i < parts.length; i++) {
                Vector2i capturedPos = convertToVector2i(parts[i]);
                capturedPositions.add(capturedPos);
                System.out.println("[DEBUG] Captured position: " + capturedPos);
            }
        }

        // Get the pawn to move
        Pawn pawnToMove = mainBoard.moveManager.getPawnAtPosition(from);
        if (pawnToMove == null) {
            System.out.println("[ERROR] No pawn found at position: " + from);
            return;
        }

        System.out.println("[DEBUG] Moving pawn: " + pawnToMove + " to " + to);

        // Animate the pawn movement and execute the move
        mainBoard.animatePawnMovement(
                pawnToMove,
                to,
                () -> {
                    System.out.println("[DEBUG] Executing move...");
                    mainBoard.moveManager.executeMove(pawnToMove, to);
                });

        // Remove captured pawns
        for (Vector2i capturedPos : capturedPositions) {
            System.out.println("[DEBUG] Removing captured pawn at: " + capturedPos);
            mainBoard.moveManager.removePawn(mainBoard.moveManager.getPawnAtPosition(capturedPos));
        }

        System.out.println("[DEBUG] Move processing complete.");
    }

    private Vector2i convertToVector2i(String coords) {
        String[] split = coords.split(",");
        int x = Integer.parseInt(split[0]);
        int y = Integer.parseInt(split[1]);
        return new Vector2i(x, y);
    }

    public void sendMove(Vector2i from, Vector2i to, List<Vector2i> capturedPositions) {
        if (from == null || to == null || capturedPositions == null || writer == null) {
            return; // Invalid move or writer not initialized
        }

        StringBuilder message =
                new StringBuilder("MOVE " + from.x + "," + from.y + " TO " + to.x + "," + to.y);
        if (!capturedPositions.isEmpty()) {
            message.append(" CAPTURED");
            for (Vector2i pos : capturedPositions) {
                message.append(" ").append(pos.x).append(",").append(pos.y);
            }
        }
        writer.println(message.toString());
    }

    // Public method to send messages to server
    public void sendMessage(String message) {
        if (writer != null) {
            writer.println(message);
        }
    }
}
