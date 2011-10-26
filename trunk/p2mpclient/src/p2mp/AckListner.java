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

    private void ProcessAck() {
        while (!DataRepository.WAITING_FOR_LAST_ACK) {//todo: while(last ack not received.)
            try {
                myLock.lock();
                while (!DataRepository.AckQueue.isEmpty()) {
                    Datagram dg = DataRepository.AckQueue.poll();
                    int seqNo = dg.getSequenceNumber();
                    if (seqNo == -1) {
                        myDatagramSender.Retransmit(seqNo + 1, dg.ServerNumber);
                    }
                    boolean setAck = SlidingWindow.setAck(seqNo, dg.ServerNumber);
                    if (setAck) {
                        //a triple ack has been found ... so do fast retransmit
                        //todo: reset timer
                        myDatagramSender.Retransmit(seqNo + 1, dg.ServerNumber);//send the datagram with next seqno as triple ack is sent for the successfully sent previous packet
                    } else {
                        //move the window by some number of segments and add new segments for the removed ones.
                        int seqindex = SlidingWindow.StartingSeqNumber;
                        while (SlidingWindow.checkIfAckCompletedByAllRecievers(seqindex)) {
                            SlidingWindow.removeItemFromWindow(seqindex);
                            if (DataRepository.LAST_DATAPACKET_SEQNO == seqindex) {
                                DataRepository.FILE_TRANSFER_COMPLETE = true;
                                break;
                            }
                            Datagram tempDg = myDatagramSender.TransmitNextSegment();
                            if (tempDg == null) {
                                DataRepository.LAST_DATAPACKET_SENT = true;
                                DataRepository.LAST_DATAPACKET_SEQNO = SlidingWindow.EndingSeqNumber;
                            } else {
                                Segment tempSeg = new Segment(tempDg);
                                SlidingWindow.addItemToWindow(tempDg.getSequenceNumber(), tempSeg);
                            }
                            seqindex++;
                            if (seqindex > seqNo) {
                                break;
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                Logger.getLogger(AckListner.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                myLock.unlock();
            }
        }
    }

    @Override
    public void run() {
        myLock = new ReentrantLock();
        myDatagramSender = DatagramSender.getInstance();
        ProcessAck();
    }
}
