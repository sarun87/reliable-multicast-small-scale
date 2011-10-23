/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package p2mp;

import java.util.HashMap;


public class DataRepository {
    private static int nextSequenceNumber = -1;
    
    public static boolean FILE_TRANSFER_COMPLETE = false;
    public static int RTT  = 500;//in milliseconds
    public static final int DATAPACKET = 0x5555;
    public static final int ACKPACKET = 0xAAAA;
    
    public static int MSS = -1;
    public static int WINDOWSIZE = -1;
    public static int NUMBER_OF_RECEIVERS = -1;
    public static int SENDER_PORT_NUMBER = -1;
    public static HashMap<String, Integer> serverIPs;
    public static String fileName;
    
    public static int getNextSequenceNumber(){
        return (int)((nextSequenceNumber++) % Math.pow(2, 32));
    }
    
}
