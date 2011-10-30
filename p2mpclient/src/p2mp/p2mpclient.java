package p2mp;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

class p2mpclient {

	public static void main(String args[]) {
		try {
			initDataRepository(args);
			DatagramSender ds = DatagramSender.getInstance();

			Thread networkListnerThread = new Thread(new NetworkListner());
			networkListnerThread.start();
			Thread ackListnerThread = new Thread(new AckListner());
			ackListnerThread.start();
			Thread timerThread = new Thread(Timer.getInstance());

			System.out.println("File Transfer started");
			DataRepository.TimeTakenForFileTransfer = System
					.currentTimeMillis();
			for (int i = 0; i < DataRepository.WINDOWSIZE; i++) {
				try {
					ds.TransmitNextSegment();
				} catch (IOException ex) {
					Logger.getLogger(p2mpclient.class.getName()).log(
							Level.SEVERE, null, ex);
				}
			}

			timerThread.start();
			while (!DataRepository.FILE_TRANSFER_COMPLETE) {
				// do nothing
			}

		} catch (Exception ex) {
			Logger.getLogger(p2mpclient.class.getName()).log(Level.SEVERE, null,
					ex);
		}
	}

	private static void initDataRepository(String args[]) {
		try {
			// Initialize the parameters given using the command-line
			// Set the Number of Receivers, Window Size and Number of
			// receivers in the DataRepository.
			DataRepository.serverIPs = new HashMap<String, Integer>();
			int i = 0;
			for (i = 0; !(args[i].contains(".") == false); ++i) {
				DataRepository.serverIPs.put(args[i].toString(), i);
			}
			DataRepository.NUMBER_OF_RECEIVERS = i;
			DataRepository.clientSocket = new DatagramSocket();

			DataRepository.SENDER_PORT_NUMBER = Integer.parseInt(args[i]);
			DataRepository.fileName = args[++i];
			DataRepository.WINDOWSIZE = Integer.parseInt(args[++i]);
			DataRepository.MSS = Integer.parseInt(args[++i]);
			DataRepository.RTT = Integer.parseInt(args[++i]);

			DataRepository.AckQueue = new LinkedList<Datagram>();
		} catch (SocketException ex) {
			System.out.println("Format: p2mpclient <serverIP1> <serverIP2> ... <portNo#> <InputFileName.txt> <Window Size> <MSS> <RTT>");
		}
	}
}
