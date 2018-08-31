/**
 * 
 */
package io.pkts.packet.arp.impl;

import java.io.IOException;
import java.io.OutputStream;

import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;
import io.pkts.frame.UnknownEtherType;
import io.pkts.framer.EthernetFramer;
import io.pkts.framer.EthernetFramer.EtherType;
import io.pkts.packet.Packet;
import io.pkts.packet.PacketParseException;
import io.pkts.packet.arp.ArpPacket;
import io.pkts.packet.impl.AbstractPacket;
import io.pkts.protocol.Protocol;

/**
 * @author jan-zajic@github.com
 */
public class ArpPacketImpl extends AbstractPacket implements ArpPacket {

    protected final Packet parent;

    /**
     * If the headers are set then this overrides any of the source stuff set
     * above.
     */
    protected final Buffer headers;

    /**
     * Construct a new {@link ArpPacket} based on the supplied headers.
     * 
     */
    public ArpPacketImpl(final Packet parent, final Buffer headers, final Buffer payload) {
        super(Protocol.ARP, parent, payload);
        this.parent = parent;
        this.headers = headers;
    }

    @Override
    public EtherType getProtocolType() {
    	try {
  			final byte b1 = headers.getByte(2);
  			final byte b2 = headers.getByte(3);
  			try {
  				return EthernetFramer.getEtherType(b1, b2);
  			} catch (UnknownEtherType e) {
  				throw new PacketParseException(12, String.format("Unknown Ethernet type 0x%x%x", b1, b2));
  			}
  		} catch(IOException e) {
  			throw new RuntimeException("Unable to read data from the underlying Buffer.", e);
  		}
    }
    
    @Override
		public ArpOperation getOperation() {
    	try {
    		byte b = headers.getByte(7);
    		if(b == 1) {
    			return ArpOperation.REQUEST;
    		} else if (b == 2) {
    			return ArpOperation.RESPONSE;
    		} else {
    			throw new PacketParseException(7, "Illegal value for arp operation: "+b);
    		}
    	} catch(IOException e) {
  			throw new RuntimeException("Unable to read data from the underlying Buffer.", e);
  		}
		}
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final String getSenderMacAddress() {
        try {
            return toHexString(this.headers, 8, 6);
        } catch (final IOException e) {
            throw new RuntimeException("Unable to read data from the underlying Buffer.", e);
        }
    }

    public static String toHexString(final Buffer buffer, final int start, final int length) throws IOException {
        final StringBuilder sb = new StringBuilder();
        for (int i = start; i < start + length; ++i) {
            final byte b = buffer.getByte(i);
            sb.append(String.format("%02X", b));
            if (i < start + length - 1) {
                sb.append(":");
            }
        }
        return sb.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String getTargetMacAddress() {
        try {
            return toHexString(this.headers, 18, 6);
        } catch (final IOException e) {
            throw new RuntimeException("Unable to read data from the underlying Buffer.", e);
        }
    }

    @Override
    public short[] getSenderProtocolAddress() {
    	final short a = this.headers.getUnsignedByte(14);
      final short b = this.headers.getUnsignedByte(15);
      final short c = this.headers.getUnsignedByte(16);
      final short d = this.headers.getUnsignedByte(17);
      return new short[] {a,b,c,d};
    }
    
    @Override
    public short[] getTargetProtocolAddress() {
    	final short a = this.headers.getUnsignedByte(24);
      final short b = this.headers.getUnsignedByte(25);
      final short c = this.headers.getUnsignedByte(26);
      final short d = this.headers.getUnsignedByte(27);
      return new short[] {a,b,c,d};
    }
    
    @Override
		public String getSenderIpAddress() {
			EtherType protocolType = getProtocolType();
			if(protocolType == EtherType.IPv4) {
				short[] senderProtocolAddress = getSenderProtocolAddress();
				return senderProtocolAddress[0] + "." + senderProtocolAddress[1] + "." + senderProtocolAddress[2] + "." + senderProtocolAddress[3];
			} else {
				throw new RuntimeException("sender addres is not IP address, protocol type in arp packet is "+protocolType);
			}
		}

		@Override
		public String getTargetIpAddress() {
			EtherType protocolType = getProtocolType();
			if(protocolType == EtherType.IPv4) {
				short[] targetProtocolAddress = getTargetProtocolAddress();
				return targetProtocolAddress[0] + "." + targetProtocolAddress[1] + "." + targetProtocolAddress[2] + "." + targetProtocolAddress[3];
			} else {
				throw new RuntimeException("target addres is not IP address, protocol type in arp packet is "+protocolType);
			}
		}
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void verify() {
        // nothing to verify
    }

    @Override
    public long getArrivalTime() {
        return this.parent.getArrivalTime();
    }

    @Override
    public void write(final OutputStream out, final Buffer payload) throws IOException {
        this.parent.write(out, Buffers.wrap(this.headers, payload));
    }

		@Override
		public Packet getNextPacket() throws IOException, PacketParseException {
			return null;
		}

		@Override
		public Packet clone() {
			final Packet pkt = this.parent.clone();
			return new ArpPacketImpl(pkt, this.headers.clone(), getPayload().clone());
		}

}
