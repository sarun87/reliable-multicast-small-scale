/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package p2mp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Shyam
 */
public class DatagramSender {

    File myFileTOSend;
    FileInputStream myFileInputStream;
    private DatagramSender mySender;
    private DatagramSocket clientSocket;

    public DatagramSender getInstance() {
        if (mySender == null) {
            try {
                mySender = new DatagramSender();
                clientSocket = new DatagramSocket();
                
            } catch (SocketException ex) {
                Logger.getLogger(DatagramSender.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return mySender;
    }

    private DatagramSender() {
        try {
            myFileTOSend = new File(DataRepository.fileName);
            myFileInputStream = new FileInputStream(myFileTOSend);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(DatagramSender.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public boolean TransmitNextSegment() throws IOException {
        byte[] dataBytes = new byte[DataRepository.MSS];
        int dataSize = myFileInputStream.read(dataBytes);
        if (dataSize > 0) {
            Datagram tempDatagram = new Datagram(dataBytes, dataSize, true);
            sendDatagram(tempDatagram);
            return true;
        }
        return false;//if the file is completely read.
    }

    public void Retransmit(int sequenceNumber) {

    }

    private void sendDatagram(Datagram dataGram) {
        for (Iterator it = DataRepository.serverIPs.keySet().iterator(); it.hasNext();) {
            try {
                String ipAddr = (String) it.next();
                DatagramPacket sendPacket = new DatagramPacket(dataGram.getBytes(), dataGram.Length, InetAddress.getByName(ipAddr), DataRepository.SENDER_PORT_NUMBER);
                clientSocket.send(sendPacket);
            } catch (Exception ex) {
                Logger.getLogger(DatagramSender.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
