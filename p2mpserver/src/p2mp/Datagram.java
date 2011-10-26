/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package p2mp;

import java.nio.ByteBuffer;

public class Datagram {

    public byte[] sequenceNumber;
    public byte[] checksum;
    public byte[] datagramType;
    public byte[] data;
    public byte[] dataSize;

    public Datagram(byte[] data, int dataSize, boolean isDataPacket) {

    }
    public Datagram(){
    	data = null;
    	dataSize = ByteBuffer.allocate(4).putInt(0,0).array();
    	datagramType = null;
    	checksum = ByteBuffer.allocate(2).putChar((char)0).array();
    	sequenceNumber = ByteBuffer.allocate(4).putInt(0,0).array();
    	
    	
    }
    public byte[] getBytes() {
        byte[] temp = new byte[sequenceNumber.length + checksum.length + datagramType.length + dataSize.length];
        System.arraycopy(sequenceNumber, 0, temp, 0, sequenceNumber.length);
        System.arraycopy(checksum, 0, temp, sequenceNumber.length, checksum.length);
        System.arraycopy(datagramType, 0, temp, sequenceNumber.length + checksum.length, datagramType.length);
        System.arraycopy(dataSize, 0, temp, sequenceNumber.length + checksum.length + datagramType.length, dataSize.length);
        //System.arraycopy(data, 0, temp, sequenceNumber.length + checksum.length + datagramType.length + dataSize.length, data.length);
        
        return temp;
    }
}
