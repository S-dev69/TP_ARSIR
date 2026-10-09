package exo5;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServeurHTTP {
    private static final int PORT = 8080;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Serveur démarré sur le port " + PORT + "...");

            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    new Thread(new ClientHandler(clientSocket)).start();
                } catch (IOException e) {
                    // une connexion qui échoue ne doit pas arrêter le serveur
                    System.err.println("Erreur lors de l'acceptation d'un client : " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Impossible de démarrer le serveur : " + e.getMessage());
        }
    }
}