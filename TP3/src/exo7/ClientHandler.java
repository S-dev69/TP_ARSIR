package exo7;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClientHandler implements Runnable {
    private static final Pattern REQUETE = Pattern.compile("^(\\S+) (\\S+) HTTP/\\d\\.\\d$");

    private static final Map<String, Path> SITES = new HashMap<>();
    static {
        Path site1 = trouverRacine("site_web");
        Path site2 = trouverRacine("site_web2");
        SITES.put("localhost", site1);
        SITES.put("site1.localhost", site1);
        SITES.put("site2.localhost", site2);
    }

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

            String host = null;
            String ligne;

            while ((ligne = in.readLine()) != null && !ligne.isEmpty()) {
                System.out.println(ligne);
                int idx = ligne.indexOf(':');
                if (idx > 0 && ligne.substring(0, idx).trim().equalsIgnoreCase("Host")) {
                    host = ligne.substring(idx + 1).trim();
                }
            }

            try {
                traiter(premiereLigne, host, out);
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

    private void traiter(String premiereLigne, String host, OutputStream out) throws IOException {
        Matcher m = REQUETE.matcher(premiereLigne);

        if (!m.matches()) {
            envoyerErreur(out, 400);
            return;
        }

        String methode = m.group(1);
        String cheminBrut = m.group(2);

        if (!methode.equals("GET")) {
            envoyerErreur(out, 405);
            return;
        }

        if (host == null || host.isEmpty()) {
            envoyerErreur(out, 400);
            return;
        }

        int posPort = host.indexOf(':');
        String nomHote = (posPort >= 0 ? host.substring(0, posPort) : host).toLowerCase();

        Path racine = SITES.get(nomHote);
        if (racine == null) {
            envoyerErreur(out, 404);
            return;
        }

        if (cheminBrut.contains("?")) {
            cheminBrut = cheminBrut.substring(0, cheminBrut.indexOf("?"));
        }

        String cheminDecode = URLDecoder.decode(cheminBrut, StandardCharsets.UTF_8);

        Path fichier = racine.resolve("." + cheminDecode).normalize();

        if (Files.isDirectory(fichier)) {
            fichier = fichier.resolve("index.html");
        }

        if (!fichier.startsWith(racine) || !Files.isRegularFile(fichier)) {
            envoyerErreur(out, 404);
            return;
        }

        byte[] contenuFichier = Files.readAllBytes(fichier);

        out.write(ReponseHTTP.genererEntete(200, contenuFichier.length).getBytes(StandardCharsets.UTF_8));
        out.write(contenuFichier);
        out.flush();
    }

    private void envoyerErreur(OutputStream out, int code) throws IOException {
        out.write(ReponseHTTP.genererReponseErreur(code).getBytes(StandardCharsets.UTF_8));
        out.flush();
    }

    private static Path trouverRacine(String nom) {
        String[] candidats = { nom, "TP3/" + nom, "../" + nom };
        for (String c : candidats) {
            Path p = Paths.get(c).toAbsolutePath().normalize();
            if (Files.isDirectory(p)) {
                return p;
            }
        }
        return Paths.get(nom).toAbsolutePath().normalize();
    }
}