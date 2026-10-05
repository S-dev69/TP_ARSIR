package exo1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class ClientFTP {
    private static final String HOST = "localhost";
    private static final int PORT = 2121;

    public static void main(String[] args) {
        try (
                Socket socket = new Socket(HOST, PORT);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                Scanner clavier = new Scanner(System.in)
        ) {
            Thread reader = new Thread(() -> {
                try {
                    String response;
                    while ((response = in.readLine()) != null) {
                        System.out.println(response);
                        if (response.startsWith("221")) {
                            System.exit(0);
                        }
                    }
                } catch (IOException e) {
                    System.out.println("Connexion au serveur perdue.");
                }
            });
            reader.start();

            // thread principal
            while (clavier.hasNextLine()) {
                String command = clavier.nextLine();
                out.println(command);
                if (command.equalsIgnoreCase("QUIT")) {
                    break;
                }
            }

        } catch (IOException e) {
            System.err.println("Erreur client : " + e.getMessage());
        }
    }
}