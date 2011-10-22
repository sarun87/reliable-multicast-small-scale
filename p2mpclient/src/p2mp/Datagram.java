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

    public Datagram(byte[] data, int dataSize, boolean isDataPacket) {
        sequenceNumber = ByteBuffer.allocate(4).putInt(DataRepository.getNextSequenceNumber()).array();
        if (isDataPacket) {
            datagramType = ByteBuffer.allocate(2).putInt(DataRepository.DATAPACKET).array();
        } else {
            datagramType = ByteBuffer.allocate(2).putInt(DataRepository.ACKPACKET).array();
        }
        this.data = data;
        checksum = ByteBuffer.allocate(2).putLong(InternetChecksum.getCheckSum()).array();
        this.dataSize = ByteBuffer.allocate(4).putInt(dataSize).array();
    }

    public byte[] getBytes() {
        byte[] temp = new byte[sequenceNumber.length + checksum.length + datagramType.length + dataSize.length + data.length];
        System.arraycopy(sequenceNumber, 0, temp, 0, sequenceNumber.length);
        System.arraycopy(checksum, 0, temp, sequenceNumber.length, checksum.length);
        System.arraycopy(datagramType, 0, temp, sequenceNumber.length + checksum.length, datagramType.length);
        System.arraycopy(dataSize, 0, temp, sequenceNumber.length + checksum.length + datagramType.length, dataSize.length);
        System.arraycopy(data, 0, temp, sequenceNumber.length + checksum.length + datagramType.length + dataSize.length, data.length);
        
        return temp;
    }
}
