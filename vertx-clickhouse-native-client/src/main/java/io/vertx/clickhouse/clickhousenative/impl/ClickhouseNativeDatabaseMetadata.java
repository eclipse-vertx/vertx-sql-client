package io.vertx.clickhouse.clikhousenative.impl;

import io.vertx.sqlclient.spi.DatabaseMetadata;

public class ClickhouseNativeDatabaseMetadata implements DatabaseMetadata {
  private final String productName;
  private final String fullVersion;
  private final String major;
  private final String minor;

  public ClickhouseNativeDatabaseMetadata(String productName, String fullVersion, String major, String minor) {
    this.productName = productName;
    this.fullVersion = fullVersion;
    this.major = major;
    this.minor = minor;
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
}
