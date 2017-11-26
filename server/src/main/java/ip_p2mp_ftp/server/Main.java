package ip_p2mp_ftp.server;

import java.net.SocketException;

public class Main {
	public static void main(String[] args) throws Exception {
		//p2mpserver port# file-name p
		for(String arg: args){
			System.out.println(arg);
		}
		if (args.length != 3) {
			throw new Exception("Check input format");
		}

		Server server = new Server(Integer.parseInt(args[0]), args[1], Double.parseDouble(args[2]));
		server.start();
	}
}
