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
	private Segment earliestOutstandingSegment;
	private boolean timerReset = false;
	private long timeToWait;
	private DatagramSender myDatagramSender;

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
			for (Integer serverNumber : DataRepository.serverIPs.values()) {
				if (!SlidingWindow.checkAckCompletedByReciever(
						SlidingWindow.StartingSeqNumber, serverNumber)) {
					myDatagramSender.Retransmit(
							SlidingWindow.StartingSeqNumber, serverNumber);
				}
			}
		}
	}

	public void resetTimer() {
		// System.out.println("!!! TIMER RESET !!!");
		timerReset = true;
	}

	@Override
	public void run() {
		try {
			while (!DataRepository.FILE_TRANSFER_COMPLETE) {
				earliestOutstandingSegment = SlidingWindow.Window
						.get(SlidingWindow.StartingSeqNumber);

				if (earliestOutstandingSegment != null) {
					long timeElapsed = System.currentTimeMillis()
							- earliestOutstandingSegment.PacketSentTime;
					this.timeToWait = DataRepository.RTT - timeElapsed;
					timerReset = false;
					for (int i = (int) timeToWait; i >= 0; i--) {
						Thread.sleep(1);
						if (timerReset) {
							break;
						}
					}
					if (!timerReset) {
						System.out.println("Timeout, Sequence Number: "
								+ SlidingWindow.StartingSeqNumber);
						RetransmitOnTimeout();
					}
				}
			}
		} catch (InterruptedException ex) {
			Logger.getLogger(Timer.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
}
