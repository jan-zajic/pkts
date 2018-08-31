package io.pkts.pcapng;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;

import org.junit.Test;

import io.pkts.PktsTestBase;
import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;
import io.pkts.frame.PcapGlobalHeader;

public class PcapNgTest {

	public static String RARP_REQ_REPLY = "rarp_req_reply.pcapng";
	
	protected ByteOrder defaultByteOrder;
	protected PcapGlobalHeader defaultPcapHeader;
	
	@Test
	public void testParseGlobalHeader() throws IOException {
		final InputStream stream = PktsTestBase.class.getResourceAsStream(RARP_REQ_REPLY);
		Buffer pcapStream = Buffers.wrap(stream);
		this.defaultPcapHeader = PcapGlobalHeader.parse(pcapStream);
    this.defaultByteOrder = this.defaultPcapHeader.getByteOrder();
	}
	
}
