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
    private boolean timeOutOccurred = false;
    private long timeToWait;
    private Thread timerThread;
    DatagramSender myDatagramSender;
    
    public static Timer getInstance() {
        if (myTimer == null) {
            myTimer = new Timer();
        }
        return myTimer;
    }
    
    private Timer() {
        timeToWait = 0;
        timerReset = false;
        timeOutOccurred = false;
        myDatagramSender = DatagramSender.getInstance();
        timerThread = new Thread() {
            
            @Override
            public void run() {
                try {
                    Thread.sleep(timeToWait);
                    timeOutOccurred = true;
                    System.out.println("!!! TIMEOUT !!!");
                } catch (InterruptedException ex) {
                    Logger.getLogger(Timer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };
    }
    
    private void RetransmitOnTimeout() {
        System.out.print("Retransmitting on Timeout to: ");
        for (Integer serverNumber : DataRepository.serverIPs.values()) {
            if (!SlidingWindow.checkAckCompletedByReciever(SlidingWindow.StartingSeqNumber, serverNumber)) {
                myDatagramSender.Retransmit(SlidingWindow.StartingSeqNumber + 1, serverNumber);
                System.out.print(serverNumber + " ");
            }
        }
        System.out.println("END");
    }
    
    public synchronized void resetTimer() {
        System.out.println("!!! TIMER RESET !!!");
        timerReset = true;
        timerThread.stop();
        timerThread.destroy();
    }
    
    @Override
    public void run() {
        while (!DataRepository.FILE_TRANSFER_COMPLETE) {
            earliestOutstandingSegment = SlidingWindow.Window.get(SlidingWindow.StartingSeqNumber);
            long timeElapsed = System.currentTimeMillis() - earliestOutstandingSegment.PacketSentTime;
            this.timeToWait = DataRepository.RTT - timeElapsed;
            timerReset = false;
            timeOutOccurred = false;
            timerThread.start();
            while (timeOutOccurred || timerReset) {
                //do nothing
            }
            if (timeOutOccurred) {
                System.out.println("Timeout occurred for datagram: " + SlidingWindow.StartingSeqNumber);
                RetransmitOnTimeout();
            }
        }
    }
}
