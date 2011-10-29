/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package p2mp;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * @author Shyam
 */
public class DatagramSender {

	private static DatagramSender mySender;
	private File myFileToSend;
	private FileInputStream myFileInputStream;

	public static DatagramSender getInstance() {
		if (mySender == null) {
			mySender = new DatagramSender();
		}
		return mySender;
	}

	private DatagramSender() {
		try {
			myFileToSend = new File(DataRepository.fileName);
			myFileInputStream = new FileInputStream(myFileToSend);
		} catch (Exception ex) {
			Logger.getLogger(DatagramSender.class.getName()).log(Level.SEVERE,
					null, ex);
		}
	}

	public Datagram TransmitNextSegment() throws IOException {
		byte[] dataBytes = new byte[DataRepository.MSS];
		int dataSize = myFileInputStream.read(dataBytes);
		if (dataSize > 0) {
			Datagram tempDatagram = new Datagram(dataBytes, dataSize, true);
			Segment tempSeg = new Segment(tempDatagram);
			SlidingWindow.addItemToWindow(tempDatagram.getSequenceNumber(),
					tempSeg);
			sendDatagram(tempDatagram);
			return tempDatagram;
		}
		return null;// if the file is completely read... no more bytes to send
	}

	public synchronized void Retransmit(int sequenceNumber, int serverNo) {
		// todo: set timer to new value.
		Segment segmentToRetransmit = SlidingWindow.Window.get(sequenceNumber);
		segmentToRetransmit.PacketSentTime = System.currentTimeMillis();
		Datagram dgToRetransmit = segmentToRetransmit.Datapacket;
		SlidingWindow.addItemToWindow(dgToRetransmit.getSequenceNumber(),
				segmentToRetransmit);
		String ipAddr = "";
		try {
			Iterator<Map.Entry<String, Integer>> it = DataRepository.serverIPs
					.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<String, Integer> pairs = (Map.Entry<String, Integer>) it
						.next();
				if (pairs.getValue() == serverNo) {
					ipAddr = pairs.getKey();
					break;
				}
			}
			DatagramPacket sendPacket = new DatagramPacket(
					dgToRetransmit.getBytes(), dgToRetransmit.Length,
					InetAddress.getByName(ipAddr),
					DataRepository.SENDER_PORT_NUMBER);
			DataRepository.clientSocket.send(sendPacket);
		} catch (Exception ex) {
			Logger.getLogger(DatagramSender.class.getName()).log(Level.SEVERE,
					null, ex);
		}
		// System.out.println("Retransmitting datagram: " +
		// dgToRetransmit.getSequenceNumber());
	}

	public void sendDatagram(Datagram dataGram) {
		for (Iterator<String> it = DataRepository.serverIPs.keySet().iterator(); it
				.hasNext();) {
			try {
				String ipAddr = (String) it.next();
				DatagramPacket sendPacket = new DatagramPacket(
						dataGram.getBytes(), dataGram.Length,
						InetAddress.getByName(ipAddr),
						DataRepository.SENDER_PORT_NUMBER);
				DataRepository.clientSocket.send(sendPacket);
			} catch (Exception ex) {
				Logger.getLogger(DatagramSender.class.getName()).log(
						Level.SEVERE, null, ex);
			}
		}
	}
}