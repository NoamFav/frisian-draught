package com.um_project_game.Server;

import java.io.*;
import java.net.Socket;

public class ClientHandler extends Thread {
    private Socket socket;
    private PrintWriter writer;
    private String playerRole;

    public ClientHandler(Socket socket) {
        this.socket = socket;
        assignRole();
    }

    @Override
    public void run() {
        try (BufferedReader reader =
                new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            writer = new PrintWriter(socket.getOutputStream(), true);

            String message;
            while ((message = reader.readLine()) != null) {
                if (message.startsWith("CHAT")) {
                    MainServer.broadcast(message, this);
                } else if (message.startsWith("MOVE")) {
                    handleMove(message);
                }
            }
        } catch (IOException e) {
            System.out.println(playerRole + " disconnected.");
        } finally {
            MainServer.removeClient(this);
            MainServer.broadcast(playerRole + " has left the game.", null);
        }
    }

    private void assignRole() {
        synchronized (MainServer.clients) {
            if (MainServer.clients.size() == 1) {
                playerRole = "White";
            } else if (MainServer.clients.size() == 2) {
                playerRole = "Black";
            } else {
                playerRole = "Spectator";
            }
        }
        sendMessage("You are " + playerRole);
    }

    public void sendMessage(String message) {
        if (writer != null) {
            writer.println(message);
        }
    }

    private void handleMove(String moveCommand) {
        // Example format: MOVE 3,5 TO 4,6
        String[] parts = moveCommand.split(" ");
        if (!parts[0].equals("MOVE") || !parts[2].equals("TO")) {
            sendMessage("Invalid move command");
            return;
        }

        String from = parts[1];
        String to = parts[3];
        System.out.println(playerRole + moveCommand);

        // Broadcast the move to the other player
        MainServer.broadcast(moveCommand, this);
    }

    private void startTurnTimer() {
        new Thread(
                        () -> {
                            try {
                                int timeRemaining = 60; // 60 seconds per turn
                                while (timeRemaining > 0) {
                                    Thread.sleep(1000);
                                    timeRemaining--;
                                    sendMessage("Time remaining: " + timeRemaining + " seconds");
                                }
                                sendMessage("Time's up!");
                                MainServer.broadcast(playerRole + " ran out of time!", this);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        })
                .start();
    }
}
