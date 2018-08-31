package io.pkts.frame;

import static org.mockito.Mockito.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.pkts.ArpTestBase;
import io.pkts.framer.EthernetFramer.EtherType;
import io.pkts.packet.EthernetPacket;
import io.pkts.packet.arp.ArpPacket;
import io.pkts.packet.arp.ArpPacket.ArpOperation;
import io.pkts.packet.arp.impl.ArpFramer;
import junit.framework.Assert;

public class ArpFrameTest extends ArpTestBase {

	/**
   * {@inheritDoc}
   */
  @Override
  @Before
  public void setUp() throws Exception {
      super.setUp();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @After
  public void tearDown() throws Exception {
      super.tearDown();
  }

  @Test
  public void testParsePacket() throws Exception {
      final ArpFramer framer = new ArpFramer();
      final ArpPacket p = framer.frame(mock(EthernetPacket.class), this.arpFrameBuffer);
      System.out.println(p.getProtocolType());
      Assert.assertEquals(EtherType.IPv4, p.getProtocolType());
      System.out.println(p.getOperation());
      Assert.assertEquals(ArpOperation.REQUEST, p.getOperation());
      System.out.println(p.getSenderMacAddress());
      Assert.assertEquals("00:1A:6B:6C:0C:CC", p.getSenderMacAddress());
      System.out.println(p.getTargetMacAddress());
      Assert.assertEquals("00:00:00:00:00:00", p.getTargetMacAddress());
      System.out.println(p.getSenderIpAddress());
      Assert.assertEquals("10.10.10.2", p.getSenderIpAddress());
      System.out.println(p.getTargetIpAddress());
      Assert.assertEquals("10.10.10.1", p.getTargetIpAddress());
  }
	
}
