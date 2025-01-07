package com.um_project_game.Server;

import com.um_project_game.Game;

import javafx.application.Platform;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class NetworkClient {
    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;
    private Thread listenerThread;
    private Game gameReference;

    public NetworkClient(String host, int port, Game game) {
        this.gameReference = game;
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

        if (message.startsWith("CHAT")) {
            String chatText = message.substring(4).trim();
            // Update your chatUI in Game
            Platform.runLater(
                    () -> {
                        gameReference.appendChatMessage(chatText);
                    });

        } else if (message.startsWith("UPDATE")) {
            // e.g. "UPDATE White moved from 3,5 to 4,6"
            // Parse details and tell `Game` / `MainBoard` to update the board
            // so it shows the new move
            String details = message.substring(6).trim();
            Platform.runLater(
                    () -> {
                        processUpdateMove(details);
                    });

        } else if (message.startsWith("You are")) {
            // e.g. "You are White"
            Platform.runLater(
                    () -> {
                        gameReference.setPlayerRole(message.substring("You are".length()).trim());
                    });
        }
    }

    private void processUpdateMove(String details) {
        // Parse something like "White moved from 3,5 to 4,6"
        // Then instruct your `Game` or `MoveManager` to update the local board state
        String[] parts = details.split(" ");
        // parts = ["White", "moved", "from", "3,5", "to", "4,6"]
        if (parts.length == 6) {
            String role = parts[0];
            String fromCoords = parts[3];
            String toCoords = parts[5];

            // parse "3,5" => fromX=3, fromY=5, etc.
            // Then call your move logic: mainBoard.moveManager.executeMove(...)
            // or mainBoard.animatePawnMovement(...)

            // gameReference.getMainBoard()...
        }
    }

    // Public method to send messages to server
    public void sendMessage(String message) {
        if (writer != null) {
            writer.println(message);
        }
    }
}
