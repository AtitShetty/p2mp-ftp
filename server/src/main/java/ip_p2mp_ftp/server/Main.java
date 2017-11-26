package ip_p2mp_ftp.server;

import java.net.SocketException;

public class Main {
	public static void main(String[] args) throws Exception {
		//p2mpserver port# file-name p
		if (args.length != 4) {
			throw new Exception("Check input format");
		}

		Server server = new Server(Integer.parseInt(args[1]), args[2], Double.parseDouble(args[3]));
		server.start();
	}
}
