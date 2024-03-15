package io.vertx.pgclient.data;

import org.junit.Test;

import java.net.InetAddress;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class CidrTest extends DataTypeTestBase{

  @Test
  public void testValidIPv4() throws Exception {
    InetAddress address = InetAddress.getByName("192.168.1.1");
    Cidr cidr = new Cidr();
    cidr.setAddress(address);
    cidr.setNetmask(24);
    assertEquals(address, cidr.getAddress());
    assertEquals(Integer.valueOf(24), cidr.getNetmask());
  }

  @Test
  public void testValidIPv6() throws Exception {
    InetAddress address = InetAddress.getByName("fe80::f03c:91ff:feae:e944");
    Cidr cidr = new Cidr();
    cidr.setAddress(address);
    cidr.setNetmask(64);
    assertEquals(address, cidr.getAddress());
    assertEquals(Integer.valueOf(64), cidr.getNetmask());
  }

  @Test
  public void testInvalidNetmaskIPv4() throws Exception {
    InetAddress address = InetAddress.getByName("192.168.1.1");
    Cidr cidr = new Cidr();
    cidr.setAddress(address);
    assertThrows(IllegalArgumentException.class, () -> cidr.setNetmask(33));
  }

  @Test
  public void testInvalidNetmaskIPv6() throws Exception {
    InetAddress address = InetAddress.getByName("fe80::f03c:91ff:feae:e944");
    Cidr cidr = new Cidr();
    cidr.setAddress(address);
    assertThrows(IllegalArgumentException.class, () -> cidr.setNetmask(129));
  }

}
