/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package p2mp;

import java.net.DatagramSocket;
import java.util.HashMap;
import java.util.Queue;

public class DataRepository {


    public static boolean FILE_TRANSFER_COMPLETE = false;
    public static boolean WAITING_FOR_LAST_ACK = false;
    public static boolean LAST_DATAPACKET_SENT = false;
    public static int LAST_DATAPACKET_SEQNO = -2;
    
    public static int RTT = 500;//in milliseconds
    public static final char DATAPACKET = (char)(0x5555);
    public static final char ACKPACKET = (char)(0xAAAA);
    public static final int HEADER_SIZE = 12;
    
    public static int MSS = -1;
    public static int WINDOWSIZE = -1;
    public static int NUMBER_OF_RECEIVERS = -1;
    public static int SENDER_PORT_NUMBER = -1;
    
    public static HashMap<String, Integer> serverIPs;
    public static DatagramSocket clientSocket;
    public static String fileName;
    
    public static Queue<Datagram> AckQueue;// queue to hold the received acks - processed later by the ackreceiver

    private static int nextSequenceNumber = -1;
    public static int getNextSequenceNumber() {
        return (int) ((++nextSequenceNumber) % Math.pow(2, 32));
    }
}
