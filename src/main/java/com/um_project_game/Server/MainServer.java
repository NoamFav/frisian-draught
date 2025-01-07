package com.um_project_game.Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class MainServer {

    private ServerSocket serverSocket;
    public static List<ClientHandler> clients = new ArrayList<>();

    public static void main(String[] args) {
        new MainServer().startServer();
    }

    public void startServer() {
        try {
            serverSocket = new ServerSocket(9000);
            System.out.println("Server is listening on port 9000");

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New client connected");

                ClientHandler clientHandler = new ClientHandler(socket);
                clients.add(clientHandler);
                clientHandler.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void broadcast(String message, ClientHandler excludeClient) {
        for (ClientHandler client : clients) {
            if (client != excludeClient) {
                client.sendMessage(message);
            }
        }
    }

    public static void removeClient(ClientHandler client) {
        synchronized (clients) {
            clients.remove(client);
        }
    }
}
