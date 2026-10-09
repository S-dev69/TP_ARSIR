package exo3;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServeurHTTP {
    private static final int PORT = 6666;

    public static void main(String[] args) {
        System.out.println("Serveur HTTP démarré sur le port " + PORT + "...");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(new ClientHandler_old(clientSocket)).start();
            }
        } catch (IOException e) {
            System.err.println("Erreur serveur : " + e.getMessage());
        }
    }
}