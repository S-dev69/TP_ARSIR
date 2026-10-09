package exo4;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private Socket socket;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    private String recevoirRequete(BufferedReader in) throws IOException {
        StringBuilder requeteComplete = new StringBuilder();
        String ligne;
        while ((ligne = in.readLine()) != null && !ligne.isEmpty()) {
            requeteComplete.append(ligne).append("\r\n");
        }
        return requeteComplete.toString();
    }

    private int analyserRequete(String requete) {
        if (requete == null || requete.isEmpty()) return 400;

        String[] parties = requete.split("\r\n")[0].split(" ");
        if (parties.length != 3) return 400;

        if (!parties[0].equals("GET")) return 405;

        return 200;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            System.out.println("\n--- Nouvelle requête HTTP reçue ---");

            String requeteBrute = recevoirRequete(in);
            System.out.print(requeteBrute);

            if (!requeteBrute.isEmpty()) {
                int codeHttp = analyserRequete(requeteBrute);
                System.out.println("-> Résultat de l'analyse : Code " + codeHttp);
            }

        } catch (IOException e) {
            System.err.println("Erreur de communication : " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException e) {}
        }
    }
}