package p2mp.server;

import java.io.File;
import java.nio.file.Paths;

public class Main {
	public static void main(String[] args) throws Exception {
		// p2mpserver port# file-name p
		for (String arg : args) {
			System.out.println(arg);
		}
		if (args.length != 3) {
			throw new Exception("Check input format");
		}

		File dest = new File(Paths.get(args[2]).toString());
		dest.createNewFile();

		Server server = new Server(Integer.parseInt(args[0]), args[1], Double.parseDouble(args[2]));

		server.start();
	}

}
