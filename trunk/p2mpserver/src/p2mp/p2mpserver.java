package p2mp;

import java.io.File;
import java.io.FileOutputStream;
import java.net.*;
import java.nio.ByteBuffer;


/**
 * 
 * @author arun
 * 
 */
class p2mpserver {
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
		//FileWriter fwriter = new FileWriter(fileName);
		//BufferedWriter out = new BufferedWriter(fwriter);
		File file = new File(fileName);
		FileOutputStream out = new FileOutputStream(file);
		
		// Start waiting for packets
		DatagramSocket serverSocket = new DatagramSocket(portNo);
		System.out.println("Init Complete. Waiting for incoming packets...");
		byte[] receiveData = new byte[1036];
		byte[] sendData = new byte[12];
		while (true) {
			DatagramPacket receivePacket = new DatagramPacket(receiveData,
					receiveData.length);
			serverSocket.receive(receivePacket);
			InetAddress IPAddress = receivePacket.getAddress();
			int port = receivePacket.getPort();
			
			//System.out.println("Segment received! Extracting packet Information now..");
			// 12 byte header extraction

			int seqNumber = ByteBuffer.allocate(4).put(receiveData, 0, 4).getInt(0);
			
			// CHECK WITH PACKET LOSS PROBABILITY TO SIMULATE LOST PACKETS //////
			double randomProb = Math.random();
			if(randomProb < packetLossProbability){
				// Discard packet. Do nothing.
				System.out.println("Packet Loss, sequence Number = "+ seqNumber );
				continue;
			}
			//////////////////////////
			
			Datagram receivedDatagram = new Datagram();
			receivedDatagram.sequenceNumber = ByteBuffer.allocate(4).putInt(0,seqNumber).array();
			receivedDatagram.checksum = ByteBuffer.allocate(2).put(receiveData,4,2).array();
			receivedDatagram.datagramType = ByteBuffer.allocate(2).put(receiveData, 6, 2).array();
			receivedDatagram.dataSize = ByteBuffer.allocate(4).put(receiveData,8,4).array();
			int dataSize = ByteBuffer.allocate(4).put(receivedDatagram.dataSize).getInt(0);
			// Data Extraction by using the dataSize present in the header.
			receivedDatagram.data = ByteBuffer.allocate(dataSize).put(receiveData,12,dataSize).array();
			// Check checksum
			long checkSumResult = InternetChecksum.getCheckSum(ByteBuffer.allocate(dataSize).put(receiveData,12,dataSize).array());
			long checkSum = (long)ByteBuffer.allocate(2).put(receivedDatagram.checksum).getChar(0);
			if(checkSumResult != checkSum ){
				// Discard packet, do nothing
				//System.out.println("Error in packet with seq:"+seqNumber);
				continue;
			}
			if(seqNumber == DataRepository.expectedSequenceNumber){
				// Expected in-sequence segment has arrived
				// send this and the other in-sequence packets to the upper layer.
				//System.out.println("In sequence packet with seq:"+seqNumber);
				SlidingWindow.addItemToWindow(seqNumber, receivedDatagram);
				
				if(ByteBuffer.allocate(2).put(receivedDatagram.datagramType).getChar(0) == DataRepository.ACKPACKET){
					System.out.println("END OF FILE RECEIVED! File Transfer complete!");
					break;
				}
				
				int maxSeqInOrder;
				for(maxSeqInOrder = seqNumber; maxSeqInOrder < SlidingWindow.StartingSeqNumber + DataRepository.WINDOWSIZE; ++maxSeqInOrder){
					Datagram value = SlidingWindow.Window.get(maxSeqInOrder);
					if (value != null){
						DataRepository.expectedSequenceNumber = maxSeqInOrder+1;
					}
					else{
						break;
					}
				}
				maxSeqInOrder = maxSeqInOrder - 1;
				//System.out.print("Sending segments to upper layer:");
				for(int count = SlidingWindow.StartingSeqNumber; count <= maxSeqInOrder; ++count){
					Datagram dgToBeWritten = SlidingWindow.Window.get(count);
					out.write(dgToBeWritten.data);
					SlidingWindow.removeItemFromWindow(count);
					//System.out.print(" "+count+" ");
				}
				//System.out.println("");

				// Construct Ack packet
				Datagram acknowledgmentPacket = new Datagram();
				acknowledgmentPacket.datagramType = ByteBuffer.allocate(2).putChar((char)DataRepository.ACKPACKET).array();
				acknowledgmentPacket.sequenceNumber= ByteBuffer.allocate(4).putInt(0,DataRepository.expectedSequenceNumber-1).array();
				sendData = acknowledgmentPacket.getBytes();
				
				// Construct a java datagram packet and send it.
				DatagramPacket sendPacket = new DatagramPacket(sendData,
									sendData.length, IPAddress, port);
				serverSocket.send(sendPacket);
				//System.out.println("Ack:"+(DataRepository.expectedSequenceNumber -1)+ " sent");
				
			}
			else
			{
				if(seqNumber > DataRepository.expectedSequenceNumber && seqNumber < SlidingWindow.StartingSeqNumber + DataRepository.WINDOWSIZE){
					// Out of sequence packet. Buffer it and send ack for expected Sequence Number - 1 (Previously ack'ed packet)
					//System.out.println("Out-of-Seq segment recieved with seq:"+seqNumber);
					SlidingWindow.addItemToWindow(seqNumber, receivedDatagram);
					
					// Send ack with packet previously ack'ed.
					Datagram acknowledgmentPacket = new Datagram();
					acknowledgmentPacket.datagramType = ByteBuffer.allocate(2).putChar((char)DataRepository.ACKPACKET).array();
					acknowledgmentPacket.sequenceNumber= ByteBuffer.allocate(4).putInt(0,DataRepository.expectedSequenceNumber-1).array();
					sendData = acknowledgmentPacket.getBytes();
					
					//System.out.println("Ack:"+(DataRepository.expectedSequenceNumber-1)+" sent");
					// Construct a java datagram packet and send it.
					DatagramPacket sendPacket = new DatagramPacket(sendData,
										sendData.length, IPAddress, port);
					serverSocket.send(sendPacket);
				}
				else if(seqNumber < DataRepository.expectedSequenceNumber){
					// Already acke'd. Re-ack with the last packet that has already been sent to the higher layer.
					//System.out.println("Already ack-ed segment received with seq:"+seqNumber);
					
					// Send ack with packet previously ack'ed.
					Datagram acknowledgmentPacket = new Datagram();
					acknowledgmentPacket.datagramType = ByteBuffer.allocate(2).putChar((char)DataRepository.ACKPACKET).array();
					acknowledgmentPacket.sequenceNumber= ByteBuffer.allocate(4).putInt(0,DataRepository.expectedSequenceNumber-1).array();
					sendData = acknowledgmentPacket.getBytes();
					
					//System.out.println("Ack:"+(DataRepository.expectedSequenceNumber - 1)+ " sent");
					// Construct a java datagram packet and send it.
					DatagramPacket sendPacket = new DatagramPacket(sendData,
										sendData.length, IPAddress, port);
					serverSocket.send(sendPacket);
				}
				else if(ByteBuffer.allocate(2).put(receivedDatagram.datagramType).getChar(0) == DataRepository.ACKPACKET){
					System.out.println("END OF FILE RECEIVED! File Transfer complete!");
					break;
				}
				else
				{
					// Packet out of sequence and invalid.
					// Drop packet and do nothing.
					//System.out.println("Invalid packet/packet not in window. seq:"+seqNumber);
					continue;
				}
			}  // End of else - Not the right seq number
		} // End of while construct
		out.close();
	} // End of Method - main
} // End of Method - class
