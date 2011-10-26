package p2mp;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

class UDPClient {

    public static void main(String args[]) {
        try {
            initDataRepository(args);
            DatagramSender ds = DatagramSender.getInstance();

            Thread networkListnerThread = new Thread(new NetworkListner());
            networkListnerThread.start();
            Thread ackListnerThread = new Thread(new AckListner());
            ackListnerThread.start();

            for (int i = 0; i < DataRepository.WINDOWSIZE; i++) {
                try {
                    Datagram tempDg = ds.TransmitNextSegment();
                    Segment tempSeg = new Segment(tempDg);
                    SlidingWindow.addItemToWindow(tempDg.getSequenceNumber(), tempSeg);
                } catch (IOException ex) {
                    Logger.getLogger(UDPClient.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            //Thread timerThread = new Thread(new Timer());
            //timerThread.start();

            while (!DataRepository.FILE_TRANSFER_COMPLETE) {
                //do nothing
            }
            //timerThread.join();
            ackListnerThread.join();
            networkListnerThread.join();
        } catch (InterruptedException ex) {
            Logger.getLogger(UDPClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void initDataRepository(String args[]) {
        try {
            // Initialize the parameters given using the command-line
            // Set the Number of Receivers, Window Size and Number of
            // receivers in the DataRepository.
            DataRepository.serverIPs = new HashMap<String, Integer>();
            DataRepository.SENDER_PORT_NUMBER = Integer.parseInt(args[0]);
            int i = 0;
            for (i = 1; args[i].contains(".") == true && args[i].contains(".txt") == false; ++i) {
                DataRepository.serverIPs.put(args[i].toString(), i - 1);
            }
            DataRepository.NUMBER_OF_RECEIVERS = i - 1;
            DataRepository.clientSocket = new DatagramSocket();

            DataRepository.fileName = args[i];
            DataRepository.WINDOWSIZE = Integer.parseInt(args[++i]);
            DataRepository.MSS = Integer.parseInt(args[++i]);

            DataRepository.AckQueue = new LinkedList<Datagram>();
        } catch (SocketException ex) {
            Logger.getLogger(UDPClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
