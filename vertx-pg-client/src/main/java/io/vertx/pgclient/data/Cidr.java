package io.vertx.pgclient.data;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;

/**
 * A PostgreSQL <a href="https://www.postgresql.org/docs/current/datatype-net-types.html#DATATYPE-CIDR">classless internet domain routing</a>.
 */
public class Cidr {
  private InetAddress address;
  private Integer netmask;

  public InetAddress getAddress(){
    return address;
  }
  public Cidr setAddress(InetAddress address) {
    if (address instanceof Inet4Address || address instanceof Inet6Address) {
      this.address = address;
    } else {
      throw new IllegalArgumentException("Invalid IP address type");
    }
    return this;
  }

  public Integer getNetmask(){
    return netmask;
  }

  public Cidr setNetmask(Integer netmask) {
    if (netmask != null && ((getAddress() instanceof Inet4Address && (netmask < 0 || netmask > 32)) ||
      (getAddress() instanceof Inet6Address && (netmask < 0 || netmask > 128)))) {
      throw new IllegalArgumentException("Invalid netmask: " + netmask);
    }
    this.netmask = netmask;
    return this;
  }


}
