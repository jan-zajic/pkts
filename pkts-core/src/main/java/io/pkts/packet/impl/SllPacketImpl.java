package io.pkts.packet.impl;

import io.pkts.buffer.Buffer;
import io.pkts.packet.PCapPacket;
import io.pkts.protocol.Protocol;

public class SllPacketImpl extends EthernetPacketImpl {
	
	public SllPacketImpl(PCapPacket parent, Buffer headers, Buffer payload) {
		super(Protocol.SLL, parent, headers, payload);
	}

}
