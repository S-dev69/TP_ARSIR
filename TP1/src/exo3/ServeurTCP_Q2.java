package exo3;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ServeurTCP_Q2 {
    private static final int PORT = 7777;

    public static void main(String[] args) {
        System.out.println("Serveur Horloge Parlante TCP (Q2) en écoute sur le port " + PORT + "...");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                // Acceptation du client
                try (Socket clientSocket = serverSocket.accept();
                     BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                     PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

                    System.out.println("Nouveau client connecté : " + clientSocket.getRemoteSocketAddress());

                    String commande;
                    // Boucle tant que le client envoie des commandes
                    while ((commande = in.readLine()) != null) {
                        commande = commande.trim().toUpperCase();
                        System.out.println("Commande reçue : " + commande);

                        if ("CLOSE".equals(commande)) {
                            out.println("Connexion fermée. Au revoir !");
                            break; // Sort de la boucle pour fermer la socket
                        }

                        LocalDateTime maintenant = LocalDateTime.now();
                        String reponse;

                        switch (commande) {
                            case "DATE":
                                reponse = "DATE: " + maintenant.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                                break;
                            case "HOUR":
                                reponse = "HOUR: " + maintenant.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                                break;
                            case "FULL":
                                reponse = "FULL: " + maintenant.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
                                break;
                            default:
                                reponse = "ERREUR: Commande inconnue. Tapez DATE, HOUR, FULL ou CLOSE.";
                                break;
                        }

                        out.println(reponse);
                    }

                    System.out.println("Client déconnecté.");
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur serveur : " + e.getMessage());
        }
    }
}