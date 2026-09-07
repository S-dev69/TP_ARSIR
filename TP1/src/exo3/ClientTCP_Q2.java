package exo3;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class ClientTCP_Q2 {
    private static final String HOST = "localhost";
    private static final int PORT = 7777;

    public static void main(String[] args) {
        try (Socket socket = new Socket(HOST, PORT);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             Scanner scanner = new Scanner(System.in)) {

            System.out.println("Connecté au serveur ! Commandes disponibles : DATE, HOUR, FULL, CLOSE");

            while (true) {
                System.out.print("\nEntrez une commande > ");
                String commande = scanner.nextLine();

                // Envoi de la commande au serveur
                out.println(commande);

                // Lecture de la réponse
                String reponse = in.readLine();
                System.out.println("Réponse serveur > " + reponse);

                // Si on a envoyé CLOSE, on quitte la boucle
                if ("CLOSE".equalsIgnoreCase(commande.trim())) {
                    break;
                }
            }

        } catch (Exception e) {
            System.err.println("Erreur client : " + e.getMessage());
        }
    }
}