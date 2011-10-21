package p2mp;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

/**
 * @author arun
 */
class UDPClient
{
	
	public static void main(String args[]) {

		 // Initialize the parameters given using the command-line
	   	ArrayList<String> serverIPs = new ArrayList<String>();
	   	Integer portNumber = Integer.parseInt(args[0]);
	   	int i=0;
	   	for(i=1; args[i].contains(".") == true && args[i].contains(".txt")==false;++i){
	   		serverIPs.add(args[i].toString());
	   	}
	   	String fileName = args[i];
	   	String windowSize = args[i+1];
	   	String MSS = args[i+2];
	   	
	   	// Start timer thread
	   	//Thread timerThread = new Thread(new Timer());
	   	//timerThread.start();
	   	
	   	// Start byte send
	   	
	   	
	   	
	}
	
	
	
	
	
	
/*
   @SuppressWarnings("null")
public static void main(String args[]) throws Exception
   {
	   
	    // Initialize the parameters given using the command-line
	   	ArrayList<String> serverIPs = new ArrayList<String>();
	   	Integer portNumber = Integer.parseInt(args[0]);
	   	int i=0;
	   	for(i=1; args[i].contains(".") == true && args[i].contains(".txt")==false;++i){
	   		serverIPs.add(args[i].toString());
	   	}
	   	String fileName = args[i];
	   	String windowSize = args[i+1];
	   	String MSS = args[i+2];
	   	
	   	File file = new File(fileName);
	   	FileInputStream fileIN = new FileInputStream(file);
	   	byte[] fileData = new byte[(int)file.length()];
	   	fileIN.read(fileData);
	   	//System.out.println(fileData);
	   	fileIN.close();
		int packetSize = Math.min(Integer.parseInt(MSS), fileData.length);
	   	int totalBytesLeftToBeSent = fileData.length;
	   	
      //BufferedReader inFromUser =
      //   new BufferedReader(new InputStreamReader(System.in));
      DatagramSocket clientSocket = new DatagramSocket();
      //InetAddress IPAddress = InetAddress.getByName("localhost");
      int startPos = 0;
      while(totalBytesLeftToBeSent > 0){

          byte[] sendData = new byte[packetSize];
          byte[] receiveData = new byte[1024];
          // String sentence = inFromUser.readLine();
          //  sendData = sentence.getBytes();
          //sendData = "Hello".getBytes();
          int count = 0;
          for(int c = startPos; c < startPos + packetSize; ++c){
        	  sendData[count++]=(byte)fileData[c];
          }
          for(int i1 = 0; i1 < serverIPs.size(); ++i1){
           DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(serverIPs.get(i1)), portNumber);
           clientSocket.send(sendPacket);
           DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
           clientSocket.receive(receivePacket);
           String modifiedSentence = new String(receivePacket.getData());
           System.out.println("FROM SERVER:("+i1+"):" + modifiedSentence);
          }
    	  totalBytesLeftToBeSent -= packetSize;
    	  startPos = startPos + packetSize;
          packetSize = Math.min(Integer.parseInt(MSS), totalBytesLeftToBeSent);
      }
      clientSocket.close();
 
   }
   
   */
}
