package ip_p2mp_ftp.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Main {

	public static void main(String[] args) throws IOException {
		DatagramSocket socket = new DatagramSocket();
		String request = "Hey server";
		DatagramPacket packet = new DatagramPacket(request.getBytes(), request.getBytes().length,
				InetAddress.getLocalHost(), 7735);
		socket.send(packet);

		DatagramPacket responsePacket = new DatagramPacket(new byte[2048], 2048);
		socket.receive(responsePacket);
		System.out.println(new String(responsePacket.getData(), 0, responsePacket.getLength()));
		socket.close();
	}

}
