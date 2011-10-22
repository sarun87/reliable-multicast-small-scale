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

    private boolean isTimerSet = false;
    private boolean isTimerReset = true;
    private long waitTime = 0;
    private Thread timerThread;

    public void setTimer(long waitTime) {
        if (!isTimerSet) {
            isTimerSet = true;
            isTimerReset = false;
            this.waitTime = waitTime;
        }
    }

    public void startTimer() {
        timerThread = new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(waitTime);
                } catch (InterruptedException ex) {
                    Logger.getLogger(Timer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };


    }

    public void resetTimer() {
        isTimerReset = true;
        this.isTimerSet = false;
        timerThread.stop();
        timerThread.destroy();
    }

    @Override
    public void run() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
