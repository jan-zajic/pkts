package io.pkts.frame;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.pkts.VlanTestBase;
import io.pkts.framer.EthernetFramer.EtherType;
import io.pkts.framer.VlanFramer;
import io.pkts.packet.EthernetPacket;
import io.pkts.packet.VlanPacket;

public class VlanFrameTest extends VlanTestBase {
	
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
      final VlanFramer framer = new VlanFramer();
      final VlanPacket p = framer.frame(mock(EthernetPacket.class), this.vlanFrameBuffer);
      assertThat(p.getVlanId(), is(111));
      assertThat(p.getEthernetProtocol(), is(EtherType.None));
  }
	
}
