package exoBonus;

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
    private boolean monTour = false;

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

    public void setMonTour(boolean monTour) {
        this.monTour = monTour;
    }

    @Override
    public void run() {
        try {
            String ligne;
            // boucle de réception des requêtes
            while ((ligne = in.readLine()) != null) {
                System.out.println("Message reçu du joueur " + symbole + " : " + ligne);

                // Traitement d'un coup réseau (format: "MOVE 3")
                if (ligne.startsWith("MOVE") && monTour) {
                    try {
                        int c = Integer.parseInt(ligne.split(" ")[1]);

                        if (jeu.estCoupValide(c)) {
                            // On cherche où le jeton s'arrête
                            int l = jeu.trouverLigne(c);
                            jeu.jouerCoup(l, c, symbole);

                            // Information des joueurs
                            envoyerMessage("YOU_MOVED " + l + "," + c);
                            adversaire.envoyerMessage("OPPONENT_MOVED " + l + "," + c);

                            // Test de fin de partie
                            if (jeu.aGagne(symbole)) {
                                envoyerMessage("VICTORY");
                                adversaire.envoyerMessage("DEFEAT");
                                break;
                            } else if (jeu.estPleine()) {
                                envoyerMessage("DRAW");
                                adversaire.envoyerMessage("DRAW");
                                break;
                            }

                            // Passation du tour
                            this.monTour = false;
                            adversaire.setMonTour(true);
                            this.envoyerMessage("WAIT_OPPONENT");
                            adversaire.envoyerMessage("YOUR_TURN");

                        } else {
                            envoyerMessage("INVALID_MOVE");
                        }
                    } catch (Exception e) {
                        envoyerMessage("INVALID_MOVE");
                    }
                }
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