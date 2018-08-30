package io.pkts.packet;

import io.pkts.framer.EthernetFramer.EtherType;

/**
 * Represents a IEEE 802.1Q virtual LAN Ethernet packet
 * 
 * @author jan-zajic at github.com
 * 
 */
public interface VlanPacket extends Packet, Cloneable {

	int getVlanId();
	EtherType getEthernetProtocol();
	
}
