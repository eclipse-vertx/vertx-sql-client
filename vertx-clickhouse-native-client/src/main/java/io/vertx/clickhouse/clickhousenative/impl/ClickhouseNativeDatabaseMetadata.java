package io.vertx.clickhouse.clickhousenative.impl;

import io.vertx.sqlclient.spi.DatabaseMetadata;

import java.time.ZoneId;

public class ClickhouseNativeDatabaseMetadata implements DatabaseMetadata {
  private final String productName;
  private final String fullVersion;
  private final int major;
  private final int minor;
  private final int revision;
  private final int patchVersion;
  private final String displayName;
  private final ZoneId timezone;
  private final String clientName;

  public ClickhouseNativeDatabaseMetadata(String productName, String fullVersion, int major, int minor, int revision,
                                          int patchVersion, String displayName, ZoneId timezone, String clientName) {
    this.productName = productName;
    this.fullVersion = fullVersion;
    this.major = major;
    this.minor = minor;
    this.revision = revision;
    this.patchVersion = patchVersion;
    this.displayName = displayName;
    this.timezone = timezone;
    this.clientName = clientName;
  }

  @Override
  public String productName() {
    return null;
  }

  @Override
  public String fullVersion() {
    return null;
  }

  @Override
  public int majorVersion() {
    return 0;
  }

  @Override
  public int minorVersion() {
    return 0;
  }

  public int getRevision() {
    return revision;
  }

  public int getPatchVersion() {
    return patchVersion;
  }

  public String getDisplayName() {
    return displayName;
  }

  public ZoneId getTimezone() {
    return timezone;
  }

  public String getClientName() {
    return clientName;
  }

  @Override
  public String toString() {
    return "ClickhouseNativeDatabaseMetadata{" +
      "productName='" + productName + '\'' +
      ", fullVersion='" + fullVersion + '\'' +
      ", major=" + major +
      ", minor=" + minor +
      ", revision=" + revision +
      ", patchVersion=" + patchVersion +
      ", displayName='" + displayName + '\'' +
      ", timezone='" + timezone + '\'' +
      '}';
  }
}
