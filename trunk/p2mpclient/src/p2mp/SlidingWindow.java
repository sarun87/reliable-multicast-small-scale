/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package p2mp;

import java.util.HashMap;

public class SlidingWindow {

	public static int StartingSeqNumber = 0;
	public static int EndingSeqNumber = -1;
	public static Segment previousSegment;
	public static HashMap<Integer, Segment> Window = new HashMap<Integer, Segment>(
			DataRepository.WINDOWSIZE);

	// Added segment to Sliding window
	public static synchronized boolean addItemToWindow(int sequenceNumber,
			Segment info) {
		if (EndingSeqNumber - StartingSeqNumber > DataRepository.WINDOWSIZE) {
			return false;
		}

		if (!Window.containsKey(sequenceNumber)) {
			EndingSeqNumber = sequenceNumber;
		}
		Window.put(sequenceNumber, info);
		// EndingSeqNumber = EndingSeqNumber + 1;

		return true;
	}

	// Remove segment from sliding window
	public static synchronized boolean removeItemFromWindow(int sequenceNumber) {
		if (sequenceNumber >= StartingSeqNumber
				&& sequenceNumber <= EndingSeqNumber) {
			StartingSeqNumber = StartingSeqNumber + 1;
			previousSegment = Window.get(sequenceNumber);
			Window.remove(sequenceNumber);
			return true;
		}
		return false;
	}

	// /////////// Acknowledgment handling ////////////
	// Ack's the segment that is got from the receiver and checks if triple dup
	// has happened.
	// If it has, it return's a true else it returns a false.
	public static synchronized boolean setAck(int sequenceNumber, int receiver) {
		// Increment the number of ack's received.
		// Return the now. of ack finished.
		boolean result = false;
		if (sequenceNumber >= StartingSeqNumber
				&& sequenceNumber < StartingSeqNumber
						+ DataRepository.WINDOWSIZE) {
			Segment resultSegment = Window.get(sequenceNumber);
			/*
			 * System.out.println("Start:" + StartingSeqNumber + " End:" +
			 * EndingSeqNumber + " seq:" + sequenceNumber);
			 */
			resultSegment.ReceiverAckList[receiver] = resultSegment.ReceiverAckList[receiver] + 1;
			Window.put(sequenceNumber, resultSegment);

			// / Cumulative ack logic. (Ack all the previous ack's if this ack
			// is bigger..
			Integer[] keys = (Window.keySet()).toArray(new Integer[0]);
			for (int i = 0; i < keys.length; ++i) {
				Segment value = Window.get(keys[i]);
				if (keys[i] < sequenceNumber
						&& value.ReceiverAckList[receiver] == 0) {
					value.ReceiverAckList[receiver] = 1;
					Window.remove(keys[i]);
					Window.put(keys[i], value);
				}
			}
			result = checkForTripleDuplicate(sequenceNumber, receiver);
		} else if (sequenceNumber == StartingSeqNumber - 1) {
			previousSegment.ReceiverAckList[receiver] = previousSegment.ReceiverAckList[receiver] + 1;
			result = checkForTripleDuplicate(sequenceNumber, receiver);
		}
		return result;
	}

	// Check if an ack is received by a given particular receiver
	public static synchronized boolean checkAckCompletedByReciever(
			int sequenceNumber, int receiver) {
		if (sequenceNumber < StartingSeqNumber) {
			return true;
		}
		if ((Window.get(sequenceNumber)).ReceiverAckList[receiver] >= 1) {
			return true;
		} else {
			return false;
		}
	}

	// Check if a sequence has been acked by all the receivers.
	public static synchronized boolean checkIfAckCompletedByAllRecievers(
			int sequenceNumber) {
		boolean result = true;
		for (int i = 0; i < DataRepository.NUMBER_OF_RECEIVERS; ++i) {
			if ((Window.get(sequenceNumber)).ReceiverAckList[i] == 0) {
				result = false;
				break;
			}
		}
		return result;
	}

	// Check if a given receiver has acked thrice for a given sequence number.
	public static synchronized boolean checkForTripleDuplicate(
			int sequenceNumber, int receiver) {
		Segment segmentToCheck;
		if (sequenceNumber == StartingSeqNumber - 1) {
			segmentToCheck = previousSegment;
		} else {
			segmentToCheck = Window.get(sequenceNumber);
		}
		if (segmentToCheck.ReceiverAckList[receiver] == 3) {
			return true;
		} else {
			return false;
		}

	}
}
