package exo4;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class ServeurCapitalisation {
    private static final int PORT = 8888;

    public static void main(String[] args) {
        System.out.println("Serveur de Capitalisation en écoute sur le port " + PORT + "...");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                // Attente de la connexion d'un client
                try (Socket clientSocket = serverSocket.accept();
                     BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                     PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

                    System.out.println("Client connecté : " + clientSocket.getRemoteSocketAddress());

                    String ligneRecue;
                    // Lecture ligne par ligne tant que le client envoie du texte
                    while ((ligneRecue = in.readLine()) != null) {
                        System.out.println("Reçu du client : " + ligneRecue);

                        // Transformation en majuscules
                        String ligneMajuscule = ligneRecue.toUpperCase();

                        // Renvoi au client
                        out.println(ligneMajuscule);
                    }

                    System.out.println("Client déconnecté.");
                } catch (Exception e) {
                    System.err.println("Erreur de communication avec le client : " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur du serveur : " + e.getMessage());
        }
    }
}