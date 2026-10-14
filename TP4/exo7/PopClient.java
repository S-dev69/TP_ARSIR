import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class PopClient {
    public static void main(String[] args) {
        String server = "localhost";
        int port = 110;

        String user = "soufiane";

        Scanner scanner = new Scanner(System.in);
        System.out.print("Entrez le mot de passe Linux pour l'utilisateur " + user + " : ");
        String password = scanner.nextLine();

        System.out.println("--- Début de la connexion POP3 ---");

        try (Socket socket = new Socket(server, port);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {

            System.out.println("Serveur : " + reader.readLine());

            sendCommand(writer, "USER " + user);
            System.out.println("Serveur : " + reader.readLine());

            sendCommand(writer, "PASS " + password);
            System.out.println("Serveur : " + reader.readLine());

            // vérification du nombre de messages
            sendCommand(writer, "STAT");
            String statResponse = reader.readLine();
            System.out.println("Serveur : " + statResponse);

            int numMessages = 0;
            if (statResponse.startsWith("+OK")) {
                String[] parts = statResponse.split(" ");
                if (parts.length >= 2) {
                    numMessages = Integer.parseInt(parts[1]);
                }
            }

            System.out.println("\nNombre de messages à récupérer : " + numMessages + "\n");

            // récupération et suppression de chaque message
            for (int i = 1; i <= numMessages; i++) {
                sendCommand(writer, "RETR " + i);
                String retrResponse = reader.readLine();
                System.out.println("Serveur : " + retrResponse);

                if (retrResponse.startsWith("+OK")) {
                    System.out.println("=== Début du message " + i + " ===");
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.equals(".")) {
                            break;
                        }
                        System.out.println(line);
                    }
                    System.out.println("=== Fin du message " + i + " ===\n");

                    sendCommand(writer, "DELE " + i);
                    System.out.println("Serveur : " + reader.readLine());
                }
            }

            sendCommand(writer, "QUIT");
            System.out.println("Serveur : " + reader.readLine());

        } catch (IOException e) {
            System.err.println("Erreur de connexion POP3 : " + e.getMessage());
        }
    }

    private static void sendCommand(PrintWriter writer, String command) {
        System.out.println("Client  : " + command);
        writer.print(command + "\r\n");
        writer.flush();
    }
}