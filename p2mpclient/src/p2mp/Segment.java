/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package p2mp;

public class Segment {
	public Datagram Datapacket;
	public long PacketSentTime;
	public int[] ReceiverAckList;

	public Segment(Datagram dataPacket) {
		Datapacket = dataPacket;
		PacketSentTime = System.currentTimeMillis();
		ReceiverAckList = new int[DataRepository.NUMBER_OF_RECEIVERS];
		for (int i = 0; i < DataRepository.NUMBER_OF_RECEIVERS; i++) {
			ReceiverAckList[i] = 0;
		}
	}
}
