package io.pkts.frame;

public class PcapngSectionHeader {

	private final int headerBlockLength;
	private final byte[] body;
	
  public PcapngSectionHeader(int headerBlockLength, byte[] body) {
		this.headerBlockLength = headerBlockLength;
		this.body = body;
	}

  
  
	public static int parseBlockLength(byte[] length, boolean isBigEndian) {
    if (isBigEndian) {
        int blockLength = (((length[0] << 32) & 0xFF) + ((length[1] << 16) & 0xFF) + ((length[2] << 8) & 0xFF) + (
                (length[3] << 0) & 0xFF));
        return blockLength;
    } else {
        int blockLength = (((length[0] << 0) & 0xFF) + ((length[1] << 8) & 0xFF00) + ((length[2] << 16) &
                0xFF0000) + ((length[3] << 32) & 0xFF000000));
        return blockLength;
    }
  }
	
}
