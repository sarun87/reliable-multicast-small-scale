/** ********************************************************************
 * File:           DataRepository.java 
 * Description:    Consists the datarepository functionality for server.
 * Authors:        Arun, Shyam, Rahul, Venkatesh 
 * Created:        Sat Oct 22 11:43:45 EST 2011
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

/**
 * Datarepository object that holds constants and variables required and used by
 * the program.
 */
public class DataRepository {
	/**
	 * Boolean flag to notify completion of the file transfer
	 */
	public static boolean FILE_TRANSFER_COMPLETE = false;

	/**
	 * Round trip time in milliseconds. Used for timeout
	 */
	public static int RTT = 500;

	/**
	 * Data packet type id = 101010101010101
	 */
	public static final int DATAPACKET = 0x5555;

	/**
	 * Acknowledgment packet type id = 1010101010101010
	 */
	public static final int ACKPACKET = 0xAAAA;

	/**
	 * Maximum segment size of the datagram packet
	 */
	public static int MSS = -1;

	/**
	 * Window size for the protocol (N)
	 */
	public static int WINDOWSIZE = -1;

	/**
	 * Next sequence number datagram that the receiver expects.
	 */
	public static int expectedSequenceNumber = 0;

	/**
	 * Port number that the receiver will listen to.
	 */
	public static int SENDER_PORT_NUMBER = -1;

	/**
	 * Filename to which this file has to be written to.
	 */
	public static String fileName;
}
