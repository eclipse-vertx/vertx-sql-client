package io.vertx.db2client.impl;

import java.util.Objects;

import io.vertx.sqlclient.spi.DatabaseMetadata;

public class DB2DatabaseMetadata implements DatabaseMetadata {

  private final boolean isZOS;
  private final String productName;
  private final String fullVersion;
  private final int majorVersion;
  private final int minorVersion;

  public DB2DatabaseMetadata(String serverReleaseLevel) {
    Objects.requireNonNull(serverReleaseLevel, "No server release level (SRVRLSLV) returned by server");
    fullVersion = serverReleaseLevel;
    if (serverReleaseLevel.startsWith("SQL")) {
      isZOS = false;
      productName = "DB2 for Linux/Unix/Windows";
    } else if (serverReleaseLevel.startsWith("DSN")) {
      isZOS = true;
      productName = "DB2 for z/OS";
    } else {
      throw new IllegalArgumentException("Received unknown server product release level: " + serverReleaseLevel);
    }
    if (serverReleaseLevel.length() < 7) {
      throw new IllegalArgumentException("Unable to determine server major/minor version from release level: " + serverReleaseLevel);
    }
    majorVersion = Integer.parseInt(serverReleaseLevel.substring(3, 5));
    minorVersion = Integer.parseInt(serverReleaseLevel.substring(5, 7));
  }

  public boolean isZOS() {
    return isZOS;
  }

  @Override
  public String productName() {
    return productName;
  }
  
  @Override
  public String fullVersion() {
    return fullVersion;
  }

  @Override
  public int majorVersion() {
    return majorVersion;
  }

  @Override
  public int minorVersion() {
    return minorVersion;
  }

}
