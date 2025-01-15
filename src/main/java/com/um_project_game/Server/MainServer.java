package com.um_project_game.Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class MainServer implements Runnable {

    private ServerSocket serverSocket;

    @Override
    public void run() {
        int port = 9000;

        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server is listening on port " + port);

            while (!serverSocket.isClosed()) {
                try {
                    Socket socket = serverSocket.accept();
                    System.out.println("New client connected");
                    new ClientHandler(socket).start();
                } catch (SocketException ex) {
                    if (serverSocket.isClosed()) {
                        System.out.println("Server socket closed, exiting loop.");
                        break;
                    } else {
                        throw ex;
                    }
                }
            }
        } catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void close() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
                serverSocket = null;
                System.out.println("Server closed");
            }
        } catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public boolean isRunning() {
        return serverSocket != null && !serverSocket.isClosed();
    }
}

class ClientHandler extends Thread {
    private Socket socket;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try (InputStream input = socket.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                OutputStream output = socket.getOutputStream();
                PrintWriter writer = new PrintWriter(output, true)) {

            String text;
            while ((text = reader.readLine()) != null) {
                System.out.println("Received: " + text);
                writer.println("Echo: " + text);
            }
        } catch (IOException ex) {
            System.out.println("Client disconnected: " + ex.getMessage());
        }
    }
}
