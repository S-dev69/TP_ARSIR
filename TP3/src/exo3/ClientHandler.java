package exo3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private Socket socket;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            System.out.println("\n--- Nouvelle requête HTTP reçue ---");

            String ligne;
            while ((ligne = in.readLine()) != null && !ligne.isEmpty()) {
                System.out.println(ligne);
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