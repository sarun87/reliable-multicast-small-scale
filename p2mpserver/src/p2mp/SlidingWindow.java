/** ********************************************************************
 * File:           SlidingWindow.java 
 * Description:    Sliding window that holds the segments present in the
 * 				   window.
 * Authors:        Arun, Shyam, Rahul, Venkatesh 
 * Created:        Sun Oct 23 01:49:09 EST 2011
 *
 * (C) Copyright 2011
 ** Licensed under the GPL License, Version 3.0 (the "License");
 ** you may not use this file except in compliance with the License.
 ** You may obtain a copy of the License at
 ** http://www.gnu.org/licenses/gpl-3.0.txt
 ** Unless required by applicable law or agreed to in writing, software
 ** distributed under the License is distributed on an "AS IS" BASIS,
 ** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 ** See the License for the specific language governing permissions and
 ** limitations under the License.
 *
 **********************************************************************/
package p2mp;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

/**
 * Sliding window code
 *
 */
public class SlidingWindow {

	/**
	 * Hold the sequence number that is the left edge of the window.
	 */
	public static int StartingSeqNumber = 0;

	/**
	 * The Window hashmap contains the sequencenumber mapped with the datagram.
	 */
	public static HashMap<Integer, Datagram> Window = new HashMap<Integer, Datagram>(
			DataRepository.WINDOWSIZE);

	/**
	 * Added segment to Sliding window
	 * @param sequenceNumber
	 * @param info - Datagram
	 * @return true if success, else false
	 */
	public static boolean addItemToWindow(int sequenceNumber, Datagram info) {
		Window.put(sequenceNumber, info);
		return true;
	}

	/**
	 * Remove segment from sliding window
	 * @param sequenceNumber
	 * @return
	 */
	public static boolean removeItemFromWindow(int sequenceNumber) {
		if (sequenceNumber >= StartingSeqNumber
				&& sequenceNumber < StartingSeqNumber
						+ DataRepository.WINDOWSIZE) {
			Window.remove(sequenceNumber);
			// Move sliding window
			StartingSeqNumber = StartingSeqNumber + 1;
			return true;
		}
		return false;
	}

	/**
	 * Print the contents of the sliding window (i.e. the sequence numbers)
	 */
	public static void printWindow() {
		System.out.println("---- Ack Window -----");
		Set<Map.Entry<Integer, Datagram>> tempSet = Window.entrySet();
		Iterator<Entry<Integer, Datagram>> iterator = tempSet.iterator();
		while (iterator.hasNext()) {
			Map.Entry<Integer, Datagram> entry = (Entry<Integer, Datagram>) iterator
					.next();
			System.out.println("Segment:" + entry.getKey()
					+ " present in window");
		}
		System.out.println("---------------------");
	}
}
