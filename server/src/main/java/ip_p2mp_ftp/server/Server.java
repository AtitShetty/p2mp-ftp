package ip_p2mp_ftp.server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;

public class Server extends Thread {
	private DatagramSocket socket;
	private InetAddress address;
	private int port;
	private static double p;
	private String sequence;
	public static final String ACK_PACKET = "1010101010101010";
	public static final String CHECKSUM = "0000000000000000";


	public Server() throws SocketException {
		socket = new DatagramSocket(7735);
	}

	public void run() {
		try {
			while (true) {
				DatagramPacket packet = new DatagramPacket(new byte[256], 256);
				this.socket.receive(packet);

				address = packet.getAddress();
				port = packet.getPort();
				String request = new String(packet.getData(), 0, packet.getLength());
				System.out.println(request);
				String response = "Received:" + request;
				socket.send(new DatagramPacket(response.getBytes(), response.getBytes().length, address, port));
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	private void send_ack(int s) {
		try {
			byte[] sequenceNo = ByteBuffer.allocate(4).putInt(Integer.parseInt(sequence, 2)).array();
			byte[] chksum = ByteBuffer.allocate(2).putShort(Short.parseShort(CHECKSUM, 2)).array();
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
			double r = Math.random();
			/*if(r>p){
				byte[] buffer = new byte[2048];
				DatagramPacket response = new DatagramPacket(buffer, buffer.length);
				this.socket.receive(response);

				Packet ackResponse = (Packet) SendFiles.convertByteArrayToObject(response.getData());

				Short packetType = Short.valueOf(ByteBuffer.allocate(ackResponse.packetType.length).getShort());

				if (packetType == packetTypeAck) {
			}*/
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
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
