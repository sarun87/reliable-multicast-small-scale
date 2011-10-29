/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package p2mp;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * @author Shyam
 */
public class Timer implements Runnable {

	private static Timer myTimer;
	Segment earliestOutstandingSegment;
	private boolean timerReset = false;
	private long timeToWait;
	DatagramSender myDatagramSender;

	public static Timer getInstance() {
		if (myTimer == null) {
			myTimer = new Timer();
		}
		return myTimer;
	}

	private Timer() {
		timeToWait = DataRepository.RTT;
		timerReset = false;
		myDatagramSender = DatagramSender.getInstance();
	}

	private void RetransmitOnTimeout() {
		if (SlidingWindow.Window.get(SlidingWindow.StartingSeqNumber) != null) {
			System.out.print("Retransmitting on Timeout to: ");
			for (Integer serverNumber : DataRepository.serverIPs.values()) {
				if (!SlidingWindow.checkAckCompletedByReciever(
						SlidingWindow.StartingSeqNumber, serverNumber)) {
					myDatagramSender.Retransmit(
							SlidingWindow.StartingSeqNumber, serverNumber);
					System.out.print(serverNumber + " ");
				}
			}
			System.out.println("END");
		}
	}

	public void resetTimer() {
		System.out.println("!!! TIMER RESET !!!");
		timerReset = true;
	}

	@Override
	public void run() {
		try {
			while (!DataRepository.FILE_TRANSFER_COMPLETE) {
				earliestOutstandingSegment = SlidingWindow.Window
						.get(SlidingWindow.StartingSeqNumber);

				if (earliestOutstandingSegment != null) {
					System.out.println("Timer started for seqno: "
							+ earliestOutstandingSegment.Datapacket
									.getSequenceNumber());
					long timeElapsed = System.currentTimeMillis()
							- earliestOutstandingSegment.PacketSentTime;
					this.timeToWait = DataRepository.RTT - timeElapsed;
					timerReset = false;
					System.out.println("timeToWait = "
							+ timeToWait
							+ "ms for seqno: "
							+ earliestOutstandingSegment.Datapacket
									.getSequenceNumber());
					for (int i = (int) timeToWait; i >= 0; i--) {
						Thread.sleep(1);
						if (timerReset) {
							break;
						}
					}
					if (!timerReset) {
						System.out.println("Timeout occurred for datagram: "
								+ SlidingWindow.StartingSeqNumber);
						RetransmitOnTimeout();
					}
				} else {
					System.out.println("************ERROR************");
				}
			}
		} catch (InterruptedException ex) {
			Logger.getLogger(Timer.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
}
