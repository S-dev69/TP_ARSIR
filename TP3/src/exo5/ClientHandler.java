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

            String premiereLigne = in.readLine();
            if (premiereLigne == null) {
                return;
            }
            System.out.println(premiereLigne);

            String ligne;
            while ((ligne = in.readLine()) != null && !ligne.isEmpty()) {
                System.out.println(ligne);
            }

            try {
                traiter(premiereLigne, out);
            } catch (Exception e) {
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

        if (!m.matches()) {
            envoyerErreur(out, 400);
            return;
        }

        String methode = m.group(1);
        String chemin = m.group(2);

        if (!methode.equals("GET")) {
            envoyerErreur(out, 405);
            return;
        }

        if (chemin.equals("/")) {
            chemin = "/index.html";
        }

        Path fichier = RACINE.resolve("." + chemin).normalize();
        if (!fichier.startsWith(RACINE) || !Files.isRegularFile(fichier)) {
            envoyerErreur(out, 404);
            return;
        }

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
        String[] candidats = {
                "site_web",
                "TP3/site_web",
                "../site_web"
        };
        for (String c : candidats) {
            Path p = Paths.get(c).toAbsolutePath().normalize();
            if (Files.isDirectory(p)) {
                return p;
            }
        }

        return Paths.get("site_web").toAbsolutePath().normalize();
    }
}