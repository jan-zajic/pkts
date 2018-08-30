package io.pkts.packet;

import io.pkts.framer.EthernetFramer.EtherType;

public interface EthernetPacket extends MACPacket {

	public EtherType getEthernetProtocol();
	
	@Override
	EthernetPacket clone();
	
}
