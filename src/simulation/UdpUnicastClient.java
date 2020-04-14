package simulation;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class UdpUnicastClient implements Runnable {
    private final int port;
    
    public UdpUnicastClient(int port) {
        this.port = port;
    }
    
    @Override
    public void run() {
        try(DatagramSocket clientSocket = new DatagramSocket(port)) {
            while(true) {
                byte[]         buffer         = new byte[65507];
                DatagramPacket datagramPacket = new DatagramPacket(buffer, 0, buffer.length);
                clientSocket.receive(datagramPacket);
                
                String receivedMessage = new String(datagramPacket.getData());
                MessageProcessing.processMessage(receivedMessage);
            }
        } catch(SocketException e) {
            e.printStackTrace();
        } catch(IOException e) {
            System.out.println("Timeout. Client is closing.");
        }
    }
}
