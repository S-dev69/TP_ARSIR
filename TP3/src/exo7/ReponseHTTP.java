package exo7;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ReponseHTTP {
    private static final String VERSION = "HTTP/1.1";
    private static final String NOM_SERVEUR = "MonServeurJava";

    private static final Map<Integer, String> MESSAGES = new HashMap<>();
    static {
        MESSAGES.put(200, "OK");
        MESSAGES.put(400, "Bad Request");
        MESSAGES.put(404, "Not Found");
        MESSAGES.put(405, "Method Not Allowed");
        MESSAGES.put(500, "Internal Server Error");
    }

    public static String genererEntete(int code) {
        return construireEntete(code, -1);
    }

    public static String genererEntete(int code, int taille) {
        return construireEntete(code, taille);
    }

    private static String construireEntete(int code, int taille) {
        if (!MESSAGES.containsKey(code)) {
            code = 500;
        }

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
        sb.append("\r\n");
        return sb.toString();
    }

    public static String genererReponseErreur(int code) {
        if (!MESSAGES.containsKey(code)) {
            code = 500;
        }

        String corps = "<html><body><h1>" + code + " " + MESSAGES.get(code)
                + "</h1></body></html>\r\n";
        int taille = corps.getBytes(StandardCharsets.UTF_8).length;

        return genererEntete(code, taille) + corps;
    }
}