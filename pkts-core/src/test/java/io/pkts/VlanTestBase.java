package io.pkts;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;

import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;
import io.pkts.frame.Frame;
import io.pkts.frame.PcapGlobalHeader;
import io.pkts.framer.FramerManager;
import io.pkts.framer.PcapFramer;
import io.pkts.packet.IPv4Packet;
import io.pkts.packet.PCapPacket;
import io.pkts.packet.Packet;
import io.pkts.packet.PacketParseException;
import io.pkts.packet.sip.SipPacket;
import io.pkts.protocol.Protocol;

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

    public List<Packet> loadStream(final String streamName) throws Exception {
        final InputStream stream = PktsTestBase.class.getResourceAsStream(streamName);
        final Pcap pcap = Pcap.openStream(stream);
        final List<Packet> packets = new ArrayList<Packet>();
        pcap.loop(new PacketHandler() {
            @Override
            public boolean nextPacket(final Packet packet) {
                packets.add(packet);
                return true;
            }
        });
        pcap.close();
        return packets;
    }

    public List<IPv4Packet> loadIPPackets(final String streamName) throws Exception {
        final List<Packet> packets = loadStream(streamName);
        final List<IPv4Packet> ipPackets = new ArrayList<>();
        for (final Packet packet : packets) {
            final IPv4Packet ip = (IPv4Packet) packet.getPacket(Protocol.IPv4);
            ipPackets.add(ip);
        }
        return ipPackets;
    }

    /**
     * Helper class that simply just counts the number of SIP requests.
     * 
     */
    public static class MethodCalculator implements PacketHandler {
        public int total;
        public int invite;
        public int bye;
        public int ack;
        public int cancel;

        @Override
        public boolean nextPacket(final Packet packet) {
            try {
                final SipPacket msg = (SipPacket) packet.getPacket(Protocol.SIP);
                ++this.total;
                if (msg.isRequest()) {
                    if (msg.isInvite()) {
                        ++this.invite;
                    } else if (msg.isBye()) {
                        ++this.bye;
                    } else if (msg.isAck()) {
                        ++this.ack;
                    } else if (msg.isCancel()) {
                        ++this.cancel;
                    }
                }
            } catch (final IOException e) {
                fail("Got an IOException in my test " + e.getMessage());
            } catch (final PacketParseException e) {
                fail("Got a PacketParseException in my test " + e.getMessage());
            }
            return true;
        }

    }

    /**
     * Helper class that will write either {@link Frame}s or {@link Packet} to
     * the output stream. It will ONLY write INVITE and BYE messages.
     */
    public static class TestWriteStreamHandler implements PacketHandler {

        private final PcapOutputStream out;

        /**
         * 
         * @param out
         *            the output stream to write to
         */
        public TestWriteStreamHandler(final PcapOutputStream out) {
            this.out = out;
        }

        @Override
        public boolean nextPacket(final Packet packet) {
            try {
                // only write out INVITE and BYE requests
                final SipPacket msg = (SipPacket) packet.getPacket(Protocol.SIP);
                final String method = msg.getMethod().toString();
                final boolean isInviteOrBye = "INVITE".equals(method) || "BYE".equals(method);
                if (msg.isRequest() && isInviteOrBye) {
                    // final TransportPacket pkt = (TransportPacket) msg.getPacket(Protocol.UDP);
                    this.out.write(msg);
                }
            } catch (final IOException e) {
                fail("Got an IOException in my test " + e.getMessage());
            } catch (final PacketParseException e) {
                fail("Got a PacketParseException in my test " + e.getMessage());
            }

            return true;
        }
    }

}