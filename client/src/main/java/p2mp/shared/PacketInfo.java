package p2mp.shared;

import java.time.Instant;

public class PacketInfo {
	public Instant start;
	public Instant end;
	public int fileSize;

	public PacketInfo(Instant start, int fileSize) {
		this.start = start;
		this.fileSize = fileSize;
	}

	public PacketInfo() {

	}
}
