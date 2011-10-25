package p2mp;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;

/**
 * 
 * @author arun
 * 
 */
class UDPServer {
	public static void main(String args[]) throws Exception {
		// Initialize the parameters given using the command-line
		Integer portNo = Integer.parseInt(args[0]);
		String fileName = args[1];
		Integer windowSize = Integer.parseInt(args[2]);
		double packetLossProbability = Double.parseDouble(args[3]);

		// Set the dataRepository
		DataRepository.setWindowSize(windowSize);
		DataRepository.setPortNumber(portNo);
		DataRepository.fileName = fileName;

		// Open file that has to be written into
		FileWriter fwriter = new FileWriter(fileName);
		BufferedWriter out = new BufferedWriter(fwriter);
		
		// Start waiting for packets
		DatagramSocket serverSocket = new DatagramSocket(portNo);
		System.out.println("Waiting for incoming packets...");
		byte[] receiveData = new byte[1032];
		byte[] sendData = new byte[12];
		while (true) {
			DatagramPacket receivePacket = new DatagramPacket(receiveData,
					receiveData.length);
			serverSocket.receive(receivePacket);
			InetAddress IPAddress = receivePacket.getAddress();
			int port = receivePacket.getPort();
			
			//// TODO: CHECK WITH PACKET LOSS PROBABILITY TO SIMULATE LOST PACKETS //////
			if(packetLossProbability == 0.1){
				
			}
			//////////////////////////
			
			System.out.println("Segment received! Extracting packet Information now..");
			// 12 byte header extraction
			int seqNumber = ByteBuffer.allocate(4).put(receiveData, 0, 4).getInt();
			Datagram receivedDatagram = new Datagram();
			receivedDatagram.sequenceNumber = ByteBuffer.allocate(4).putInt(seqNumber).array();
			receivedDatagram.checksum = ByteBuffer.allocate(2).put(receiveData,4,2).array();
			receivedDatagram.datagramType = ByteBuffer.allocate(2).put(receiveData, 6, 2).array();
			receivedDatagram.dataSize = ByteBuffer.allocate(4).put(receiveData,8,4).array();
			int dataSize = ByteBuffer.allocate(4).put(receivedDatagram.dataSize).getInt();
			// Data Extraction by using the dataSize present in the header.
			receivedDatagram.data = ByteBuffer.allocate(dataSize).put(receiveData,12,dataSize).array();
			// Check checksum
			long checkSumResult = InternetChecksum.getCheckSum(ByteBuffer.allocate(dataSize+12).put(receiveData,0,12+dataSize).array(), receivedDatagram.checksum);
			if(checkSumResult != 0xffff ){
				// Discard packet, do nothing
				continue;
			}
			if(seqNumber == DataRepository.expectedSequenceNumber){
				// Expected in-sequence segment has arrived
				// send this and the other in-sequence packets to the upper layer.
				SlidingWindow.addItemToWindow(seqNumber, receivedDatagram);
				
				// Wrong..
				int maxSeqInOrder;
				for(maxSeqInOrder = seqNumber; maxSeqInOrder < SlidingWindow.StartingSeqNumber + DataRepository.WINDOWSIZE; ++maxSeqInOrder){
					Datagram value = SlidingWindow.Window.get(maxSeqInOrder);
					if (value == null){
						DataRepository.expectedSequenceNumber = maxSeqInOrder;
						maxSeqInOrder = maxSeqInOrder - 1;
						break;
					}
				}
				for(int count = SlidingWindow.StartingSeqNumber; count < maxSeqInOrder; ++count){
					Datagram dgToBeWritten = SlidingWindow.Window.get(seqNumber);
					Integer size = ByteBuffer.allocate(4).put(dgToBeWritten.dataSize).getInt();
					CharBuffer dataToBeWritten = ByteBuffer.allocate(size).put(dgToBeWritten.data).asCharBuffer();
					out.write(dataToBeWritten.array());
					SlidingWindow.removeItemFromWindow(count);
				}
				//for(int count = SlidingWindow.StartingSeqNumber; count <seqNumber; ++count){
				//	SlidingWindow.removeItemFromWindow(count);
				//}
				// Construct Ack packet
				Datagram acknowledgmentPacket = new Datagram();
				acknowledgmentPacket.datagramType = ByteBuffer.allocate(2).putInt(DataRepository.ACKPACKET).array();
				acknowledgmentPacket.sequenceNumber= ByteBuffer.allocate(4).putInt(DataRepository.expectedSequenceNumber-1).array();
				sendData = acknowledgmentPacket.getBytes();
				
				// Construct a java datagram packet and send it.
				DatagramPacket sendPacket = new DatagramPacket(sendData,
									sendData.length, IPAddress, port);
				serverSocket.send(sendPacket);
				
			}
			else
			{
				if(seqNumber >= SlidingWindow.StartingSeqNumber && seqNumber < SlidingWindow.StartingSeqNumber + DataRepository.WINDOWSIZE){
					// Out of sequence packet. Buffer it and send ack for expected Sequence Number - 1 (Previously ack'ed packet)
					SlidingWindow.addItemToWindow(seqNumber, receivedDatagram);
					
					// Send ack with packet previously ack'ed.
					Datagram acknowledgmentPacket = new Datagram();
					acknowledgmentPacket.datagramType = ByteBuffer.allocate(2).putInt(DataRepository.ACKPACKET).array();
					acknowledgmentPacket.sequenceNumber= ByteBuffer.allocate(4).putInt(DataRepository.expectedSequenceNumber-1).array();
					sendData = acknowledgmentPacket.getBytes();
					
					// Construct a java datagram packet and send it.
					DatagramPacket sendPacket = new DatagramPacket(sendData,
										sendData.length, IPAddress, port);
					serverSocket.send(sendPacket);
				}
				else if(ByteBuffer.allocate(4).put(receivedDatagram.datagramType).getInt() == DataRepository.ACKPACKET){
					break;
				}
				else
				{
					// Packet out of sequence and invalid.
					// Drop packet and do nothing.
					continue;
				}
			}
			//serverSocket.receive(receivePacket);
			//String sentence = new String(receivePacket.getData());
			//System.out.println("RECEIVED: " + sentence);

			//String capitalizedSentence = "Received by Server:"
			//		+ InetAddress.getLocalHost().toString() + "Bytes:"
			//		+ sentence.length();
			//sendData = capitalizedSentence.getBytes();
			//DatagramPacket sendPacket = new DatagramPacket(sendData,
			//		sendData.length, IPAddress, port);
			//serverSocket.send(sendPacket);
		}
		out.close();
	}
}
