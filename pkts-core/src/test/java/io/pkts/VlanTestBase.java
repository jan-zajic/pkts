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
 * Test base for all tests regarding vlan framing and parsing
 * 
 * @author jan-zajic@github.com
 * 
 */
public class VlanTestBase {
	
    protected FramerManager framerManager;

    /**
     * Default stream pointing to a pcap that contains some sip traffic
     */
    protected Buffer pcapStream;

    protected ByteOrder defaultByteOrder;

    /**
     * Default frame that most tests can use to test their basic framing
     * abilities
     */
    protected Buffer defaultFrame;

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
     * A raw vlan frame buffer containing a 802.1Q packet
     */
    protected Buffer vlanFrameBuffer;
    
    /**
     * A raw ipv4 frame buffer
     */
    protected Buffer ipFrameBuffer;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        this.framerManager = FramerManager.getInstance();
        final InputStream stream = PktsTestBase.class.getResourceAsStream("dot1q.cap");

        this.pcapStream = Buffers.wrap(stream);

        // Get the first frame for tests to use. Since this actually will
        // use the PcapFramer to frame it, make sure the PcapFramer isn't
        // broken so do some assertions on it...
        this.defaultPcapHeader = PcapGlobalHeader.parse(this.pcapStream);
        this.defaultByteOrder = this.defaultPcapHeader.getByteOrder();
        final PcapFramer framer = new PcapFramer(this.defaultPcapHeader, this.framerManager);
        this.defaultPcapPacket = framer.frame(null, this.pcapStream);
        this.defaultFrame = this.defaultPcapPacket.getPayload();
        assertThat(this.defaultFrame.capacity(), is(68));

        this.ethernetFrameBuffer = this.defaultFrame;
        this.vlanFrameBuffer = this.ethernetFrameBuffer.slice(14, this.ethernetFrameBuffer.capacity());

        // slice out the individual payloads so that our tests can work
        // directly on this raw data.
        this.ipFrameBuffer = this.vlanFrameBuffer.slice(4, this.vlanFrameBuffer.capacity());
    }
    
    @After
    public void tearDown() throws Exception {
    }

}