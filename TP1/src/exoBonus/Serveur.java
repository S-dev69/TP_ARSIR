package exoBonus;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Serveur {
    private static final int PORT = 8989;

    public static void main(String[] args) {
        System.out.println("Démarrage du serveur Puissance 4 sur le port " + PORT + "...");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            Jeu jeu = new Jeu();

            // Attente du Joueur 1 (X)
            Socket socket1 = serverSocket.accept();
            System.out.println("Joueur X connecté.");
            Joueur j1 = new Joueur(socket1, jeu, 'X');
            j1.envoyerMessage("WELCOME X");
            j1.envoyerMessage("WAIT");

            // Attente du Joueur 2 (O)
            Socket socket2 = serverSocket.accept();
            System.out.println("Joueur O connecté.");
            Joueur j2 = new Joueur(socket2, jeu, 'O');
            j2.envoyerMessage("WELCOME O");

            // Association des adversaires
            j1.setAdversaire(j2);
            j2.setAdversaire(j1);

            // Démarrage des threads
            j1.start();
            j2.start();

            // Notification du début de partie
            j1.envoyerMessage("START");
            j2.envoyerMessage("START");

            // Le joueur X commence
            j1.setMonTour(true);
            j1.envoyerMessage("YOUR_TURN");
            j2.envoyerMessage("WAIT_OPPONENT");

        } catch (IOException e) {
            System.err.println("Erreur serveur : " + e.getMessage());
        }
    }
}