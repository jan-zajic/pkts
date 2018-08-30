package io.pkts.packet.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;
import io.pkts.frame.UnknownEtherType;
import io.pkts.framer.EthernetFramer;
import io.pkts.framer.EthernetFramer.EtherType;
import io.pkts.framer.IPv4Framer;
import io.pkts.framer.IPv6Framer;
import io.pkts.packet.IPPacket;
import io.pkts.packet.PCapPacket;
import io.pkts.packet.Packet;
import io.pkts.packet.PacketParseException;
import io.pkts.packet.VlanPacket;
import io.pkts.protocol.Protocol;

public final class VlanPacketImpl extends AbstractPacket implements VlanPacket {

	private static final IPv4Framer ipv4Framer = new IPv4Framer();
	private static final IPv6Framer ipv6Framer = new IPv6Framer();

	/**
	 * If the headers are set then this overrides any of the source stuff set
	 * above.
	 */
	protected final Buffer headers;

	public VlanPacketImpl(Packet parent, final Buffer headers, Buffer payload) {
		super(Protocol.DOT1Q, parent, payload);
		this.headers = headers;
	}

	@Override
	public VlanPacket clone() {
		final Packet pkt = this.getParentPacket().clone();
		return new VlanPacketImpl(pkt, this.headers.clone(), getPayload().clone());
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
			default:
				return Protocol.UNKNOWN;
			}
		} else {
			// TODO: figure out how an SLL packet indicates IPv4 vs IPv6
			return Protocol.IPv4;
		}
	}

	@Override
	public IPPacket getNextPacket() throws IOException {
		final Buffer payload = getPayload();
		if (payload == null) {
			return null;
		}
		switch (getNextProtocol()) {
		case IPv4:
			return ipv4Framer.frame(this, payload);
		case IPv6:
			return ipv6Framer.frame(this, payload);
		default:
			return null;
		}
	}

	@Override
	public long getArrivalTime() {
		return this.getParentPacket().getArrivalTime();
	}

	@Override
	public void write(OutputStream out, Buffer payload) throws IOException {
		this.getParentPacket().write(out, Buffers.wrap(this.headers, payload));
	}

	@Override
	public int getVlanId() {		
		try {
			int b1 = this.headers.getByte(0) & 0xF;
			int b2 = this.headers.getByte(1) & 0xFF;
			return ((b1 << 8) & 0xFF00) | b2;
		} catch (final IOException e) {
      throw new RuntimeException("Unable to read data from the underlying Buffer.", e);
		}
	}

	@Override
	public EtherType getEthernetProtocol() {
		try {
			final byte b1 = headers.getByte(2);
			final byte b2 = headers.getByte(3);
			try {
				return EthernetFramer.getEtherType(b1, b2);
			} catch (UnknownEtherType e) {
				throw new PacketParseException(12, String.format("Unknown Ethernet type 0x%x%x", b1, b2));
			}
		} catch (IOException e) {
			throw new RuntimeException("Unable to read data from the underlying Buffer.", e);
		}
	}

}
