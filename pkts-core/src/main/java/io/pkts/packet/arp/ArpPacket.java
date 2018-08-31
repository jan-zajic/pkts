package io.pkts.packet.arp;

import io.pkts.framer.EthernetFramer.EtherType;
import io.pkts.packet.Packet;

public interface ArpPacket extends Packet {

	public static enum ArpOperation {
		REQUEST, RESPONSE
	}
	
	EtherType getProtocolType();
	ArpOperation getOperation();
	
	String getSenderMacAddress();	
	String getTargetMacAddress();
	
	short[] getSenderProtocolAddress();
	short[] getTargetProtocolAddress();
	
	String getSenderIpAddress();
	String getTargetIpAddress();
		
}
