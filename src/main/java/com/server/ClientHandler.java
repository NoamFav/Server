package com.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

class ClientHandler extends Thread {
    private Socket socket;
    private PrintWriter writer;
    private String playerRole;

    public ClientHandler(Socket socket) {
        System.out.println("ClientHandler created");
        this.socket = socket;
        this.playerRole = MainServer.assignRole(this);
    }

    @Override
    public void run() {
        try (BufferedReader reader =
                new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            writer = new PrintWriter(socket.getOutputStream(), true);

            String message;
            System.out.println("Waiting for messages from " + playerRole);
            while ((message = reader.readLine()) != null) {
                System.out.println(playerRole + " sent: " + message);
                if (message.startsWith("CHAT")) {
                    MainServer.broadcast(message, this);
                } else if (message.startsWith("MOVE")) {
                    handleMove(message);
                } else if (message.equals("READY")) {
                    sendMessage("You are " + playerRole);
                    MainServer.broadcast(playerRole + " has joined the game.", null);
                    System.out.println(playerRole + " has joined the game.");
                }
            }
        } catch (IOException e) {
            System.out.println(playerRole + " disconnected.");
        } finally {
            MainServer.removeClient(this);
            MainServer.broadcast(playerRole + " has left the game.", null);
        }
    }

    public void sendMessage(String message) {
        if (writer != null) {
            writer.println(message);
        }
    }

    public String getPlayerRole() {
        return playerRole;
    }

    public void setPlayerRole(String role) {
        this.playerRole = role;
    }

    private void handleMove(String moveCommand) {
        // Example format: MOVE 3,5 TO 4,6
        String[] parts = moveCommand.split(" ");
        if (!parts[0].equals("MOVE") || !parts[2].equals("TO")) {
            sendMessage("Invalid move command");
            return;
        }

        System.out.println(playerRole + " " + moveCommand);

        // Broadcast the move to the other player
        MainServer.broadcast(moveCommand, this);
    }
}
