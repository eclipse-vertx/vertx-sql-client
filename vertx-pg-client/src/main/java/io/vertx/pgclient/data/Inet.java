package io.vertx.pgclient.data;

import java.net.InetAddress;

/**
 * A PosgreSQL <a href="https://www.postgresql.org/docs/9.1/datatype-net-types.html#:~:text=1.-,inet,(the%20%22netmask%22).">inet network address</a>.
 */
public class Inet {

  private InetAddress address;
  private Integer netmask;

  /**
   * @return the inet address
   */
  public InetAddress getAddress() {
    return address;
  }

  /**
   * Set the inet address
   * @param address
   * @return a reference to this, so the API can be used fluently
   */
  public Inet setAddress(InetAddress address) {
    this.address = address;
    return this;
  }

  /**
   * @return the optional netmask
   */
  public Integer getNetmask() {
    return netmask;
  }

  /**
   * Set a netmask.
   *
   * @param netmask the netmask
   * @return a reference to this, so the API can be used fluently
   */
  public Inet setNetmask(Integer netmask) {
    if (netmask != null && (netmask < 0 || netmask > 255)) {
      throw new IllegalArgumentException("Invalid netmask: " + netmask);
    }
    this.netmask = netmask;
    return this;
  }
}
