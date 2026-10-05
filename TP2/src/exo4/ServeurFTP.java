package exo4;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServeurFTP {
    private static final int PORT = 2121;

    public static void main(String[] args) {
        System.out.println("Serveur FTP démarré sur le port " + PORT + "...");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Nouveau client connecté : " + clientSocket.getInetAddress());

                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            System.err.println("Erreur serveur : " + e.getMessage());
        }
    }
}
