package io.pkts.packet.impl;

import java.io.IOException;

import io.pkts.buffer.Buffer;
import io.pkts.frame.UnknownEtherType;
import io.pkts.framer.EthernetFramer;
import io.pkts.framer.EthernetFramer.EtherType;
import io.pkts.packet.EthernetPacket;
import io.pkts.packet.PCapPacket;
import io.pkts.packet.PacketParseException;
import io.pkts.protocol.Protocol;

public class EthernetPacketImpl extends MACPacketImpl implements EthernetPacket {

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

		return new EthernetPacketImpl(Protocol.ETHERNET_II, parent, headers, null);
	}

	public EthernetPacketImpl(Protocol protocol, PCapPacket parent, Buffer headers, Buffer payload) {
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
		return new EthernetPacketImpl(getProtocol(), pkt, this.headers.clone(), getPayload().clone());
	}

}
