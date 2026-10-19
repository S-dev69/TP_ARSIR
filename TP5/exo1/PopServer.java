import java.io.*;
import java.net.*;
import java.util.*;

public class PopServer {
    // stockage des mdp
    private static final Map<String, String> users = new HashMap<>();
    static {
        users.put("asterix", "potion");
        users.put("cesar", "empire");
    }

    public static void main(String[] args) {
        int port = 1110;
        System.out.println("Démarrage du serveur POP3 sur le port " + port + "...");

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Nouveau client connecté : " + clientSocket.getInetAddress());
                new Thread(() -> handleClient(clientSocket)).start();
            }
        } catch (IOException e) {
            System.err.println("Erreur du serveur : " + e.getMessage());
        }
    }

    private static void handleClient(Socket socket) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {

            writer.println("+OK Serveur POP3 prêt");

            String currentUser = null;
            boolean isAuthenticated = false;

            // variables boîte mail
            List<File> messages = new ArrayList<>();
            Set<Integer> markedForDeletion = new HashSet<>();

            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("Reçu : " + line);
                String[] parts = line.split(" ", 2);
                String command = parts[0].toUpperCase();
                String argument = parts.length > 1 ? parts[1] : "";

                switch (command) {
                    case "USER":
                        if (users.containsKey(argument)) {
                            currentUser = argument;
                            writer.println("+OK Utilisateur trouvé");
                        } else {
                            writer.println("-ERR Utilisateur inconnu");
                        }
                        break;

                    case "PASS":
                        if (currentUser != null && argument.equals(users.get(currentUser))) {
                            isAuthenticated = true;
                            // charge les mails
                            File userDir = new File("TP5/exo1/mailboxes/" + currentUser);
                            if (userDir.exists() && userDir.isDirectory()) {
                                File[] files = userDir.listFiles();
                                if (files != null) {
                                    messages = new ArrayList<>(Arrays.asList(files));
                                    messages.sort(Comparator.comparing(File::getName));
                                }
                            } else {
                                userDir.mkdirs();
                            }
                            markedForDeletion.clear();
                            writer.println("+OK Authentification réussie pour " + currentUser);
                        } else {
                            writer.println("-ERR Mot de passe incorrect");
                        }
                        break;

                    case "STAT":
                        if (!isAuthenticated) { writer.println("-ERR Non authentifié"); break; }
                        int count = 0;
                        long totalSize = 0;
                        for (int i = 0; i < messages.size(); i++) {
                            if (!markedForDeletion.contains(i)) {
                                count++;
                                totalSize += messages.get(i).length();
                            }
                        }
                        writer.println("+OK " + count + " " + totalSize);
                        break;

                    case "LIST":
                        if (!isAuthenticated) { writer.println("-ERR Non authentifié"); break; }
                        writer.println("+OK Liste des messages");
                        for (int i = 0; i < messages.size(); i++) {
                            if (!markedForDeletion.contains(i)) {
                                writer.println((i + 1) + " " + messages.get(i).length());
                            }
                        }
                        writer.println(".");
                        break;

                    case "RETR":
                        if (!isAuthenticated) { writer.println("-ERR Non authentifié"); break; }
                        try {
                            int msgNum = Integer.parseInt(argument) - 1;
                            if (msgNum >= 0 && msgNum < messages.size() && !markedForDeletion.contains(msgNum)) {
                                File msgFile = messages.get(msgNum);
                                writer.println("+OK " + msgFile.length() + " octets");
                                // envoi du contenu
                                try (BufferedReader fileReader = new BufferedReader(new FileReader(msgFile))) {
                                    String fileLine;
                                    while ((fileLine = fileReader.readLine()) != null) {
                                        writer.println(fileLine);
                                    }
                                }
                                writer.println(".");
                            } else {
                                writer.println("-ERR Message invalide");
                            }
                        } catch (NumberFormatException e) {
                            writer.println("-ERR Argument invalide");
                        }
                        break;

                    case "DELE":
                        if (!isAuthenticated) { writer.println("-ERR Non authentifié"); break; }
                        try {
                            int msgNum = Integer.parseInt(argument) - 1;
                            if (msgNum >= 0 && msgNum < messages.size() && !markedForDeletion.contains(msgNum)) {
                                markedForDeletion.add(msgNum);
                                writer.println("+OK Message " + (msgNum + 1) + " marqué pour suppression");
                            } else {
                                writer.println("-ERR Message invalide");
                            }
                        } catch (NumberFormatException e) {
                            writer.println("-ERR Argument invalide");
                        }
                        break;

                    case "QUIT":
                        // suppression physique
                        if (isAuthenticated) {
                            for (int msgIndex : markedForDeletion) {
                                File file = messages.get(msgIndex);
                                if (file.exists()) {
                                    file.delete();
                                }
                            }
                        }
                        writer.println("+OK Au revoir");
                        socket.close();
                        return;

                    default:
                        if (!isAuthenticated) {
                            writer.println("-ERR Veuillez vous authentifier d'abord");
                        } else {
                            writer.println("-ERR Commande non implémentée");
                        }
                        break;
                }
            }
        } catch (IOException e) {
            System.err.println("Erreur avec le client : " + e.getMessage());
        }
    }
}