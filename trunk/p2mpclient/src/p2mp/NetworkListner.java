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
        while (!DataRepository.FILE_TRANSFER_COMPLETE) {//todo: while(last ack not received.)
            try {
                myLock.lock();
                receivePacket = new DatagramPacket(receiveData, receiveData.length);
                DataRepository.clientSocket.receive(receivePacket);
                ackReceived = new Datagram(receivePacket.getData());
                String ipAddr = (receivePacket.getAddress().toString()).replace("/", "");
                ackReceived.ServerNumber = DataRepository.serverIPs.get(ipAddr);
                DataRepository.AckQueue.add(ackReceived);
                if(DataRepository.LAST_DATAPACKET_SEQNO == ackReceived.getSequenceNumber()){
                    Thread.sleep(100);
                    DataRepository.WAITING_FOR_LAST_ACK = true;
                }
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
