package ip_p2mp_ftp.server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
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

public class Server extends Thread {
	private DatagramSocket socket;
	private InetAddress address;
	private int port;
	private static double p;
	private String curSequence;
	private String nextSequence;
	private static final String ACK_PACKET = "1010101010101010";
	private static final String DATA_PACKET = "0101010101010101";
	private static final String ACK_CHECKSUM = "0000000000000000";
	protected String fileName;

	public Server(int port, String fileName, double p) throws SocketException {
		this.port = port;
		socket = new DatagramSocket(port);
		this.fileName = fileName;
		this.p = p;
	}

	public void run() {


		while (true) {
			try {
				byte[] buffer = new byte[2048];
				DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
				this.socket.receive(packet);
				address = packet.getAddress();
				port = packet.getPort();
				double r = Math.random();
				if (r > p) {
					rcv_data(packet);
				}
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
		}
	}

	private void send_ack() {
		try {
			System.out.println("Sending ack....");
			byte[] sequenceNo = ByteBuffer.allocate(4).putInt(Integer.parseInt(curSequence, 2)).array();
			byte[] chksum = ByteBuffer.allocate(2).putShort(Short.parseShort(ACK_CHECKSUM, 2)).array();
			byte[] packetType = ByteBuffer.allocate(2).putShort(Short.parseShort(ACK_PACKET, 2)).array();
			Packet ack = new Packet(sequenceNo, chksum, packetType);
			byte[] send_ack = convertObjectToByteArray(ack);
			DatagramPacket sendack = new DatagramPacket(send_ack, send_ack.length, address, port);
			socket.send(sendack);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	private void rcv_data(DatagramPacket packet) {
		try {
			System.out.println("Receiving data....");
			Packet filepacket = (Packet) convertByteArrayToObject(packet.getData());

			if (new String(filepacket.sequenceNo).equals(curSequence)) {
				send_ack();
				return;
			}

			Short packetType = Short.valueOf(ByteBuffer.allocate(filepacket.packetType.length).getShort());
			Short datatype = Short.parseShort(DATA_PACKET);
			if (packetType == datatype) {
				String checkChk = new String(calculateChecksum(filepacket.data));
				String Chk = new String(filepacket.checksum);
				if (Chk.equals(checkChk)) {
					curSequence = new String(filepacket.sequenceNo);
					FileOutputStream os = new FileOutputStream(Paths.get(this.fileName).toString());
					os.write(filepacket.data);
					send_ack();
				}
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
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
