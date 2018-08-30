package io.pkts.packet.impl;

import java.io.IOException;

import io.pkts.buffer.Buffer;
import io.pkts.frame.UnknownEtherType;
import io.pkts.framer.EthernetFramer;
import io.pkts.framer.EthernetFramer.EtherType;
import io.pkts.framer.VlanFramer;
import io.pkts.packet.EthernetPacket;
import io.pkts.packet.IPPacket;
import io.pkts.packet.PCapPacket;
import io.pkts.packet.Packet;
import io.pkts.packet.PacketParseException;
import io.pkts.protocol.Protocol;

public class EthernetPacketImpl extends MACPacketImpl implements EthernetPacket {

	private VlanFramer vlanFramer = new VlanFramer();
	
	/**
	 * Creates a new {@link EthernetPacketImpl} and it assumes ethernet II and it
	 * does not check whether or not the ethertype is a known type. This method
	 * should only be used by the internal packet creating functions such as the
	 * {@link TransportPacketFactoryImpl} or the framers.
	 * 
	 * @param parent
	 * @param headers
	 * @return
	 */
	public static EthernetPacketImpl create(final PCapPacket parent, final Buffer headers) {
		if (headers.capacity() != 14) {
			throw new IllegalArgumentException("Not enough bytes to create this header");
		}

		if (parent == null) {
			throw new IllegalArgumentException("The parent packet cannot be null");
		}

		return new EthernetPacketImpl(parent, headers, null);
	}

	public EthernetPacketImpl(PCapPacket parent, Buffer headers, Buffer payload) {
		this(Protocol.ETHERNET_II, parent, headers, payload);
	}
	
	protected EthernetPacketImpl(Protocol protocol, PCapPacket parent, Buffer headers, Buffer payload) {
		super(protocol, parent, headers, payload);
	}

	@Override
	public EtherType getEthernetProtocol() {
		try {
			final byte b1 = headers.getByte(12);
			final byte b2 = headers.getByte(13);
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
	public EthernetPacket clone() {
		final PCapPacket pkt = this.parent.clone();
		return new EthernetPacketImpl(pkt, this.headers.clone(), getPayload().clone());
	}

	public Protocol getNextProtocol() throws IOException {
    if (getProtocol() == Protocol.ETHERNET_II) {
        final byte b1 = headers.getByte(12);
        final byte b2 = headers.getByte(13);
        EthernetFramer.EtherType etherType;
        try {
            etherType = EthernetFramer.getEtherType(b1, b2);
        } catch (UnknownEtherType e) {
            throw new PacketParseException(12, String.format("Unknown Ethernet type 0x%x%x", b1, b2));
        }
        switch (etherType) {
            case IPv4:
                return Protocol.IPv4;
            case IPv6:
                return Protocol.IPv6;
            case ARP:
                return Protocol.ARP;
            case DOT1Q:
            		return Protocol.DOT1Q;
            default:
                return Protocol.UNKNOWN;
        }
    } else {
        // TODO: figure out how an SLL packet indicates IPv4 vs IPv6
        return Protocol.IPv4;
    }
  }
	
  @Override
  public Packet getNextPacket() throws IOException {
      final Buffer payload = getPayload();
      if (payload == null) {
          return null;
      }
      switch (getNextProtocol()) {
          case IPv4:
              return ipv4Framer.frame(this, payload);
          case IPv6:
              return ipv6Framer.frame(this, payload);
          case DOT1Q:
          		return vlanFramer.frame(this, payload);
          default:
              return null;
      }
  }

}
