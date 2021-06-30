package io.vertx.mssqlclient.impl;

import io.vertx.sqlclient.spi.DatabaseMetadata;

public class MSSQLDatabaseMetadata implements DatabaseMetadata {

  private final String fullVersion;
  private final int majorVersion;
  private final int minorVersion;

  public MSSQLDatabaseMetadata(String fullVersion, int majorVersion, int minorVersion) {
    this.fullVersion = fullVersion;
    this.majorVersion = majorVersion;
    this.minorVersion = minorVersion;
  }

  @Override
  public String productName() {
    return "Microsoft SQL Server";
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
