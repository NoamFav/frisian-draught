package com.um_project_game.Server;

import com.um_project_game.Game;
import com.um_project_game.Launcher;
import com.um_project_game.board.CapturePath;
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
import java.util.stream.Collectors;

public class NetworkClient {
    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;
    private Thread listenerThread;
    private Game gameReference;
    private MainBoard mainBoard;

    private String lastProcessedMove = "";

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
        if (details.equals(lastProcessedMove)) {
            System.out.println("[DEBUG] Skipping duplicate move: " + details);
            return;
        }
        lastProcessedMove = details;

        if (details.startsWith("MOVE")) {
            Platform.runLater(() -> processUpdateMove(details));
        }

        // Example message formats:
        // - "MOVE 3,5 TO 4,6" (normal move)
        // - "MOVE 3,5 TO 9,7 CAPTURED 4,6 5,7" (capture move)

        String[] parts = details.split(" ");
        if (parts.length < 4) {
            System.out.println(
                    "[ERROR] Invalid move format. Expected at least 4 parts, got: " + parts.length);
            return;
        }

        Vector2i start = convertToVector2i(parts[1]);
        Vector2i end = convertToVector2i(parts[3]);
        List<Vector2i> capturedPositions = new ArrayList<>();

        boolean isCaptureMove = details.contains("CAPTURED");
        if (isCaptureMove) {
            for (int i = 5; i < parts.length; i++) {
                capturedPositions.add(convertToVector2i(parts[i]));
            }
        }

        Pawn pawnToMove = mainBoard.moveManager.getPawnAtPosition(start);
        if (pawnToMove == null) {
            System.out.println("[ERROR] No pawn found at position: " + start);
            return;
        }

        if (!isCaptureMove) {
            System.out.println("[DEBUG] Processing normal move...");
            mainBoard.animatePawnMovement(
                    pawnToMove, end, () -> mainBoard.moveManager.executeMove(pawnToMove, end));
        } else {
            System.out.println("[DEBUG] Processing capture move...");

            List<CapturePath> allPaths = new ArrayList<>();
            mainBoard.moveManager.captureCheck(
                    pawnToMove,
                    (x, y) ->
                            x >= 0
                                    && x < mainBoard.boardState.getBoardSize().x
                                    && y >= 0
                                    && y < mainBoard.boardState.getBoardSize().y,
                    start.x,
                    start.y,
                    new CapturePath(),
                    allPaths);

            System.out.println("[DEBUG] All generated capture paths:");
            for (CapturePath path : allPaths) {
                System.out.println(
                        "[DEBUG] Path: " + path.positions + " | Captured: " + path.capturedPawns);
            }
            System.out.println("[DEBUG] Captured positions from server: " + capturedPositions);

            CapturePath matchingPath =
                    allPaths.stream()
                            .filter(
                                    path ->
                                            path.capturedPawns.size() == capturedPositions.size()
                                                    && path.capturedPawns.stream()
                                                            .map(Pawn::getPosition)
                                                            .collect(Collectors.toList())
                                                            .containsAll(capturedPositions))
                            .findFirst()
                            .orElse(null);

            if (matchingPath == null) {
                System.out.println("[ERROR] No matching capture path found!");
                return;
            }

            System.out.println("[DEBUG] Recreated CapturePath: " + matchingPath.positions);
            mainBoard.animatePawnCaptureMovement(
                    pawnToMove,
                    matchingPath,
                    () -> mainBoard.moveManager.processAfterCaptureMove(pawnToMove, matchingPath));
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

    public void sendMove(Vector2i from, Vector2i to) {
        sendMove(from, to, new ArrayList<>());
    }

    // Public method to send messages to server
    public void sendMessage(String message) {
        if (writer != null) {
            writer.println(message);
        }
    }
}
