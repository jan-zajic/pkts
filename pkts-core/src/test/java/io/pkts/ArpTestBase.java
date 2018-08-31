package io.pkts;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.io.InputStream;
import java.nio.ByteOrder;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;

import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;
import io.pkts.frame.PcapGlobalHeader;
import io.pkts.framer.FramerManager;
import io.pkts.framer.PcapFramer;
import io.pkts.packet.PCapPacket;

/**
 * Test base for all tests regarding arp framing and parsing
 * 
 * @author jan-zajic@github.com
 * 
 */
public class ArpTestBase {
	
    protected FramerManager framerManager;

    /**
     * Default stream pointing to a pcap that contains some sip traffic
     */
    protected Buffer pcapStream;

    protected ByteOrder defaultByteOrder;

    /**
     * The default header for the default pcap stream
     */
    protected PcapGlobalHeader defaultPcapHeader;

    /**
     * The default pcap frame
     */
    protected PCapPacket defaultPcapPacket;

    /**
     * A full ethernet frame wrapped in a buffer. We will slice out the other
     * frames out of this one so that individual test cases can use the the raw
     * data with ease. All of the indices have been taken from wireshark
     */
    protected Buffer ethernetFrameBuffer;
    
    /**
     * A raw arp frame buffer
     */
    protected Buffer arpFrameBuffer;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        this.framerManager = FramerManager.getInstance();
        final InputStream stream = PktsTestBase.class.getResourceAsStream("arp.pcap");

        this.pcapStream = Buffers.wrap(stream);

        // Get the first frame for tests to use. Since this actually will
        // use the PcapFramer to frame it, make sure the PcapFramer isn't
        // broken so do some assertions on it...
        this.defaultPcapHeader = PcapGlobalHeader.parse(this.pcapStream);
        this.defaultByteOrder = this.defaultPcapHeader.getByteOrder();
        final PcapFramer framer = new PcapFramer(this.defaultPcapHeader, this.framerManager);
        this.defaultPcapPacket = framer.frame(null, this.pcapStream);
        this.ethernetFrameBuffer = this.defaultPcapPacket.getPayload();
        assertThat(this.ethernetFrameBuffer.capacity(), is(60));

        this.arpFrameBuffer = this.ethernetFrameBuffer.slice(14, this.ethernetFrameBuffer.capacity());
    }
    
    @After
    public void tearDown() throws Exception {
    }

}