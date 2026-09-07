package exo1;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

public class ClientUDP {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 6666;
    private static final int BUFFER_SIZE = 1024;

    public static void main(String[] args) {
        // try-with-resources libère automatiquement la socket à la fin du bloc
        try (DatagramSocket clientSocket = new DatagramSocket()) {

            // 1. Préparation et envoi du message
            InetAddress serverAddress = InetAddress.getByName(SERVER_HOST);
            String message = "Quelle heure est-il ?";
            byte[] sendData = message.getBytes(StandardCharsets.UTF_8);

            DatagramPacket sendPacket = new DatagramPacket(
                    sendData,
                    sendData.length,
                    serverAddress,
                    SERVER_PORT
            );

            clientSocket.send(sendPacket);
            System.out.println("Message envoyé au serveur : " + message);

            // 2. Attente de la réponse
            byte[] receiveData = new byte[BUFFER_SIZE];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

            clientSocket.receive(receivePacket); // Bloquant jusqu'à réception

            // 3. Traitement de la réponse
            String response = new String(
                    receivePacket.getData(),
                    0,
                    receivePacket.getLength(),
                    StandardCharsets.UTF_8
            );
            System.out.println("Réponse reçue du serveur : " + response);

        } catch (Exception e) {
            System.err.println("Erreur client : " + e.getMessage());
        }
    }
}