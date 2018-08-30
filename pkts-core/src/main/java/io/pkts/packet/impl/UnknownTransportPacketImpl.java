package io.pkts.packet.impl;

import java.io.IOException;
import java.io.OutputStream;

import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;
import io.pkts.packet.IPPacket;
import io.pkts.packet.TransportPacket;
import io.pkts.protocol.Protocol;

public class UnknownTransportPacketImpl extends TransportPacketImpl {
	
	public UnknownTransportPacketImpl(IPPacket parent, Protocol protocol, Buffer payload) {
		super(parent, protocol, Buffers.EMPTY_BUFFER, payload);
	}

	@Override
	public int getHeaderLength() {
		return -1;
	}

	@Override
	public void write(OutputStream out, Buffer payload) throws IOException {
		getParentPacket().write(out, payload);
	}

	@Override
	public TransportPacket clone() {
		final IPPacket parent = getParentPacket().clone();
		return new UnknownTransportPacketImpl(parent, getProtocol(), getPayload().clone());
	}

}
