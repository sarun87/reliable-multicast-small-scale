/**
 * 
 */
package p2mp;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;


/**
 * @author arun
 * 
 */
public class P2MPAck {
	public static int WINDOW_SIZE = -1;
	public static int NUMBER_OF_RECEIVERS = -1;


	public static HashMap<Integer, Integer[]> ackMap = new HashMap<Integer,Integer[]>();
	
	// Initialize the windowSize
	public static void setWindowSize(int windowSize) {
		WINDOW_SIZE = windowSize;
	}

	// Initialize the number of senders
	public static void setNumberOfSenders(int numberOfReceivers) {
		NUMBER_OF_RECEIVERS = numberOfReceivers;
	}

	// Initialize the AckMap code. this will create the window size number of
	// entries in the hash map.
	// @note Call this method only after the window size and number of receivers
	// are set.
	public static void buildAckMap() {
		if (WINDOW_SIZE != -1 && NUMBER_OF_RECEIVERS != -1) {
			for (int i = 0; i < WINDOW_SIZE; ++i) {
				addSeqInMap(i);
			}
		}
	}

	// Add the sequence number to the acknowledgement window. (This is nothing
	// but the sliding window.
	// @note Call this method only after the window size and number of receivers
	// are set.
	public static void addSeqInMap(Integer sequenceNumber) {
		Integer []receiverAckArray = new Integer[NUMBER_OF_RECEIVERS];
		for(int i = 0; i < NUMBER_OF_RECEIVERS; ++i){
			receiverAckArray[i] = 0;
		}
		ackMap.put(sequenceNumber, receiverAckArray);
	}

	// Ack's the segment that is got from the receiver and checks if triple dup
	// has happened.
	// If it has, it return's a true else it returns a false.
	public static boolean setAck(int sequenceNumber, int receiver) {
		// Increment the number of ack's received.
		// Return the now. of ack finished.
		if (sequenceNumber > -1 && sequenceNumber < WINDOW_SIZE) {
			Integer resultArray[] = ackMap.get(sequenceNumber);
			resultArray[receiver] =resultArray[receiver] +1;
			ackMap.remove(sequenceNumber);
			ackMap.put(sequenceNumber, resultArray);
		
			/// Cumulative ack logic. (Ack all the previous ack's if this ack is bigger..
			Integer[] keys = (ackMap.keySet()).toArray(new Integer[0]);
			for(int i = 0; i<keys.length; ++i){
				Integer[] value = ackMap.get(keys[i]);
				if(keys[i] < sequenceNumber && value[receiver] == 0){
					value[receiver] = 1;
					ackMap.remove(keys[i]);
					ackMap.put(keys[i],value);
				}
			}
			// ackArray[sequenceNumber][receiver]++;
		}
		boolean result = checkForTripleDuplicate(sequenceNumber, receiver);
		return result;
	}

	// Check if an ack is received by a given particular receiver
	public static boolean checkAckCompletedByReciever(int sequenceNumber,
			int receiver) {
		if (ackMap.get(sequenceNumber)[receiver] == 1)
			return true;
		else
			return false;
	}

	// Check if a sequence has been acked by all the receivers.
	public static boolean checkIfAckCompletedByAllRecievers(int sequenceNumber) {
		boolean result = true;
		for (int i = 0; i < NUMBER_OF_RECEIVERS; ++i) {
			if (ackMap.get(sequenceNumber)[i] == 0) {
				result = false;
				break;
			}
		}
		return result;
	}

	// Check if a given receiver has acked thrice.
	public static boolean checkForTripleDuplicate(int sequenceNumber, int receiver) {
		if (ackMap.get(sequenceNumber)[receiver] == 3)
			return true;
		else
			return false;
	}

	// Call this method when the window is incremented. If window is incremented
	// by more than 1 segment, this method has to be called for each segment.
	// This is used to remove the sequence number from the hashmap. Also, for
	// the
	// new elements in the hash map, the addSeqInMap has to be called.
	public static void deleteAckElementAfterWindowMove(int sequenceNumber) {
		ackMap.remove(sequenceNumber);
	}
	
	public static void printWindow(){
		System.out.println("---- Ack Window -----");
		Set<Map.Entry<Integer, Integer[]>> tempSet = ackMap.entrySet();
		Iterator<Entry<Integer, Integer[]>> iterator = tempSet.iterator();
		while (iterator.hasNext()) {
			Map.Entry<Integer, Integer[]> entry = (Entry<Integer, Integer[]>) iterator.next();
			System.out.print("Seq:"+entry.getKey()+" Ack Status:");
			Integer[] temp = entry.getValue();
			for(int i =0; i< NUMBER_OF_RECEIVERS; ++i){
				System.out.print("Sender "+(i+1)+":"+temp[i] + "  ");
			} 
			System.out.println("");
		}
		System.out.println("---------------------");
	}
	
	// Test code
	public static void main(String []args){
		setWindowSize(3);
		setNumberOfSenders(3);
		buildAckMap();
		setAck(2, 1);
		printWindow();
		setAck(1,1);
		setAck(1,0);
		setAck(1,2);
		setAck(0,1);
		setAck(0,2);
		printWindow();
		System.out.println("check if completed by all receivers:"+checkIfAckCompletedByAllRecievers(1));
		System.out.println("Check if completed by receiver:"+checkAckCompletedByReciever(2, 1));
		setAck(2,1);
		setAck(2,1);
		System.out.println("Triple dup:"+checkForTripleDuplicate(2, 1));
		addSeqInMap(3);
		deleteAckElementAfterWindowMove(0);
		printWindow();
		
	}

}
