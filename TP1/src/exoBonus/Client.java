package exoBonus;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;

public class Client {
    private static final String HOST = "localhost";
    private static final int PORT = 8989;

    private static char[][] grille = new char[6][7];

    public static void main(String[] args) {
        System.out.println("Connexion au serveur Puissance 4...");

        // init de la grille locale
        for (char[] ligne : grille) {
            Arrays.fill(ligne, ' ');
        }

        try (Socket socket = new Socket(HOST, PORT);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader clavier = new BufferedReader(new InputStreamReader(System.in))) {

            System.out.println("Connecté au serveur !");
            char monSymbole = ' ';

            String message;
            while ((message = in.readLine()) != null) {

                if (message.startsWith("WELCOME")) {
                    monSymbole = message.charAt(8);
                    System.out.println("Vous jouez avec le symbole : " + monSymbole);
                }
                else if (message.equals("WAIT")) {
                    System.out.println("En attente d'un second joueur...");
                }
                else if (message.equals("START")) {
                    System.out.println("\n--- LA PARTIE COMMENCE ---");
                    afficherGrille();
                }
                // Gestion des tours
                else if (message.equals("YOUR_TURN")) {
                    System.out.print("\nC'est votre tour ! Entrez votre coup (colonne de 0 à 6) : ");
                    String coup = clavier.readLine();
                    out.println("MOVE " + coup);
                }
                else if (message.equals("WAIT_OPPONENT")) {
                    System.out.println("En attente du coup de l'adversaire...");
                }
                else if (message.startsWith("INVALID_MOVE")) {
                    System.out.println("Colonne pleine ou invalide ! Réessayez.");
                }
                // Mise à jour de la grille
                else if (message.startsWith("OPPONENT_MOVED")) {
                    String[] coords = message.split(" ")[1].split(",");
                    int l = Integer.parseInt(coords[0]);
                    int c = Integer.parseInt(coords[1]);
                    char symboleAdversaire = (monSymbole == 'X') ? 'O' : 'X';

                    grille[l][c] = symboleAdversaire;
                    afficherGrille();
                }
                else if (message.startsWith("YOU_MOVED")) {
                    String[] coords = message.split(" ")[1].split(",");
                    int l = Integer.parseInt(coords[0]);
                    int c = Integer.parseInt(coords[1]);

                    grille[l][c] = monSymbole;
                    afficherGrille();
                }
                // Fin de partie
                else if (message.equals("VICTORY")) {
                    System.out.println("\n🎉 BRAVO ! Vous avez gagné la partie !");
                    break;
                }
                else if (message.equals("DEFEAT")) {
                    System.out.println("\n DOMMAGE ! L'adversaire a gagné.");
                    break;
                }
                else if (message.equals("DRAW")) {
                    System.out.println("\n EGALITÉ ! La grille est pleine.");
                    break;
                }
                else if (message.equals("QUIT")) {
                    System.out.println("L'adversaire s'est déconnecté.");
                    break;
                }
            }

        } catch (Exception e) {
            System.err.println("Erreur client : " + e.getMessage());
        }
    }

    private static void afficherGrille() {
        System.out.println("\n  0   1   2   3   4   5   6");
        for (int i = 0; i < 6; i++) {
            System.out.print(i + " ");
            for (int j = 0; j < 7; j++) {
                System.out.print(grille[i][j]);
                if (j < 6) System.out.print(" | ");
            }
            System.out.println();
            if (i < 5) System.out.println(" ---+---+---+---+---+---+---");
        }
        System.out.println();
    }
}