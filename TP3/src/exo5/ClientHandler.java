package exo5;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClientHandler implements Runnable {
    // ex : GET /index.html HTTP/1.1
    private static final Pattern REQUETE = Pattern.compile("^(\\S+) (\\S+) HTTP/\\d\\.\\d$");

    private static final Path RACINE = trouverRacine();
    private final Socket socket;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             OutputStream out = socket.getOutputStream()) {

            System.out.println("\n--- Requête de " + socket.getRemoteSocketAddress()
                    + " (" + Thread.currentThread().getName() + ") ---");

            // première ligne : ligne de requête
            String premiereLigne = in.readLine();
            if (premiereLigne == null) {
                return; // le client s'est déconnecté sans rien envoyer
            }
            System.out.println(premiereLigne);

            // on affiche le reste de la requête jusqu'à la ligne vide
            String ligne;
            while ((ligne = in.readLine()) != null && !ligne.isEmpty()) {
                System.out.println(ligne);
            }

            try {
                traiter(premiereLigne, out);
            } catch (Exception e) {
                // erreur interne inattendue -> 500
                System.err.println("Erreur interne : " + e.getMessage());
                envoyerErreur(out, 500);
            }

        } catch (IOException e) {
            System.err.println("Erreur de communication : " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                System.err.println("Erreur à la fermeture du socket : " + e.getMessage());
            }
        }
    }

    private void traiter(String premiereLigne, OutputStream out) throws IOException {
        Matcher m = REQUETE.matcher(premiereLigne);

        // format invalide -> 400
        if (!m.matches()) {
            envoyerErreur(out, 400);
            return;
        }

        String methode = m.group(1);
        String chemin = m.group(2);

        // méthode autorisée : GET uniquement -> sinon 405
        if (!methode.equals("GET")) {
            envoyerErreur(out, 405);
            return;
        }

        if (chemin.equals("/")) {
            chemin = "/index.html";
        }

        // protection contre les chemins du type ../
        Path fichier = RACINE.resolve("." + chemin).normalize();
        if (!fichier.startsWith(RACINE) || !Files.isRegularFile(fichier)) {
            envoyerErreur(out, 404);
            return;
        }

        // 200 : en-tête avec Content-Length, puis le contenu du fichier
        byte[] contenu = Files.readAllBytes(fichier);
        out.write(ReponseHTTP.genererEntete(200, contenu.length).getBytes(StandardCharsets.UTF_8));
        out.write(contenu);
        out.flush();
    }

    private void envoyerErreur(OutputStream out, int code) throws IOException {
        out.write(ReponseHTTP.genererReponseErreur(code).getBytes(StandardCharsets.UTF_8));
        out.flush();
    }

    private static Path trouverRacine() {
        // on essaie plusieurs emplacements selon d'où le programme est lancé
        String[] candidats = {
                "site_web",          // lancé depuis TP3
                "TP3/site_web",      // lancé depuis TP_ARSIR
                "../site_web"        // lancé depuis TP3/src
        };
        for (String c : candidats) {
            Path p = Paths.get(c).toAbsolutePath().normalize();
            if (Files.isDirectory(p)) {
                return p;
            }
        }
        // valeur par défaut (donnera des 404 si le dossier est introuvable)
        return Paths.get("site_web").toAbsolutePath().normalize();
    }
}