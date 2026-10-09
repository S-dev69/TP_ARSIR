package exo4;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class ClientHandler_old implements Runnable {
    private Socket socket;
    private String currentUser = null;
    private boolean isAuthenticated = false;

    // variables pour le transfert de données
    private String dataAddress = null;
    private int dataPort = -1;
    private ServerSocket passiveServer = null;
    private boolean isPassive = false;

    // variables pour la gestion des dossiers
    private File dossierRacine;
    private File dossierCourant;

    public ClientHandler_old(Socket socket) {
        this.socket = socket;
    }

    // connexion de données
    private Socket ouvrirConnexionDonnees() throws IOException {
        if (isPassive) {
            return passiveServer.accept();
        } else {
            return new Socket(dataAddress, dataPort);
        }
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

                        // gestion des droits
                        if (currentUser.equals("anonymous")) {
                            dossierRacine = new File("TP2/Data/anonymous");
                        } else {
                            dossierRacine = new File("TP2/Data");
                        }

                        // création automatique
                        if (!dossierRacine.exists()) {
                            dossierRacine.mkdirs();
                        }
                        dossierCourant = dossierRacine;

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
                // changement de dossier
                else if (command.equals("CWD")) {
                    if (!isAuthenticated) {
                        out.println("530 Non connecté");
                    } else {
                        try {
                            // chemin absolu
                            File nouveauDossier = new File(dossierCourant, arg).getCanonicalFile();
                            File racine = dossierRacine.getCanonicalFile();

                            if (nouveauDossier.exists() && nouveauDossier.isDirectory()) {
                                // sécurité anti-remontée
                                if (nouveauDossier.getPath().startsWith(racine.getPath())) {
                                    dossierCourant = nouveauDossier;
                                    out.println("250 Changement de dossier réussi");
                                } else {
                                    out.println("550 Accès refusé");
                                }
                            } else {
                                out.println("550 Dossier introuvable");
                            }
                        } catch (IOException e) {
                            out.println("550 Erreur de chemin");
                        }
                    }
                }
                // lister les fichiers
                else if (command.equals("LIST")) {
                    if (!isAuthenticated) {
                        out.println("530 Non connecté");
                    } else {
                        out.println("150 Ouverture de la connexion de données");
                        try (Socket dataSocket = ouvrirConnexionDonnees();
                             PrintWriter dataOut = new PrintWriter(dataSocket.getOutputStream(), true)) {

                            File[] fichiers = dossierCourant.listFiles();
                            if (fichiers != null) {
                                for (File f : fichiers) {
                                    dataOut.println(f.getName() + (f.isDirectory() ? "/" : ""));
                                }
                            }
                            out.println("226 Transfert de la liste terminé");
                        } catch (IOException e) {
                            out.println("425 Erreur de connexion de données");
                        }
                    }
                }
                // télécharger un fichier
                else if (command.equals("RETR")) {
                    if (!isAuthenticated) {
                        out.println("530 Non connecté");
                    } else {
                        try {
                            File fichier = new File(dossierCourant, arg).getCanonicalFile();
                            File racine = dossierRacine.getCanonicalFile();

                            // vérification des droits
                            if (fichier.exists() && fichier.isFile() && fichier.getPath().startsWith(racine.getPath())) {
                                out.println("150 Ouverture de la connexion de données");
                                try (Socket dataSocket = ouvrirConnexionDonnees();
                                     FileInputStream fis = new FileInputStream(fichier);
                                     OutputStream dataOut = dataSocket.getOutputStream()) {

                                    byte[] buffer = new byte[4096];
                                    int bytesLus;
                                    while ((bytesLus = fis.read(buffer)) != -1) {
                                        dataOut.write(buffer, 0, bytesLus);
                                    }
                                    out.println("226 Transfert du fichier terminé");
                                } catch (IOException e) {
                                    out.println("425 Erreur de connexion de données");
                                }
                            } else {
                                out.println("550 Fichier introuvable ou accès refusé");
                            }
                        } catch (IOException e) {
                            out.println("550 Erreur de chemin");
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