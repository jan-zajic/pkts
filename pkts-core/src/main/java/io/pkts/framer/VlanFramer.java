/**
 * 
 */
package io.pkts.framer;

import java.io.IOException;

import io.pkts.buffer.Buffer;
import io.pkts.packet.EthernetPacket;
import io.pkts.packet.VlanPacket;
import io.pkts.packet.impl.VlanPacketImpl;
import io.pkts.protocol.Protocol;

public class VlanFramer implements Framer<EthernetPacket, VlanPacket> {

    /**
     * 
     */
    public VlanFramer() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Protocol getProtocol() {
        return Protocol.DOT1Q;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public VlanPacket frame(final EthernetPacket parent, final Buffer buffer) throws IOException {
        if (parent == null) {
            throw new IllegalArgumentException("The parent frame cannot be null");
        }

        final Buffer headers = buffer.readBytes(4);
        final Buffer payload = buffer.slice(buffer.capacity());
        return new VlanPacketImpl(parent, headers, payload);
    }

    @Override
    public boolean accept(final Buffer buffer) throws IOException {
        buffer.markReaderIndex();
        try {
            final Buffer test = buffer.readBytes(4);
            final byte b3 = test.getByte(2);
            final byte b4 = test.getByte(3);
            return isKnownEtherType(b3, b4);
        } catch (final IndexOutOfBoundsException e) {
            return false;
        } finally {
            buffer.resetReaderIndex();
        }
    }

    private boolean isKnownEtherType(final byte b1, final byte b2) {
        return EthernetFramer.getEtherTypeSafe(b1, b2) != null;
    }

}
