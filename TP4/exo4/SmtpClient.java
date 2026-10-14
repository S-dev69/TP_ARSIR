import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class SmtpClient {
    public static void main(String[] args) {
        Map<String, String> contacts = new HashMap<>();
        contacts.put("Soufiane", "soufiane@localhost");
        contacts.put("Astérix", "asterix@village.fr");
        contacts.put("Soufiane Etudiant", "soufiane.taghiaoui@etu.univ-lyon1.fr");

        String server = "localhost";
        int port = 25;
        String sender = "cesar@rome.it";

        System.out.println("--- Début de la campagne d'envoi SMTP ---");

        for (Map.Entry<String, String> entry : contacts.entrySet()) {
            String nom = entry.getKey();
            String email = entry.getValue();

            System.out.println("\nTentative d'envoi à : " + nom + " (" + email + ")");

            try (Socket socket = new Socket(server, port);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {

                // lecture du message
                System.out.println("Serveur : " + reader.readLine());

                // envoi
                sendCommand(writer, "HELO rome.it", reader);
                sendCommand(writer, "MAIL FROM: <" + sender + ">", reader);
                sendCommand(writer, "RCPT TO: <" + email + ">", reader);
                sendCommand(writer, "DATA", reader);

                // corps
                writer.print("Subject: Salutations automatisees\r\n");
                writer.print("\r\n");
                writer.print("Bonjour " + nom + " !\r\n");
                writer.print(".\r\n");
                writer.flush();

                System.out.println("Client  : [Envoi du corps du mail et du point final]");
                System.out.println("Serveur : " + reader.readLine());

                sendCommand(writer, "QUIT", reader);

            } catch (IOException e) {
                System.err.println("Erreur de connexion pour " + nom + " : " + e.getMessage());
            }
        }
    }

    private static void sendCommand(PrintWriter writer, String command, BufferedReader reader) throws IOException {
        System.out.println("Client  : " + command);
        writer.print(command + "\r\n");
        writer.flush();
        System.out.println("Serveur : " + reader.readLine());
    }
}