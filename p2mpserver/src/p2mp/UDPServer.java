package p2mp;

import java.net.*;

/**
 * 
 * @author arun
 *
 */
class UDPServer
{
   public static void main(String args[]) throws Exception
      {
	    // Initialize the parameters given using the command-line
	   	Integer portNo = Integer.parseInt(args[0]);
	   	String fileName = args[1];
	   	String windowSize = args[2];
	   	String packetLossProbability = args[3];
	   	
	   	
         DatagramSocket serverSocket = new DatagramSocket(portNo);
            byte[] receiveData = new byte[1024];
            byte[] sendData = new byte[1024];
            while(true)
               {
                  DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                  serverSocket.receive(receivePacket);
                  String sentence = new String( receivePacket.getData());
                  System.out.println("RECEIVED: " + sentence);
                  InetAddress IPAddress = receivePacket.getAddress();
                  int port = receivePacket.getPort();
                  String capitalizedSentence = "Received by Server:"+InetAddress.getLocalHost().toString()+"Bytes:"+sentence.length();
                  sendData = capitalizedSentence.getBytes();
                  DatagramPacket sendPacket =
                  new DatagramPacket(sendData, sendData.length, IPAddress, port);
                  serverSocket.send(sendPacket);
               }
      }
}
