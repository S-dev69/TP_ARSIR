package exo2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private Socket socket;
    private String currentUser = null;
    private boolean isAuthenticated = false;

    // variables pour le transfert de données
    private String dataAddress = null;
    private int dataPort = -1;
    private ServerSocket passiveServer = null;
    private boolean isPassive = false;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
        ) {
            out.println("220 Serveur FTP prêt");

            String request;
            while ((request = in.readLine()) != null) {
                System.out.println("Reçu : " + request);

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
                    } else if (currentUser.equals("anonymous") || (currentUser.equals("foo") && arg.equals("bar"))) {
                        isAuthenticated = true;
                        out.println("200 Authentification reussie");
                    } else {
                        out.println("430 Identifiant ou mot de passe incorrect");
                    }
                }
                // mode actif
                else if (command.equals("PORT")) {
                    if (!isAuthenticated) {
                        out.println("530 Non connecté");
                    } else {
                        String[] p = arg.split(",");
                        if (p.length == 6) {
                            dataAddress = p[0] + "." + p[1] + "." + p[2] + "." + p[3];
                            dataPort = Integer.parseInt(p[4]) * 256 + Integer.parseInt(p[5]);
                            isPassive = false;
                            out.println("200 Commande PORT acceptée");
                        } else {
                            out.println("501 Erreur de syntaxe");
                        }
                    }
                }
                // mode passif
                else if (command.equals("PASV")) {
                    if (!isAuthenticated) {
                        out.println("530 Non connecté");
                    } else {
                        try {
                            if (passiveServer != null && !passiveServer.isClosed()) {
                                passiveServer.close();
                            }

                            passiveServer = new ServerSocket(0);
                            int port = passiveServer.getLocalPort();

                            // calcul des valeurs e et f
                            int p1 = port / 256;
                            int p2 = port % 256;

                            isPassive = true;
                            out.println("227 Entering Passive Mode (127,0,0,1," + p1 + "," + p2 + ")");
                        } catch (IOException e) {
                            out.println("425 Impossible d'ouvrir la socket de données");
                        }
                    }
                }
                else if (command.equals("QUIT")) {
                    out.println("221 Déconnexion");
                    break;
                }
                else {
                    out.println("501 Commande non implémentée");
                }
            }
        } catch (IOException e) {
            System.err.println("Client déconnecté.");
        } finally {
            try {
                if (passiveServer != null) passiveServer.close();
                socket.close();
            } catch (IOException e) {}
        }
    }
}