package ip_p2mp_ftp.server;

import java.net.SocketException;

public class Main {
	public static void main(String[] args) throws SocketException {
		Server server = new Server();
		server.start();
	}
}
