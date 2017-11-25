package p2mp.client;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Main {

	public static void main(String[] args) throws IOException, InterruptedException {
		Set<String> servers = new HashSet<String>();
		int port = 0;
		String fileName = null;
		int mss = 500;
		
		for (int i = 0; i < args.length; i++) {
			
			try {
				servers.add(InetAddress.getByName(args[i]).getHostAddress());
			} catch (UnknownHostException e) {
				System.out.println("File will be sent to following servers: " + Arrays.toString(servers.toArray()));
				port = Integer.parseInt(args[i]);
				fileName = args[i + 1];
				mss = Integer.parseInt(args[i + 2]);
			}
		}

		DatagramSocket socket = new DatagramSocket();

		Thread requestThread = new Thread(new SendFiles(socket, fileName, mss, servers, port, 5000));
		requestThread.start();
		requestThread.join(1);

		Thread responseThread = new Thread(new ListenAcks(socket, requestThread));
		responseThread.start();
		responseThread.join(1);

		System.out.println("Both Threads started");
	}


}
