package ip_p2mp_ftp.client;

import java.io.IOException;
import java.net.DatagramSocket;
import java.util.HashSet;
import java.util.Set;

public class Main {

	public static void main(String[] args) throws IOException, InterruptedException {
		Set<String> servers = new HashSet<String>();
		servers.add("localhost");
		DatagramSocket socket = new DatagramSocket();
		
		Thread requestThread = new Thread(new SendFiles(socket, "", 500, servers, 7735, 5000));
		requestThread.start();
		requestThread.join(1);

		Thread responseThread = new Thread(new ListenAcks(socket, requestThread));
		responseThread.start();
		responseThread.join(1);

		System.out.println("Both Threads started");
	}


}
