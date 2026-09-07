package exo2;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

public class ServeurNTP {
    private static final int PORT = 6666;
    private static final int BUFFER_SIZE = 1024;

    public static void main(String[] args) {
        System.out.println("Serveur NTP en écoute sur le port " + PORT + "...");

        // On ouvre la socket liée au port 6666 avec try-with-resources
        try (DatagramSocket serverSocket = new DatagramSocket(PORT)) {
            byte[] receiveData = new byte[BUFFER_SIZE];

            while (true) {
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                serverSocket.receive(receivePacket);

                // T'1 (heure de réception)
                long T1_prime = System.currentTimeMillis();

                String clientMessage = new String(
                        receivePacket.getData(),
                        0,
                        receivePacket.getLength(),
                        StandardCharsets.UTF_8
                );

                // Récupération de T1
                long T1 = Long.parseLong(clientMessage.trim());

                // Récupération de l'adresse et du port de l'expéditeur
                InetAddress clientAddress = receivePacket.getAddress();
                int clientPort = receivePacket.getPort();
                System.out.println("Requête de synchronisation reçue de [" + clientAddress + ":" + clientPort + "]");

                // T'2 (heure d'envoi)
                long T2_prime = System.currentTimeMillis();

                // Renvoi des timestamps : T1, T'1, T'2
                String replyMessage = T1 + "," + T1_prime + "," + T2_prime;
                byte[] sendData = replyMessage.getBytes(StandardCharsets.UTF_8);

                DatagramPacket sendPacket = new DatagramPacket(
                        sendData,
                        sendData.length,
                        clientAddress,
                        clientPort
                );

                serverSocket.send(sendPacket);
            }

        } catch (Exception e) {
            System.err.println("Erreur serveur : " + e.getMessage());
        }
    }
}