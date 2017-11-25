package p2mp.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SendFiles implements Runnable {

	protected static Map<Integer, Set<String>> ackMap = Collections
			.synchronizedMap(new HashMap<Integer, Set<String>>());

	public static final String DATA_PACKET = "0101010101010101";

	public static final String ACK_PACKET = "1010101010101010";

	protected DatagramSocket socket;

	protected String fileName;

	protected int mss;

	protected Set<String> servers;

	protected int port;

	protected int timeout;

	public SendFiles(DatagramSocket socket, String fileName, int mss, Set<String> servers, int port, int timeout) {
		this.socket = socket;
		this.fileName = fileName;
		this.mss = mss;
		this.servers = servers;
		this.port = port;
		this.timeout = timeout;
	}

	public void start() {
		this.run();

	}

	public void run() {

		System.out.println("SendFiles thread started");
		try {
			FileInputStream is = new FileInputStream(Paths.get(this.fileName).toString());
			byte[] buffer = new byte[this.mss];
			@SuppressWarnings("unused")
			int remaining = 0;
			int segmentNo = 0;

			Set<String> serverIpAddresses = new HashSet<String>();

			for (String s : servers) {
				serverIpAddresses.add(InetAddress.getByName(s).getHostAddress());
			}

			while ((remaining = is.read(buffer)) != -1) {

				SendFiles.ackMap.put(segmentNo,
						Collections.synchronizedSet(new HashSet<String>(serverIpAddresses)));
				while (true) {
					sendPacketsToServers(buffer, segmentNo);
					try {
						Thread.sleep(this.timeout);
					} catch (InterruptedException e) {
						System.out.println("All Acks Received.");
						SendFiles.ackMap.remove(segmentNo);
						break;
					}

				}
				segmentNo += 1;

			}
			is.close();
		} catch (Exception e) {
			System.out
					.println("Exception while sending file in SendFiles run():\n" + Arrays.toString(e.getStackTrace()));
		} finally {
			SendFiles.ackMap.clear();
			System.out.println("Thread SendFiles closed");
		}

	}

	private void sendPacketsToServers(byte[] data, int segmentNo) throws IOException {

		System.out.println("Sending packet: " + segmentNo);

		byte[] segmentNumber = ByteBuffer.allocate(4).putInt(segmentNo).array();
		byte[] packetType = ByteBuffer.allocate(2).putShort(Short.parseShort(DATA_PACKET, 2)).array();
		byte[] checksum = calculateChecksum(data);

		Packet segment = new Packet(segmentNumber, checksum, packetType, data);

		byte[] segmentArray = convertObjectToByteArray(segment);

		for (String s : SendFiles.ackMap.get(segmentNo)) {
			DatagramPacket dataPacket = new DatagramPacket(segmentArray, segmentArray.length, InetAddress.getByName(s),
					this.port);
			this.socket.send(dataPacket);
			SendFiles.ackMap.get(segmentNo).add(s);
		}
	}

	/**
	 * Below code is courtesy of
	 * https://stackoverflow.com/questions/13209364/convert-c-crc16-to-java-crc16
	 */
	private byte[] calculateChecksum(byte[] data) {

		int crc = 0xFFFF;

		for (int j = 0; j < data.length; j++) {
			crc = ((crc >>> 8) | (crc << 8)) & 0xffff;
			crc ^= (data[j] & 0xff);// byte to int, trunc sign
			crc ^= ((crc & 0xff) >> 4);
			crc ^= (crc << 12) & 0xffff;
			crc ^= ((crc & 0xFF) << 5) & 0xffff;
		}
		crc &= 0xffff;

		return ByteBuffer.allocate(2).putInt(crc).array();

	}

	public static byte[] convertObjectToByteArray(Object packet) throws IOException {

		byte[] result = null;
		ByteArrayOutputStream bos = null;
		ObjectOutputStream oos = null;
		try {
			bos = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(bos);
			oos.writeObject(packet);
			oos.flush();
			result = bos.toByteArray();
		} finally {
			if (oos != null) {
				oos.close();
			}
			if (bos != null) {
				bos.close();
			}
		}
		return result;
	}

	public static Object convertByteArrayToObject(byte[] data) throws IOException, ClassNotFoundException {
		Object obj = null;
		ByteArrayInputStream bis = null;
		ObjectInputStream ois = null;
		try {
			bis = new ByteArrayInputStream(data);
			ois = new ObjectInputStream(bis);
			obj = ois.readObject();
		} finally {
			if (bis != null) {
				bis.close();
			}
			if (ois != null) {
				ois.close();
			}
		}
		return obj;
	}

}
