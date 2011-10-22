/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package p2mp;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public class SlidingWindow {

    public static int StartingSeqNumber = 0;
    public static int EndingSeqNumber = 0;
    public static HashMap<Integer, Segment> Window = new HashMap<Integer, Segment>(DataRepository.WINDOWSIZE);
    
    ///////////// Acknowledgment handling ////////////

 	// Ack's the segment that is got from the receiver and checks if triple dup
 	// has happened.
 	// If it has, it return's a true else it returns a false.
 	public static boolean setAck(int sequenceNumber, int receiver) {
 		// Increment the number of ack's received.
 		// Return the now. of ack finished.
 		if (sequenceNumber > -1 && sequenceNumber < DataRepository.WINDOWSIZE) {
 			Segment result = Window.get(sequenceNumber);
 			result.ReceiverAckList[receiver] =result.ReceiverAckList[receiver] +1;
 			// @ToCheck Replace the entire thing? Check if the value of hash map be changed
 			// without replace of entire segment.
 			// Window.remove(sequenceNumber);
 			Window.put(sequenceNumber, result);
 		
 			/// Cumulative ack logic. (Ack all the previous ack's if this ack is bigger..
 			Integer[] keys = (Window.keySet()).toArray(new Integer[0]);
 			for(int i = 0; i<keys.length; ++i){
 				Segment value = Window.get(keys[i]);
 				if(keys[i] < sequenceNumber && value.ReceiverAckList[receiver] == 0){
 					value.ReceiverAckList[receiver] = 1;
 					Window.remove(keys[i]);
 					Window.put(keys[i],value);
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
 		if ((Window.get(sequenceNumber)).ReceiverAckList[receiver] == 1)
 			return true;
 		else
 			return false;
 	}

 	// Check if a sequence has been acked by all the receivers.
 	public static boolean checkIfAckCompletedByAllRecievers(int sequenceNumber) {
 		boolean result = true;
 		for (int i = 0; i < DataRepository.NUMBER_OF_RECEIVERS; ++i) {
 			if ((Window.get(sequenceNumber)).ReceiverAckList[i] == 0) {
 				result = false;
 				break;
 			}
 		}
 		return result;
 	}

 	// Check if a given receiver has acked thrice.
 	public static boolean checkForTripleDuplicate(int sequenceNumber, int receiver) {
 		if ((Window.get(sequenceNumber)).ReceiverAckList[receiver] == 3)
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
 		Window.remove(sequenceNumber);
 	}
 	
 	public static void printWindow(){
 		System.out.println("---- Ack Window -----");
 		Set<Map.Entry<Integer, Segment>> tempSet = Window.entrySet();
 		Iterator<Entry<Integer, Segment>> iterator = tempSet.iterator();
 		while (iterator.hasNext()) {
 			Map.Entry<Integer, Segment> entry = (Entry<Integer, Segment>) iterator.next();
 			System.out.print("Seq:"+entry.getKey()+" Ack Status:");
 			int[] temp = (entry.getValue()).ReceiverAckList;
 			for(int i =0; i< DataRepository.NUMBER_OF_RECEIVERS; ++i){
 				System.out.print("Sender "+(i+1)+":"+temp[i] + "  ");
 			} 
 			System.out.println("");
 		}
 		System.out.println("---------------------");
 	}
}
