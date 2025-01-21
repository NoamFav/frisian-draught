package com.um_project_game.Server;

import com.um_project_game.Game;
import com.um_project_game.Launcher;
import com.um_project_game.board.CapturePath;
import com.um_project_game.board.MainBoard;
import com.um_project_game.board.Pawn;

import javafx.application.Platform;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
    private static final Logger logger = LogManager.getLogger(NetworkClient.class);

    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;
    private Thread listenerThread;
    private Game gameReference;
    private MainBoard mainBoard;

    private String lastProcessedMove = "";
    private String OPPONENT_NAME = "";

    private boolean closed = false;

    public NetworkClient(Game game) {
        this.gameReference = game;
        this.mainBoard = game.getMainBoard();
        String host = "server-billowing-bird-4582.fly.dev"; // Default host
        int port = 9000; // Default port
        System.out.println("Attempting to connect to server at " + host + ":" + port);
        try {
            socket = new Socket(host, port);
            writer = new PrintWriter(socket.getOutputStream(), true);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            writer.println("READY");
            writer.flush();

            writer.println("OPPONENT_NAME " + Launcher.user.getName());
            writer.flush();

            System.out.println("Sent ready message to server.");

            // Start a separate thread to listen for messages from server
            listenerThread = new Thread(this::listenForServerMessages);
            listenerThread.start();

        } catch (IOException e) {
            logger.error(
                    "Failed to connect to server at {}:{}, error: {}", host, port, e.getMessage());
        }
    }

    private void listenForServerMessages() {
        try {

            System.out.println("Listening for server messages...");
            String message;
            System.out.println("Starting message loop...");
            while ((message = reader.readLine()) != null) {

                handleServerMessage(message);
            }
            System.out.println("Server closed connection.");
        } catch (IOException e) {
            // Connection error or server closed
            logger.error("Error while listening to server messages: {}", e.getMessage());
        }
    }

    // Example: parse and handle server messages
    private void handleServerMessage(String message) {
        String name = Launcher.user.getName();
        System.out.println("Received message: " + message);

        if (message.startsWith("CHAT")) {
            String chatText = message.substring(4).trim();
            // Update your chatUI in Game
            Platform.runLater(
                    () -> {
                        gameReference.appendChatMessage(chatText);
                        System.out.println("Chat message: " + chatText);
                    });

        } else if (message.startsWith("MOVE")) {
            Platform.runLater(
                    () -> {
                        processUpdateMove(message);
                    });

        } else if (message.startsWith("You are")) {
            System.out.println("Received role message: " + message);
            String role = message.substring("You are".length()).trim();
            System.out.println("Role: " + role);
            Platform.runLater(
                    () -> {
                        System.out.println("Setting role: " + role);
                        gameReference.setPlayerRole(role, name);
                        System.out.println("Role set: " + role);
                    });
        } else if (message.startsWith("OPPONENT_NAME")) {
            OPPONENT_NAME = message.substring("OPPONENT_NAME".length()).trim();
            System.out.println("Opponent name: " + OPPONENT_NAME);
            Platform.runLater(
                    () -> {
                        System.out.println("Setting opponent name: " + OPPONENT_NAME);
                        gameReference.setOpponentName(OPPONENT_NAME);
                    });
        } else if (message.startsWith("RESIGN")) {
            Platform.runLater(
                    () -> {
                        gameReference.showResignDialog();
                    });
        } else if (message.startsWith("DRAW")) {
            if (message.contains("ACCEPTED")) {
                Platform.runLater(
                        () -> {
                            gameReference.showDrawAcceptedDialog();
                        });
            } else if (message.contains("DECLINED")) {
                Platform.runLater(
                        () -> {
                            gameReference.showDrawDeclinedDialog();
                        });
            } else {
                Platform.runLater(
                        () -> {
                            gameReference.showDrawDialog();
                        });
            }
        }
    }

    private void processUpdateMove(String details) {
        System.out.println("Processing move: " + details);
        if (details.equals(lastProcessedMove)) {
            System.out.println("Ignoring duplicate move...");
            return;
        }
        lastProcessedMove = details;

        String[] parts = details.split(" ");
        if (parts.length < 4) {
            System.out.println("Invalid move command");
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
            System.out.println("No pawn found at start position: " + start);
            return;
        }

        if (!isCaptureMove) {
            System.out.println("Processing normal move...");
            mainBoard.animatePawnMovement(
                    pawnToMove, end, () -> mainBoard.moveManager.executeMove(pawnToMove, end));
        } else {
            System.out.println("Processing capture move...");

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

            System.out.println("All paths:");
            for (CapturePath path : allPaths) {
                System.out.println(path.positions);
            }

            System.out.println("Captured positions: " + capturedPositions);

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
                System.out.println(
                        "No matching path found for captured positions: " + capturedPositions);
                return;
            }

            System.out.println("Matching path: " + matchingPath.positions);
            mainBoard.animatePawnCaptureMovement(
                    pawnToMove,
                    matchingPath,
                    () -> mainBoard.moveManager.processAfterCaptureMove(pawnToMove, matchingPath));
        }
        System.out.println("Move processed.");
    }

    private Vector2i convertToVector2i(String coords) {
        String[] split = coords.split(",");
        int x = Integer.parseInt(split[0]);
        int y = Integer.parseInt(split[1]);
        return new Vector2i(x, y);
    }

    public void sendMove(Vector2i from, Vector2i to, List<Vector2i> capturedPositions) {
        if (from == null || to == null || capturedPositions == null || writer == null) {
            System.out.println("Invalid move command");
            return;
        }

        StringBuilder message =
                new StringBuilder("MOVE " + from.x + "," + from.y + " TO " + to.x + "," + to.y);

        if (!capturedPositions.isEmpty()) {
            message.append(" CAPTURED");
            for (Vector2i pos : capturedPositions) {
                message.append(" ").append(pos.x).append(",").append(pos.y);
            }
        }

        if (message.toString().equals(lastProcessedMove)) {
            System.out.println("Ignoring duplicate move...");
            return;
        }

        writer.println(message.toString());
        System.out.println("Sent move: " + message);
    }

    public void sendMove(Vector2i from, Vector2i to) {
        sendMove(from, to, new ArrayList<>());
    }

    public void sendMessage(String message) {
        if (message.equals(lastProcessedMove)) {
            System.out.println("Ignoring duplicate message...");
            return;
        }
        if (writer != null) {
            writer.println(message);
            System.out.println("Sent message: " + message);
        } else {
            System.out.println("Failed to send message: " + message);
        }
    }

    public void close() {
        try {
            if (socket != null && !socket.isClosed() && writer != null && !writer.checkError()) {
                writer.println("DISCONNECT");
                writer.flush();
            }

            if (listenerThread != null && listenerThread.isAlive()) {
                listenerThread.interrupt();
                listenerThread.join(1000);
                if (listenerThread.isAlive()) {
                    logger.warn("Listener thread did not terminate in time.");
                }
                listenerThread = null;
            }

            if (writer != null) {
                writer.close();
                writer = null;
            }
            if (reader != null) {
                reader.close();
                reader = null;
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
                socket = null;
            }

            logger.info("Network client closed successfully.");
        } catch (IOException e) {
            logger.error("Error while closing client: {}", e.getMessage());
        } catch (InterruptedException e) {
            logger.error("Error while waiting for listener thread to finish: {}", e.getMessage());
            Thread.currentThread().interrupt();
        } finally {
            closed = true;
            writer = null;
            reader = null;
            socket = null;
            listenerThread = null;
        }
    }
}
