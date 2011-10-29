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
		while (!DataRepository.FILE_TRANSFER_COMPLETE) {// todo: while(last ack
														// not received.)
			try {
				myLock.lock();
				while (!DataRepository.AckQueue.isEmpty()) {
					Datagram dg = DataRepository.AckQueue.poll();
					int seqNo = dg.getSequenceNumber();
					System.out.println("Processing ACK: " + seqNo);
					if (seqNo == -1) {
						myDatagramSender.Retransmit(0, dg.ServerNumber);
					} else if (SlidingWindow.setAck(seqNo, dg.ServerNumber)) {
						// a triple ack has been found ... so do fast retransmit
						// todo: reset timer
						myTimer.resetTimer();
						System.out.println("Triple dup on seq:"+seqNo+ " by server:"+dg.ServerNumber);
						if (SlidingWindow.Window.get(seqNo + 1) != null) {
							for (Integer serverNumber : DataRepository.serverIPs
									.values()) {
								if (!SlidingWindow.checkAckCompletedByReciever(
										seqNo + 1, serverNumber)) {
									myDatagramSender.Retransmit(seqNo + 1,
											serverNumber);
									System.out.println("Retransmit on triple dup seq:"+(seqNo+1)+" to server:"+serverNumber);
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
								System.out
										.println("Removed Segment with seqNo: "
												+ seqindex);
								if (DataRepository.LAST_DATAPACKET_SEQNO == seqindex) {
									DataRepository.FILE_TRANSFER_COMPLETE = true;
									System.out
											.println("Final ACK received, seqNo: "
													+ DataRepository.LAST_DATAPACKET_SEQNO);
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
								} else {
									// Segment tempSeg = new Segment(tempDg);
									// SlidingWindow.addItemToWindow(tempDg.getSequenceNumber(),
									// tempSeg);
									System.out
											.println("Sent new Segment with seqNo: "
													+ tempDg.getSequenceNumber());
									//Thread.sleep(10);
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

	@Override
	public void run() {
		myLock = new ReentrantLock();
		myTimer = Timer.getInstance();
		myDatagramSender = DatagramSender.getInstance();

		ProcessAck();
	}
}
