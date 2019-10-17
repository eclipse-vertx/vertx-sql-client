package io.vertx.mssqlclient.impl.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;

public final class Utils {
  public static String getHostName() {
    String hostName;
    try {
      hostName = InetAddress.getLocalHost().getHostName();
    } catch (UnknownHostException e) {
      hostName = "";
    }
    return hostName;
  }
}
