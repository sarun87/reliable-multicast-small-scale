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
   // public static int EndingSeqNumber = 0;
    public static HashMap<Integer, Datagram> Window = new HashMap<Integer, Datagram>(DataRepository.WINDOWSIZE);
    
    // Added segment to Sliding window
    public static boolean addItemToWindow(int sequenceNumber,Datagram info){
//    	if(EndingSeqNumber+1 - StartingSeqNumber > DataRepository.WINDOWSIZE){
//    		return false;
//    	}
    	Window.put(sequenceNumber, info);
//    	EndingSeqNumber = EndingSeqNumber + 1;
    	return true;   	
    }
    
    // Remove segment from sliding window
    public static boolean removeItemFromWindow(int sequenceNumber){
    	if(sequenceNumber >=StartingSeqNumber && sequenceNumber < StartingSeqNumber + DataRepository.WINDOWSIZE){
    		Window.remove(sequenceNumber);
    		StartingSeqNumber = StartingSeqNumber + 1;
    		return true;
    	}
    	return false;
    }
    
   
 	public static void printWindow(){
 		System.out.println("---- Ack Window -----");
 		Set<Map.Entry<Integer, Datagram>> tempSet = Window.entrySet();
 		Iterator<Entry<Integer, Datagram>> iterator = tempSet.iterator();
 		while (iterator.hasNext()) {
 			Map.Entry<Integer, Datagram> entry = (Entry<Integer, Datagram>) iterator.next();
 			System.out.println("Segment:"+entry.getKey() +" present in window");
 		}
 		System.out.println("---------------------");
 	}
}
