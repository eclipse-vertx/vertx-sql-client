package io.vertx.mssqlclient.impl;

import io.vertx.sqlclient.spi.DatabaseMetadata;

public class MSSQLDatabaseMetadata implements DatabaseMetadata {

  @Override
  public String productName() {
    return "Microsoft SQL Server";
  }
  
  @Override
  public String fullVersion() {
    throw new UnsupportedOperationException();
  }

  @Override
  public int majorVersion() {
    throw new UnsupportedOperationException();
  }

  @Override
  public int minorVersion() {
    throw new UnsupportedOperationException();
  }

}
