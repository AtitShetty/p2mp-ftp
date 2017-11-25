package ip_p2mp_ftp.server;

import java.io.Serializable;

public class Packet implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8150286908083682999L;

	public byte[] sequenceNo;

	public byte[] checksum;

	public byte[] packetType;

	public byte[] data;

	public Packet(byte[] sequenceNo, byte[] checksum, byte[] packetType) {
		this.sequenceNo = sequenceNo;
		this.checksum = checksum;
		this.packetType = packetType;
		//this.data = data;
	}

	public Packet() {
		this(new byte[4], new byte[2], new byte[2]);
	}

}
