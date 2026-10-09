package exo5;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ReponseHTTP {
    private static final String VERSION = "HTTP/1.1";
    private static final String NOM_SERVEUR = "MonServeurJava";

    // table de correspondance code -> message
    private static final Map<Integer, String> MESSAGES = new HashMap<>();
    static {
        MESSAGES.put(200, "OK");
        MESSAGES.put(400, "Bad Request");
        MESSAGES.put(404, "Not Found");
        MESSAGES.put(405, "Method Not Allowed");
        MESSAGES.put(500, "Internal Server Error");
    }

    // Q1 : en-tête avec uniquement le code
    public static String genererEntete(int code) {
        return construireEntete(code, -1);
    }

    // Q1 : en-tête avec code + taille du contenu (surcharge)
    public static String genererEntete(int code, int taille) {
        return construireEntete(code, taille);
    }

    private static String construireEntete(int code, int taille) {
        // code inconnu -> 500
        if (!MESSAGES.containsKey(code)) {
            code = 500;
        }

        // ex : lun., 24 nov. 2025 09:45:39 CET
        SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.FRANCE);
        String date = format.format(new Date());

        StringBuilder sb = new StringBuilder();
        sb.append(VERSION).append(" ").append(code).append(" ").append(MESSAGES.get(code)).append("\r\n");
        sb.append("Date: ").append(date).append("\r\n");
        sb.append("Server: ").append(NOM_SERVEUR).append("\r\n");
        sb.append("Connection: close\r\n");
        if (taille >= 0) {
            sb.append("Content-Length: ").append(taille).append("\r\n");
        }
        sb.append("Content-Type: text/html\r\n");
        sb.append("\r\n"); // ligne vide qui termine l'en-tête
        return sb.toString();
    }

    // Q2 : réponse complète en cas d'erreur (en-tête + message + ligne vide)
    public static String genererReponseErreur(int code) {
        if (!MESSAGES.containsKey(code)) {
            code = 500;
        }
        // message adapté : code + message associé, suivi d'une ligne vide
        String corps = "<html><body><h1>" + code + " " + MESSAGES.get(code)
                + "</h1></body></html>\r\n";
        int taille = corps.getBytes(StandardCharsets.UTF_8).length; // taille en octets

        return genererEntete(code, taille) + corps;
    }
}