package exo5;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Joueur extends Thread {
    private Socket socket;
    private Jeu jeu;
    private char symbole;
    private Joueur adversaire;

    private BufferedReader in;
    private PrintWriter out;

    public Joueur(Socket socket, Jeu jeu, char symbole) {
        this.socket = socket;
        this.jeu = jeu;
        this.symbole = symbole;

        try {
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            System.err.println("Erreur d'initialisation des flux pour le joueur " + symbole);
        }
    }

    public void setAdversaire(Joueur adversaire) {
        this.adversaire = adversaire;
    }

    public void envoyerMessage(String message) {
        out.println(message);
    }

    @Override
    public void run() {
        try {
            envoyerMessage("Bienvenue ! Tu joues les " + symbole);

            String ligne;
            // boucle de réception des requêtes
            while ((ligne = in.readLine()) != null) {
                System.out.println("Message reçu du joueur " + symbole + " : " + ligne);
                // TODO: logique du protocole
            }

        } catch (IOException e) {
            System.err.println("Le joueur " + symbole + " s'est déconnecté.");
        } finally {
            try {
                socket.close();
            } catch (IOException e) {}
        }
    }
}