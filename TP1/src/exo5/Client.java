package exo5;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {
    private static final String HOST = "localhost";
    private static final int PORT = 8989;

    private static char[][] grille = {
            {' ', ' ', ' '},
            {' ', ' ', ' '},
            {' ', ' ', ' '}
    };

    public static void main(String[] args) {
        System.out.println("Connexion au serveur Tic-Tac-Toe...");

        try (Socket socket = new Socket(HOST, PORT);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader clavier = new BufferedReader(new InputStreamReader(System.in))) {

            System.out.println("Connecté au serveur !");
            char monSymbole = ' ';
            boolean monTour = false;

            String message;
            while ((message = in.readLine()) != null) {

                // Initialisation : attribution du symbole ('X' ou 'O')
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
                // Gestion des tours de jeu
                else if (message.equals("YOUR_TURN")) {
                    monTour = true;
                    System.out.print("\nC'est votre tour ! Entrez votre coup (ligne,colonne ex: 1,2) : ");
                    String coup = clavier.readLine();
                    out.println("MOVE " + coup);
                }
                else if (message.equals("WAIT_OPPONENT")) {
                    monTour = false;
                    System.out.println("En attente du coup de l'adversaire...");
                }
                // Validation et mise à jour de la grille
                else if (message.startsWith("VALID_MOVE")) {
                    // On a joué un coup valide, la mise à jour sera confirmée ou faite directement
                }
                else if (message.startsWith("INVALID_MOVE")) {
                    System.out.println("Coup invalide ou case déjà occupée ! Réessayez.");
                }
                else if (message.startsWith("OPPONENT_MOVED")) {
                    // Exemple de message : "OPPONENT_MOVED 1,2 X"
                    String[] parts = message.split(" ");
                    String[] coords = parts[1].split(",");
                    int l = Integer.parseInt(coords[0]);
                    int c = Integer.parseInt(coords[1]);
                    char symboleAdversaire = (monSymbole == 'X') ? 'O' : 'X';

                    grille[l][c] = symboleAdversaire;
                    afficherGrille();
                }
                else if (message.startsWith("YOU_MOVED")) {
                    // Confirmation de notre propre coup : "YOU_MOVED 1,2"
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
                    System.out.println("\n❌ DOMMAGE ! L'adversaire a gagné.");
                    break;
                }
                else if (message.equals("DRAW")) {
                    System.out.println("\n🤝 EGALITÉ ! La grille est pleine.");
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

    // Affiche la grille 3x3 dans la console
    private static void afficherGrille() {
        System.out.println("\n  0   1   2");
        for (int i = 0; i < 3; i++) {
            System.out.print(i + " ");
            for (int j = 0; j < 3; j++) {
                System.out.print(grille[i][j]);
                if (j < 2) System.out.print(" | ");
            }
            System.out.println();
            if (i < 2) System.out.println(" ---+---+---");
        }
        System.out.println();
    }
}
