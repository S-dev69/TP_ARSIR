package exo1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

class ClientHandler implements Runnable {
    private Socket socket;
    private String currentUser = null;
    private boolean isAuthenticated = false;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
        ) {
            out.println("220 Bienvenue sur le serveur FTP de test");

            String request;
            while ((request = in.readLine()) != null) {
                System.out.println("Reçu : " + request);

                // découpage commande argument
                String[] parts = request.split(" ", 2);
                String command = parts[0].toUpperCase();
                String arg = parts.length > 1 ? parts[1].trim() : "";

                // traitement du login
                if (command.equals("USER")) {
                    if (arg.equals("anonymous") || arg.equals("foo")) {
                        currentUser = arg;
                        out.println("331 Utilisateur reconnu, en attente du mot de passe");
                    } else {
                        currentUser = null;
                        out.println("430 Identifiant ou mot de passe incorrect");
                    }
                }
                else if (command.equals("PASS")) {
                    if (currentUser == null) {
                        out.println("501 Erreur de syntaxe : faites USER avant PASS");
                    } else if (currentUser.equals("anonymous")) {
                        isAuthenticated = true;
                        out.println("200 Action demandée accomplie avec succès (Anonyme)");
                    } else if (currentUser.equals("foo") && arg.equals("bar")) {
                        isAuthenticated = true;
                        out.println("200 Action demandée accomplie avec succès (Bienvenue foo)");
                    } else {
                        out.println("430 Identifiant ou mot de passe incorrect");
                    }
                }
                else if (command.equals("QUIT")) {
                    out.println("221 Déconnexion");
                    break;
                }
                else {
                    out.println("501 Erreur de syntaxe / Commande inconnue");
                }
            }
        } catch (IOException e) {
            System.err.println("Client déconnecté brutalement.");
        } finally {
            try { socket.close(); } catch (IOException e) {}
        }
    }
}