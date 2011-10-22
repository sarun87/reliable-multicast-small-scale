/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package p2mp;

import java.util.ArrayList;


public class DataRepository {
    public static boolean FILE_TRANSFER_COMPLETE = false;
    public static int RTT  = 500;//in milliseconds
    public static final int DATAPACKET = 0x5555;
    public static final int ACKPACKET = 0xAAAA;
    
    public static int MSS = -1;
    public static int WINDOWSIZE = -1;
    public static int NUMBER_OF_RECEIVERS = -1;
    private static int nextSequenceNumber = -1;
    public static int SENDER_PORT_NUMBER = -1;
    
    public static ArrayList<String> serverIPs;
    public static String fileName;
       
    public static int getNextSequenceNumber(){
        return (int)((nextSequenceNumber++) % Math.pow(2, 32));
    }
    // Initialize the number of senders - public now (not required)
    public static void setNumberOfSenders(int numberOfReceivers) {
		NUMBER_OF_RECEIVERS = numberOfReceivers;
	}
    // Initialize the windowSize- public now (not required)
 	public static void setWindowSize(int windowSize) {
 		WINDOWSIZE = windowSize;
 	}
 	// Set MSS value- public now (not required)
 	public static void setMSS(int maxSegSize){
 		MSS = maxSegSize;
 	}
 	// Set the Port Number- public now (not required)
 	public static void setPortNumber(int port){
 		SENDER_PORT_NUMBER = port;
 	}
}
