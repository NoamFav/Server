package com.server;

import java.io.*;
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
            System.out.println("Server is listening on port 9000...");

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New client connected");

                ClientHandler clientHandler = new ClientHandler(socket);
                synchronized (clients) {
                    clients.add(clientHandler);
                }
                clientHandler.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static synchronized String assignRole(ClientHandler client) {
        boolean whiteAssigned = clients.stream().anyMatch(c -> "White".equals(c.getPlayerRole()));
        boolean blackAssigned = clients.stream().anyMatch(c -> "Black".equals(c.getPlayerRole()));

        if (!whiteAssigned) {
            return "White";
        } else if (!blackAssigned) {
            return "Black";
        } else {
            return "Spectator";
        }
    }

    public static void broadcast(String message, ClientHandler excludeClient) {
        synchronized (clients) {
            for (ClientHandler client : clients) {
                if (client != excludeClient) {
                    client.sendMessage(message);
                }
            }
        }
    }

    public static void removeClient(ClientHandler client) {
        synchronized (clients) {
            clients.remove(client);
        }
        reassignRoles();
    }

    private static void reassignRoles() {
        synchronized (clients) {
            boolean whiteAssigned = false;
            boolean blackAssigned = false;

            for (ClientHandler client : clients) {
                if (!whiteAssigned) {
                    client.setPlayerRole("White");
                    whiteAssigned = true;
                } else if (!blackAssigned) {
                    client.setPlayerRole("Black");
                    blackAssigned = true;
                } else {
                    client.setPlayerRole("Spectator");
                }
                client.sendMessage("You are " + client.getPlayerRole());
            }
        }
    }
}
