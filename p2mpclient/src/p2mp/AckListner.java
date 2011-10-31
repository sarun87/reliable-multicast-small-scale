/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package p2mp;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * @author Shyam
 */
public class AckListner implements Runnable {

	private Lock myLock;
	private DatagramSender myDatagramSender;
	private Timer myTimer;

	private void ProcessAck() {
		while (true) {
			try {
				myLock.lock();
				while (!DataRepository.AckQueue.isEmpty()) {
					
					Datagram dg = DataRepository.AckQueue.take();
					if(dg == null){
						break;
					}
					int seqNo = dg.getSequenceNumber();

					if (seqNo == -1) {
						myDatagramSender.Retransmit(0, dg.ServerNumber);
					} else if (SlidingWindow.setAck(seqNo, dg.ServerNumber)) {
						// a triple ack has been found ... so do fast retransmit
						myTimer.resetTimer();
						if (SlidingWindow.Window.get(seqNo + 1) != null) {
							for (Integer serverNumber : DataRepository.serverIPs
									.values()) {
								if (!SlidingWindow.checkAckCompletedByReciever(
										seqNo + 1, serverNumber)) {
									myDatagramSender.Retransmit(seqNo + 1,
											serverNumber);
									// System.out.println("Retransmit on triple dup seq:"+(seqNo+1)+" to server:"+serverNumber);
								}
							}
						}
						// send the datagram with next seqno as triple ack is
						// sent for the successfully sent previous packet
					} else {
						// move the window by some number of segments and add
						// new segments for the removed ones.
						int seqindex = SlidingWindow.StartingSeqNumber;
						if (SlidingWindow.Window.get(seqindex) != null) {
							while (SlidingWindow
									.checkIfAckCompletedByAllRecievers(seqindex)) {
								SlidingWindow.removeItemFromWindow(seqindex);
								myTimer.resetTimer();
								if (DataRepository.LAST_DATAPACKET_SEQNO == seqindex) {
									DataRepository.FILE_TRANSFER_COMPLETE = true;
									System.out
											.println("Final ACK received, seqNo: "
													+ DataRepository.LAST_DATAPACKET_SEQNO);
									System.out
											.println("Time taken: "
													+ (System
															.currentTimeMillis() - DataRepository.TimeTakenForFileTransfer)
													/ 1000.0 + " sec");
									SendAckToTerminate();
									System.exit(0);
									break;
								}
								Datagram tempDg = myDatagramSender
										.TransmitNextSegment();
								if (tempDg == null) {
									DataRepository.WAITING_FOR_LAST_ACK = true;
									DataRepository.LAST_DATAPACKET_SEQNO = SlidingWindow.EndingSeqNumber;
									System.out
											.println("Waiting for last ACK with seqNo: "
													+ DataRepository.LAST_DATAPACKET_SEQNO);
								} 
								seqindex++;
								if (seqindex > seqNo) {
									break;
								}
							}
						}
					}
				}
			} catch (Exception ex) {
				Logger.getLogger(AckListner.class.getName()).log(Level.SEVERE,
						null, ex);
			} finally {
				myLock.unlock();
			}
		}
	}

	private void SendAckToTerminate() {
		Datagram lastAckDg = new Datagram(new byte[0], 0, false);
		for (int i = 0; i < 10; i++) {
			myDatagramSender.sendDatagram(lastAckDg);
		}
	}

	@Override
	public void run() {
		try {
			myLock = new ReentrantLock();
			myTimer = Timer.getInstance();
			myDatagramSender = DatagramSender.getInstance();

			ProcessAck();
		} catch (Exception e) {
			// TODO: handle exception
		}
		
	}
}
