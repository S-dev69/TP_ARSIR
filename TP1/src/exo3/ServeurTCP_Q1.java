package exo3;

import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class ServeurTCP_Q1 {
    private static final int PORT = 7777;

    public static void main(String[] args) {
        System.out.println("Serveur TCP Horloge (Q1) démarré sur le port " + PORT + "...");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                // Attente et acceptation d'une connexion client
                try (Socket clientSocket = serverSocket.accept();
                     PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

                    System.out.println("Client connecté : " + clientSocket.getRemoteSocketAddress());

                    // Envoi de l'heure courante
                    String heure = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                    out.println("Heure du serveur : " + heure);

                    // La socket se ferme automatiquement à la fin du try-with-resources
                    System.out.println("Heure envoyée, connexion fermée.");
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur serveur : " + e.getMessage());
        }
    }
}