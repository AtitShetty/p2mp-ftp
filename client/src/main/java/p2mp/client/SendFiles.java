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
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import p2mp.shared.Packet;

public class SendFiles implements Runnable {

	protected static Map<Integer, Set<String>> ackMap = Collections
			.synchronizedMap(new HashMap<Integer, Set<String>>());

	protected Map<String, Long> rttMap = new HashMap<String, Long>();

	public static final BitSet DATA_PACKET = new BitSet(16) {
		/**
		 * 
		 */
		private static final long serialVersionUID = 5509594355442936364L;

		{
			set(0);
			set(2);
			set(4);
			set(6);
			set(8);
			set(10);
			set(12);
			set(14);
		}
	};

	public static final BitSet ACK_PACKET = new BitSet(16) {
		/**
		 * 
		 */
		private static final long serialVersionUID = -1075747658132666370L;

		{
			set(1);
			set(3);
			set(5);
			set(7);
			set(9);
			set(11);
			set(13);
			set(15);
		}
	};

	public static final BitSet RTT_PACKET = new BitSet(16) {
		/**
		 * 
		 */
		private static final long serialVersionUID = -1075747658132666370L;

		{
			set(8);
			set(9);
			set(10);
			set(11);
			set(12);
			set(13);
			set(14);
			set(15);
		}
	};

	protected DatagramSocket socket;

	protected String fileName;

	protected int mss;

	protected Set<String> servers;

	protected int port;

	protected long timeout;

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

		System.out.println("Client started");
		// System.out.println(Paths.get(this.fileName).toString());

		try {
			findRTT();
			Thread responseThread = new Thread(new ListenAcks(socket, Thread.currentThread()));
			responseThread.start();
			responseThread.join(1);
			FileInputStream is = new FileInputStream(Paths.get(this.fileName).toString());
			byte[] buffer = new byte[this.mss];
			@SuppressWarnings("unused")
			int remaining = 0;
			int segmentNo = 0;

			Set<String> serverIpAddresses = new HashSet<String>();

			for (String s : servers) {
				serverIpAddresses.add(InetAddress.getByName(s).getHostAddress());
			}
			Instant start = Instant.now();
			while ((remaining = is.read(buffer)) != -1) {

				SendFiles.ackMap.put(segmentNo, Collections.synchronizedSet(new HashSet<String>(serverIpAddresses)));
				while (true) {
					sendPacketsToServers(buffer, segmentNo);
					try {
						Thread.sleep(this.timeout);
					} catch (InterruptedException e) {
						System.out.println("All Acks Received.");
						SendFiles.ackMap.remove(segmentNo);
						break;
					}
					System.out.println("Timeout, sequence number = " + segmentNo);

				}
				segmentNo += 1;

			}
			Instant end = Instant.now();
			long fileSize = Paths.get(this.fileName).toFile().length();
			System.out.println("File " + this.fileName + " of size " + fileSize + " bytes sent successfully in "
					+ Duration.between(start, end).toMillis() + " ms.");
			is.close();
		} catch (Exception e) {
			System.out
					.println("Exception while sending file in SendFiles run():\n" + Arrays.toString(e.getStackTrace()));
		} finally {
			SendFiles.ackMap.clear();

		}

	}

	private void sendPacketsToServers(byte[] data, int segmentNo) throws IOException {

		System.out.println("Sending packet: " + segmentNo);
		byte[] segmentNumber = ByteBuffer.allocate(4).putInt(segmentNo).array();
		byte[] packetType = DATA_PACKET.toByteArray();
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

		return ByteBuffer.allocate(4).putInt(crc).array();

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

	private void findRTT() throws IOException {
		System.out.println("Determining RTT");
		for (String s : this.servers) {

			Packet request = new Packet();
			request.packetType = RTT_PACKET.toByteArray().clone();
			byte[] requestBuf = convertObjectToByteArray(request);
			DatagramPacket requestPacket = new DatagramPacket(requestBuf, requestBuf.length, InetAddress.getByName(s),
					this.port);
			this.socket.send(requestPacket);
			Instant start = Instant.now();

			DatagramPacket responsePacket = new DatagramPacket(new byte[2048], 2048);
			this.socket.receive(responsePacket);

			Instant end = Instant.now();

			long rtt = Duration.between(start, end).toMillis();

			System.out.println("RTT of " + s + " is " + rtt + " ms.");

			rttMap.put(s, rtt);
		}
		System.out.println();
		
		long timeout = this.timeout;
		
		long average = 0;
		
		for(String s : rttMap.keySet()) {
			average += rttMap.get(s);
		}
		
		average = average / rttMap.size();
		
		if(average > timeout) {
			timeout = (long)Math.ceil(1.25*average);			
		}
		
		this.timeout = timeout;

		System.out.println("Timeout is " + timeout);
	}

}
