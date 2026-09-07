package exo3;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;

public class ClientTCP_Q1 {
    private static final String HOST = "localhost";
    private static final int PORT = 7777;

    public static void main(String[] args) {
        try (Socket socket = new Socket(HOST, PORT);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            // Lecture de la réponse envoyée par le serveur dès l'ouverture
            String reponse = in.readLine();
            System.out.println("Reçu du serveur : " + reponse);

        } catch (Exception e) {
            System.err.println("Erreur client : " + e.getMessage());
        }
    }
}