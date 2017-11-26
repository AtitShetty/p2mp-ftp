package p2mp.server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.BitSet;

import p2mp.shared.Packet;

public class Server extends Thread {
	private DatagramSocket socket;
	private InetAddress address;
	private int port;
	private static double p;
	private int curSequence = -1;
	private String nextSequence;
	public static final BitSet DATA_PACKET = new BitSet(16) {
		/**
		 * 
		 */
		private static final long serialVersionUID = -1458577492519184881L;

		/**
		 * 
		 */


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
		private static final long serialVersionUID = -5394736956744448951L;

		/**
		 * 
		 */


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
	private static final BitSet ACK_CHECKSUM = new BitSet(16);
	protected String fileName;

	public Server(int port, String fileName, double p) throws SocketException {
		this.port = port;
		this.socket = new DatagramSocket(port);
		this.fileName = fileName;
		this.p = p;
	}

	public void run() {

		System.out.println("Server thread started");
		System.out.println(this.socket.getLocalAddress());
		System.out.println(this.socket.getLocalPort());
		while (true) {
			try {
				byte[] buffer = new byte[2048];
				DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

				this.socket.receive(packet);
				double r = Math.random();
				// if (r > p) {
					rcv_data(packet);
				// }
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
		}
	}

	private void send_ack(InetAddress address, int port) {
		try {
			System.out.println("Sending ack....");
			byte[] sequenceNo = ByteBuffer.allocate(4).putInt(curSequence).array();
			byte[] chksum = ACK_CHECKSUM.toByteArray();
			byte[] packetType = ACK_PACKET.toByteArray();
			Packet ack = new Packet(sequenceNo, chksum, packetType, null);
			byte[] send_ack = convertObjectToByteArray(ack);
			DatagramPacket sendack = new DatagramPacket(send_ack, send_ack.length, address, port);
			socket.send(sendack);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	private void rcv_data(DatagramPacket packet) {
		try {
			System.out.println("Receiving data");
			Packet filepacket = (Packet) convertByteArrayToObject(packet.getData());
			InetAddress clientAddress = packet.getAddress();
			int clientPort = packet.getPort();
			System.out.println("received seq is"
					+ ByteBuffer.wrap(filepacket.sequenceNo).getInt());
			if (ByteBuffer.wrap(filepacket.sequenceNo).getInt() == curSequence) {
				send_ack(clientAddress, clientPort);
				return;
			}

			BitSet packetType = BitSet.valueOf(filepacket.packetType);
			System.out.println(packetType.toString());
			System.out.println(DATA_PACKET.toString());
			if (packetType.equals(DATA_PACKET)) {
				System.out.println("packetType matched");
				String checkChk = new String(calculateChecksum(filepacket.data));
				String chk = new String(filepacket.checksum);
				if (chk.equals(checkChk)) {
					System.out.println("checksum");
					curSequence = ByteBuffer.wrap(filepacket.sequenceNo).getInt();
					System.out.println("now sequenc is " + curSequence);
					FileOutputStream os = new FileOutputStream(Paths.get(this.fileName).toString());
					os.write(filepacket.data);
					send_ack(clientAddress, clientPort);
					os.close();
				}
			}
		} catch (Exception e) {
			System.out.println("exception in rcv" + Arrays.toString(e.getStackTrace()));
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

}
