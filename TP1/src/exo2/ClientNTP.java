package exo2;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

public class ClientNTP {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 6666;
    private static final int BUFFER_SIZE = 1024;

    public static void main(String[] args) {
        try (DatagramSocket clientSocket = new DatagramSocket()) {

            // T1 (heure d'envoi)
            long T1 = System.currentTimeMillis();

            InetAddress serverAddress = InetAddress.getByName(SERVER_HOST);
            String message = String.valueOf(T1);
            byte[] sendData = message.getBytes(StandardCharsets.UTF_8);

            DatagramPacket sendPacket = new DatagramPacket(
                    sendData,
                    sendData.length,
                    serverAddress,
                    SERVER_PORT
            );

            // Envoi au serveur
            clientSocket.send(sendPacket);
            System.out.println("Requête envoyée au serveur (T1 = " + T1 + ")");

            byte[] receiveData = new byte[BUFFER_SIZE];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

            clientSocket.receive(receivePacket);

            // T2 (heure de réception)
            long T2 = System.currentTimeMillis();

            String response = new String(
                    receivePacket.getData(),
                    0,
                    receivePacket.getLength(),
                    StandardCharsets.UTF_8
            );

            // Parsing de la réponse
            String[] temps = response.trim().split(",");
            long T1_echo = Long.parseLong(temps[0]);
            long T1_prime = Long.parseLong(temps[1]);
            long T2_prime = Long.parseLong(temps[2]);

            // Calcul du délai de transmission (delta)
            long delta = (T2 - T1) - (T2_prime - T1_prime);

            // Calcul de l'écart entre les horloges (theta)
            long theta = (T1_prime + T2_prime) / 2 - (T1 + T2) / 2;

            // Affichage
            System.out.println("Délai de transmission (delta) : " + delta + " ms");
            System.out.println("Écart entre les horloges (theta) : " + theta + " ms");

        } catch (Exception e) {
            System.err.println("Erreur client : " + e.getMessage());
        }
    }
}