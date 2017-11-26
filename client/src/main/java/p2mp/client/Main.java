package p2mp.client;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;

public class Main {

	public static void main(String[] args) throws Exception {

		int argsSize = args.length;

		if (argsSize < 4) {
			throw new Exception("Check input format");
		}

		Set<String> servers = new HashSet<String>();
		int port = Integer.parseInt(args[argsSize - 3]);
		String fileName = args[argsSize - 2];
		int mss = Integer.parseInt(args[argsSize - 1]);
		// System.out.println(Arrays.toString(args));
		
		for (int i = argsSize - 4; i > 0; i--) {
			
			try {
				servers.add(InetAddress.getByName(args[i]).getHostAddress());
			} catch (UnknownHostException e) {
				throw new Exception("Check Host addresses or input format");
			}
		}

		// System.out.println("Port is: " + port);
		// System.out.println("Filename is: " + fileName);
		// System.out.println("mss is:" + mss);
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
