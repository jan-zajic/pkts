/**
 * 
 */
package io.pkts.packet.arp.impl;

import java.io.IOException;

import io.pkts.buffer.Buffer;
import io.pkts.framer.Framer;
import io.pkts.packet.Packet;
import io.pkts.packet.arp.ArpPacket;
import io.pkts.protocol.Protocol;

public class ArpFramer implements Framer<Packet, ArpPacket> {

    /**
     * 
     */
    public ArpFramer() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Protocol getProtocol() {
        return Protocol.ARP;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ArpPacket frame(final Packet parent, final Buffer buffer) throws IOException {
        if (parent == null) {
            throw new IllegalArgumentException("The parent frame cannot be null");
        }

        final Buffer headers = buffer.readBytes(28);
        final Buffer payload = buffer.slice(buffer.capacity());
        return new ArpPacketImpl(parent, headers, payload);
    }

    @Override
    public boolean accept(final Buffer buffer) throws IOException {
        return true;
    }

}
