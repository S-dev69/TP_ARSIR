import java.io.*;
import java.net.*;
import java.util.*;

public class ImapClient {
    public static void main(String[] args) {
        String server = "localhost";
        int port = 143;
        String user = "asterix";
        String pass = "potion";

        try (Socket socket = new Socket(server, port);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {

            System.out.println("S: " + reader.readLine());

            sendCommand(writer, "A1 LOGIN " + user + " " + pass);
            readUntil(reader, "A1");

            sendCommand(writer, "A2 SELECT INBOX");
            readUntil(reader, "A2");

            sendCommand(writer, "A3 SEARCH UNSEEN");
            String searchRes = readUntil(reader, "A3");

            // recup ids
            List<String> unreadMails = parseSearch(searchRes);

            if (unreadMails.isEmpty()) {
                System.out.println("\n--- Aucun message non lu ---");
            } else {
                for (String id : unreadMails) {
                    System.out.println("\n--- Lecture du mail " + id + " ---");

                    sendCommand(writer, "A4 FETCH " + id + " BODY[]");
                    readUntil(reader, "A4");

                    sendCommand(writer, "A5 STORE " + id + " +FLAGS (\\Deleted)");
                    readUntil(reader, "A5");
                }

                System.out.println("\n--- Suppression définitive ---");
                sendCommand(writer, "A6 CLOSE");
                readUntil(reader, "A6");
            }

            sendCommand(writer, "A7 LOGOUT");
            readUntil(reader, "A7");

        } catch (IOException e) {
            System.err.println("Erreur : " + e.getMessage());
        }
    }

    // envoi cmd
    private static void sendCommand(PrintWriter writer, String cmd) {
        System.out.println("C: " + cmd);
        writer.print(cmd + "\r\n");
        writer.flush();
    }

    // lit reponse
    private static String readUntil(BufferedReader reader, String tag) throws IOException {
        StringBuilder fullRes = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println("S: " + line);
            fullRes.append(line).append(" ");
            if (line.startsWith(tag)) break;
        }
        return fullRes.toString();
    }

    // extrait ids
    private static List<String> parseSearch(String response) {
        List<String> ids = new ArrayList<>();
        String[] parts = response.split(" ");
        boolean inSearch = false;
        for (String p : parts) {
            if (p.equals("SEARCH")) inSearch = true;
            else if (inSearch && p.matches("\\d+")) ids.add(p);
            else if (inSearch && p.startsWith("A3")) break;
        }
        return ids;
    }
}