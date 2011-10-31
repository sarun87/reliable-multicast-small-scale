/** ********************************************************************
 * File:           p2mpserver.java 
 * Description:    The server code running on multicast node that recei-
 * 				   -ves the file.
 * Authors:        Arun, Shyam, Rahul, Venkatesh 
 * Created:        Thu Oct 20 12:01:05 EST 2011
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

import java.io.File;
import java.io.FileOutputStream;
import java.net.*;
import java.nio.ByteBuffer;

/**
 * Class contains the main method that runs the program.
 */
class p2mpserver {

	/**
	 * Main method - start p2mpserver and wait for the multicast.
	 * 
	 * @param args
	 *            - portNo fileName WindowSize LossProbability
	 * @throws Exception
	 */
	public static void main(String args[]) throws Exception {

		// Initialize the parameters given using the command-line
		Integer portNo = Integer.parseInt(args[0]);
		String fileName = args[1];
		Integer windowSize = Integer.parseInt(args[2]);
		double packetLossProbability = Double.parseDouble(args[3]);

		// Set the dataRepository
		DataRepository.WINDOWSIZE = windowSize;
		DataRepository.SENDER_PORT_NUMBER = portNo;
		DataRepository.fileName = fileName;

		// Open file that has to be written into
		File file = new File(fileName);
		FileOutputStream out = new FileOutputStream(file);

		// Start waiting for packets
		DatagramSocket serverSocket = new DatagramSocket(portNo);
		System.out.println("Init Complete. Waiting for incoming packets...");
		byte[] receiveData = new byte[1036];
		byte[] sendData = new byte[12];
		// Continue till end of file has been received.
		while (true) {
			DatagramPacket receivePacket = new DatagramPacket(receiveData,
					receiveData.length);
			serverSocket.receive(receivePacket);
			InetAddress IPAddress = receivePacket.getAddress();
			int port = receivePacket.getPort();

			// @Debug
			// System.out.println("Segment received! Extracting packet Information now..");

			// 12 byte header extraction
			int seqNumber = ByteBuffer.allocate(4).put(receiveData, 0, 4)
					.getInt(0);

			// CHECK WITH PACKET LOSS PROBABILITY TO SIMULATE LOST PACKETS
			// //////
			double randomProb = Math.random();
			if (randomProb < packetLossProbability) {
				// Discard packet. Do nothing.
				System.out.println("Packet Loss, sequence Number = "
						+ seqNumber);
				continue;
			}
			// ////////////////////////

			// Build the datagram from the bytes received.
			Datagram receivedDatagram = new Datagram();
			receivedDatagram.sequenceNumber = ByteBuffer.allocate(4)
					.putInt(0, seqNumber).array();
			receivedDatagram.checksum = ByteBuffer.allocate(2)
					.put(receiveData, 4, 2).array();
			receivedDatagram.datagramType = ByteBuffer.allocate(2)
					.put(receiveData, 6, 2).array();
			receivedDatagram.dataSize = ByteBuffer.allocate(4)
					.put(receiveData, 8, 4).array();
			int dataSize = ByteBuffer.allocate(4)
					.put(receivedDatagram.dataSize).getInt(0);
			// Data Extraction by using the dataSize present in the header.
			receivedDatagram.data = ByteBuffer.allocate(dataSize)
					.put(receiveData, 12, dataSize).array();
			// Check checksum
			long checkSumResult = InternetChecksum.getCheckSum(ByteBuffer
					.allocate(dataSize).put(receiveData, 12, dataSize).array());
			long checkSum = (long) ByteBuffer.allocate(2)
					.put(receivedDatagram.checksum).getChar(0);
			// Check if the datagram is intact. Note: pseudo-header checksum not
			// implemented.
			if (checkSumResult != checkSum) {
				// Discard packet, do nothing
				// System.out.println("Error in packet with seq:"+seqNumber);
				continue;
			}
			// Check the sequence number of the datagram received
			if (seqNumber == DataRepository.expectedSequenceNumber) {
				// Expected in-sequence segment has arrived
				// send this and the other in-sequence packets to the upper
				// layer.
				// @Debug
				// System.out.println("In sequence packet with seq:"+seqNumber);
				SlidingWindow.addItemToWindow(seqNumber, receivedDatagram);

				// Check to see if the END of File is notified. In this case,
				// it's an ACK packet from the sender.
				if (ByteBuffer.allocate(2).put(receivedDatagram.datagramType)
						.getChar(0) == DataRepository.ACKPACKET) {
					System.out
							.println("END OF FILE RECEIVED! File Transfer complete!");
					break;
				}

				// Get the maximum segment that is in-order in the window.
				int maxSeqInOrder;
				for (maxSeqInOrder = seqNumber; maxSeqInOrder < SlidingWindow.StartingSeqNumber
						+ DataRepository.WINDOWSIZE; ++maxSeqInOrder) {
					Datagram value = SlidingWindow.Window.get(maxSeqInOrder);
					if (value != null) {
						DataRepository.expectedSequenceNumber = maxSeqInOrder + 1;
					} else {
						break;
					}
				}
				maxSeqInOrder = maxSeqInOrder - 1;
				// @Debug System.out.print("Sending segments to upper layer:");
				// Send the in-order packets to the application (upper layer)
				for (int count = SlidingWindow.StartingSeqNumber; count <= maxSeqInOrder; ++count) {
					Datagram dgToBeWritten = SlidingWindow.Window.get(count);
					out.write(dgToBeWritten.data);
					SlidingWindow.removeItemFromWindow(count);
					// @Debug System.out.print(" "+count+" ");
				}
				// @Debug System.out.println("");

				// Construct Acknowledgment packet for the received sequence
				// number
				Datagram acknowledgmentPacket = new Datagram();
				acknowledgmentPacket.datagramType = ByteBuffer.allocate(2)
						.putChar((char) DataRepository.ACKPACKET).array();
				acknowledgmentPacket.sequenceNumber = ByteBuffer.allocate(4)
						.putInt(0, DataRepository.expectedSequenceNumber - 1)
						.array();
				sendData = acknowledgmentPacket.getBytes();

				// Construct a java datagram packet and send it.
				DatagramPacket sendPacket = new DatagramPacket(sendData,
						sendData.length, IPAddress, port);
				serverSocket.send(sendPacket);
				// @Debug
				// System.out.println("Ack:"+(DataRepository.expectedSequenceNumber
				// -1)+ " sent");

			} else {
				// Not the in-order sequence number. Check for datagram already
				// ack-ed, out-of-sequence or invalid packet
				if (seqNumber > DataRepository.expectedSequenceNumber
						&& seqNumber < SlidingWindow.StartingSeqNumber
								+ DataRepository.WINDOWSIZE) {
					// Out of sequence packet. Buffer it and send ack for
					// expected Sequence Number - 1 (Previously ack'ed packet)
					// @Debug
					// System.out.println("Out-of-Seq segment recieved with seq:"+seqNumber);
					SlidingWindow.addItemToWindow(seqNumber, receivedDatagram);

					// Send ack with packet previously ack'ed.
					Datagram acknowledgmentPacket = new Datagram();
					acknowledgmentPacket.datagramType = ByteBuffer.allocate(2)
							.putChar((char) DataRepository.ACKPACKET).array();
					acknowledgmentPacket.sequenceNumber = ByteBuffer
							.allocate(4)
							.putInt(0,
									DataRepository.expectedSequenceNumber - 1)
							.array();
					sendData = acknowledgmentPacket.getBytes();

					// @Debug
					// System.out.println("Ack:"+(DataRepository.expectedSequenceNumber-1)+" sent");
					// Construct a java datagram packet and send it.
					DatagramPacket sendPacket = new DatagramPacket(sendData,
							sendData.length, IPAddress, port);
					serverSocket.send(sendPacket);
				} else if (seqNumber < DataRepository.expectedSequenceNumber) {
					// Already acke'd. Re-ack with the last packet that has
					// already been sent to the higher layer.
					// @Debug
					// System.out.println("Already ack-ed segment received with seq:"+seqNumber);

					// Send ack with packet previously ack'ed.
					Datagram acknowledgmentPacket = new Datagram();
					acknowledgmentPacket.datagramType = ByteBuffer.allocate(2)
							.putChar((char) DataRepository.ACKPACKET).array();
					acknowledgmentPacket.sequenceNumber = ByteBuffer
							.allocate(4)
							.putInt(0,
									DataRepository.expectedSequenceNumber - 1)
							.array();
					sendData = acknowledgmentPacket.getBytes();

					// @Debug
					// System.out.println("Ack:"+(DataRepository.expectedSequenceNumber
					// - 1)+ " sent");
					// Construct a java datagram packet and send it.
					DatagramPacket sendPacket = new DatagramPacket(sendData,
							sendData.length, IPAddress, port);
					serverSocket.send(sendPacket);
				} else if (ByteBuffer.allocate(2)
						.put(receivedDatagram.datagramType).getChar(0) == DataRepository.ACKPACKET) {
					System.out
							.println("END OF FILE RECEIVED! File Transfer complete!");
					break;
				} else {
					// Packet out of sequence and invalid.
					// Drop packet and do nothing.
					// @Debug
					// System.out.println("Invalid packet/packet not in window. seq:"+seqNumber);
					continue;
				}
			} // End of else - Not the right seq number
		} // End of while construct
			// Close the file
		out.close();
	} // End of Method - main
} // End of Method - class
