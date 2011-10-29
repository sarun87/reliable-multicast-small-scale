/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package p2mp;

import java.net.DatagramPacket;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Shyam
 */
public class NetworkListner implements Runnable {

    private Lock myLock;

    private void listenForAckForever() {
        DatagramPacket receivePacket;
        Datagram ackReceived;
        byte[] receiveData = new byte[DataRepository.MSS + DataRepository.HEADER_SIZE];
        while (!DataRepository.FILE_TRANSFER_COMPLETE) {
            try {
                myLock.lock();
                receivePacket = new DatagramPacket(receiveData, receiveData.length);
                DataRepository.clientSocket.receive(receivePacket);
                ackReceived = new Datagram(receivePacket.getData());
                String ipAddr = (receivePacket.getAddress().toString()).replace("/", "");
                ackReceived.ServerNumber = DataRepository.serverIPs.get(ipAddr);
                System.out.println("Ack received and queued -> seqNo: " + ackReceived.getSequenceNumber() + " from: " + ackReceived.ServerNumber);
                DataRepository.AckQueue.add(ackReceived);
            } catch (Exception ex) {
                Logger.getLogger(NetworkListner.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                myLock.unlock();
            }
        }
    }

    @Override
    public void run() {
        myLock = new ReentrantLock();
        listenForAckForever();
    }
}
