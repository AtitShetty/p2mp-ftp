package ip_p2mp_ftp.server;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class Server extends Thread {
	private DatagramSocket socket;

	public Server() throws SocketException {
		socket = new DatagramSocket(7735);
	}

	public void run() {
		try {
			while (true) {
				DatagramPacket packet = new DatagramPacket(new byte[256], 256);
				this.socket.receive(packet);

				InetAddress address = packet.getAddress();
				int port = packet.getPort();
				String request = new String(packet.getData(), 0, packet.getLength());
				System.out.println(request);
				String response = "Received:" + request;
				socket.send(new DatagramPacket(response.getBytes(), response.getBytes().length, address, port));
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
}
