/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package p2mp;

import java.nio.ByteBuffer;

/**
 * 
 * @author Shyam
 */
public class Datagram {

	private byte[] sequenceNumber;
	private byte[] checksum;
	private byte[] datagramType;
	private byte[] data;
	private byte[] dataSize;
	public int ServerNumber;
	public int Length;

	public Datagram(byte[] data, int dataSize, boolean isDataPacket) {
		sequenceNumber = ByteBuffer.allocate(4)
				.putInt(0, DataRepository.getNextSequenceNumber()).array();
		if (isDataPacket) {
			datagramType = ByteBuffer.allocate(2)
					.putChar(0, DataRepository.DATAPACKET).array();
		} else {
			datagramType = ByteBuffer.allocate(2)
					.putChar(0, DataRepository.ACKPACKET).array();
		}
		this.data = data;
		checksum = ByteBuffer.allocate(2)
				.putChar((char) (InternetChecksum.getCheckSum(data))).array();
		this.dataSize = ByteBuffer.allocate(4).putInt(dataSize).array();
	}

	public Datagram(byte[] ackPacket) {
		// 12 byte header extraction
		sequenceNumber = ByteBuffer.allocate(4).put(ackPacket, 0, 4).array();
		datagramType = ByteBuffer.allocate(2).put(ackPacket, 6, 2).array();
	}

	public byte[] getBytes() {
		Length = sequenceNumber.length + checksum.length + datagramType.length
				+ dataSize.length + data.length;
		byte[] temp = new byte[Length];
		System.arraycopy(sequenceNumber, 0, temp, 0, sequenceNumber.length);
		System.arraycopy(checksum, 0, temp, sequenceNumber.length,
				checksum.length);
		System.arraycopy(datagramType, 0, temp, sequenceNumber.length
				+ checksum.length, datagramType.length);
		System.arraycopy(dataSize, 0, temp, sequenceNumber.length
				+ checksum.length + datagramType.length, dataSize.length);
		System.arraycopy(data, 0, temp, sequenceNumber.length + checksum.length
				+ datagramType.length + dataSize.length, data.length);

		return temp;
	}

	public int getSequenceNumber() {
		return ByteBuffer.allocate(4).put(sequenceNumber, 0, 4).getInt(0);
	}
}
