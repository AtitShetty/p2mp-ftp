package ip_p2mp_ftp.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;

public class Server extends Thread {
	private DatagramSocket socket;
	private InetAddress address;
	private int port;
	private static double p;
	private String sequence;
		
	public Server() throws SocketException {
		socket = new DatagramSocket(7735);
	}

	public void run() {
		try {
			while (true) {
				DatagramPacket packet = new DatagramPacket(new byte[256], 256);
				this.socket.receive(packet);

				address = packet.getAddress();
				port = packet.getPort();
				String request = new String(packet.getData(), 0, packet.getLength());
				System.out.println(request);
				String response = "Received:" + request;
				socket.send(new DatagramPacket(response.getBytes(), response.getBytes().length, address, port));
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	

	 private void send_ack(int s) throws IOException
	 {
		 try{
	    	int ack = Integer.parseInt(sequence+"00000000000000001010101010101010", 2);
	    	byte[] send_ack = ByteBuffer.allocate(8).putInt(ack).array();
	    	DatagramPacket sendack = new DatagramPacket(send_ack, send_ack.length, address, port);
	    	socket.send(sendack);
		 }catch (Exception e) {
				System.out.println(e.getMessage());
		}
	 } 
}
