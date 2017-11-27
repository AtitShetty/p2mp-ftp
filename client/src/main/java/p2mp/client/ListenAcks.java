package p2mp.client;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.BitSet;

import p2mp.shared.Packet;

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
		// System.out.println("ListenAcks thread started");

		while (true) {
			try {
				byte[] buffer = new byte[2048];
				DatagramPacket response = new DatagramPacket(buffer, buffer.length);
				this.socket.receive(response);

				Packet ackResponse = (Packet) SendFiles.convertByteArrayToObject(response.getData());

				BitSet packetType = BitSet.valueOf(ackResponse.packetType);

				if (packetType.equals(SendFiles.EOF)) {
					break;
				}

				if (packetType.equals(SendFiles.ACK_PACKET)) {
					int ackNo = Integer.valueOf(ByteBuffer.wrap(ackResponse.sequenceNo).getInt());

					String serverName = response.getAddress().getHostAddress();

					SendFiles.ackMap.get(ackNo).remove(serverName);

					System.out.println("ACK received for packet " + ackNo + " from " + serverName);

					System.out.println("ACKs waiting from " + Arrays.toString(SendFiles.ackMap.get(ackNo).toArray()));

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
