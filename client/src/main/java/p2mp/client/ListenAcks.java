package p2mp.client;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class ListenAcks implements Runnable {

	protected DatagramSocket socket;

	protected Thread requestThread;

	public ListenAcks(DatagramSocket socket, Thread requestThread) {
		this.socket = socket;
		this.requestThread = requestThread;
	}

	public void start() {
		this.run();
	}

	public void run() {
		System.out.println("ListenAcks thread started");

		Short packetTypeAck = Short.parseShort(SendFiles.ACK_PACKET, 2);

		while (true) {
			try {
				byte[] buffer = new byte[2048];
				DatagramPacket response = new DatagramPacket(buffer, buffer.length);
				this.socket.receive(response);

				Packet ackResponse = (Packet) SendFiles.convertByteArrayToObject(response.getData());

				Short packetType = Short.valueOf(ByteBuffer.allocate(ackResponse.packetType.length).getShort());

				if (packetType == packetTypeAck) {
					int ackNo = Integer.valueOf(ByteBuffer.allocate(ackResponse.sequenceNo.length).getInt());

					String serverName = response.getAddress().getHostAddress();

					SendFiles.ackMap.get(ackNo).remove(serverName);

					System.out.println("Acked received for packet " + ackNo + " from " + serverName);

					System.out.println("Acks waiting from " + Arrays.toString(SendFiles.ackMap.get(ackNo).toArray()));

					if (SendFiles.ackMap.get(ackNo).isEmpty()) {
						this.requestThread.interrupt();
					}
				}

			} catch (Exception e) {
				System.out.println(
						"Exception in ListenAcks while reading response:\n" + Arrays.toString(e.getStackTrace()));
			}
		}

	}

}
