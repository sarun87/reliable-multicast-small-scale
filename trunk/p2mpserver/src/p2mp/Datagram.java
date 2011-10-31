/** ********************************************************************
 * File:           Datagram.java 
 * Description:    Consists the datagram object.
 * Authors:        Arun, Shyam, Rahul, Venkatesh 
 * Created:        Sun Oct 23 01:17:04 EST 2011
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

import java.nio.ByteBuffer;

/**
 * Create a datagram that contains header and body of the datagarm.
 */
public class Datagram {

	/**
	 * Holds the sequencee number of the datagram.
	 */
	public byte[] sequenceNumber;

	/**
	 * Holds the checksum for the body. Checksum not implemented as sudo-header
	 */
	public byte[] checksum;

	/**
	 * Holds datagramType which is either DATA or ACKNOWLEDGMENT
	 */
	public byte[] datagramType;

	/**
	 * Holds the data in bytes.
	 */
	public byte[] data;

	/**
	 * Holds the size of data that the object "data" cotains.
	 */
	public byte[] dataSize;

	/**
	 * Constructor to create a datagram object
	 * 
	 * @param data
	 *            Data to be stored by the datagram
	 * @param dataSize
	 *            Datasize of the data
	 * @param isDataPacket
	 *            packet is data or ack
	 */
	public Datagram(byte[] data, int dataSize, boolean isDataPacket) {

	}

	/**
	 * Default constructor that initializes the datagram object to null/zero
	 */
	public Datagram() {
		data = null;
		dataSize = ByteBuffer.allocate(4).putInt(0, 0).array();
		datagramType = null;
		checksum = ByteBuffer.allocate(2).putChar((char) 0).array();
		sequenceNumber = ByteBuffer.allocate(4).putInt(0, 0).array();

	}

	/**
	 * Get the datagram as a byte array. Used to send it over the socket.
	 * 
	 * @return entire packet in bytes
	 */
	public byte[] getBytes() {
		byte[] temp = new byte[sequenceNumber.length + checksum.length
				+ datagramType.length + dataSize.length];
		System.arraycopy(sequenceNumber, 0, temp, 0, sequenceNumber.length);
		System.arraycopy(checksum, 0, temp, sequenceNumber.length,
				checksum.length);
		System.arraycopy(datagramType, 0, temp, sequenceNumber.length
				+ checksum.length, datagramType.length);
		System.arraycopy(dataSize, 0, temp, sequenceNumber.length
				+ checksum.length + datagramType.length, dataSize.length);
		return temp;
	}
}
