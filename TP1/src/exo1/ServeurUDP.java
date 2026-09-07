package exo1;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class ServeurUDP {
    private static final int PORT = 6666;
    private static final int BUFFER_SIZE = 1024;

    public static void main(String[] args) {
        System.out.println("Serveur UDP en écoute sur le port " + PORT + "...");

        // On ouvre la socket liée au port 6666
        try (DatagramSocket serverSocket = new DatagramSocket(PORT)) {
            byte[] receiveData = new byte[BUFFER_SIZE];

            while (true) { // Boucle d'écoute continue
                // 1. Réception du paquet client
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                serverSocket.receive(receivePacket);

                String clientMessage = new String(
                        receivePacket.getData(),
                        0,
                        receivePacket.getLength(),
                        StandardCharsets.UTF_8
                );

                // Récupération de l'adresse et du port de l'expéditeur
                InetAddress clientAddress = receivePacket.getAddress();
                int clientPort = receivePacket.getPort();
                System.out.println("Demande d'heure reçue de [" + clientAddress + ":" + clientPort + "]");

                // 2. Génération de l'heure courante
                LocalTime heureActuelle = LocalTime.now();
                DateTimeFormatter format = DateTimeFormatter.ofPattern("HH:mm:ss");

                // 2. Préparation et envoi de la réponse
                String replyMessage = "Heure du serveur : " + heureActuelle.format(format);
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