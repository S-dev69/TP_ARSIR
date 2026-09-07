package exo4;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientCapitalisation {
    private static final String HOST = "localhost";
    private static final int PORT = 8888;

    public static void main(String[] args) {
        System.out.println("Connexion au serveur de capitalisation...");

        try (Socket socket = new Socket(HOST, PORT);
             // Flux pour lire la réponse du serveur
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             // Flux pour envoyer du texte au serveur (auto-flush activé avec true)
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             // Flux pour lire la saisie de l'utilisateur au clavier
             BufferedReader clavier = new BufferedReader(new InputStreamReader(System.in))) {

            System.out.println("Connecté ! Tapez votre texte (Appuyez sur Entrée pour envoyer, Ctrl+C pour quitter) :");

            String ligneSaisie;
            // Lit ligne par ligne la saisie au clavier
            while ((ligneSaisie = clavier.readLine()) != null) {
                // Envoi au serveur
                out.println(ligneSaisie);

                // Lecture de la réponse transformée par le serveur
                String reponse = in.readLine();
                if (reponse != null) {
                    System.out.println("Réponse du serveur : " + reponse);
                } else {
                    System.out.println("Le serveur a fermé la connexion.");
                    break;
                }
            }

        } catch (Exception e) {
            System.err.println("Erreur client : " + e.getMessage());
        }
    }
}
