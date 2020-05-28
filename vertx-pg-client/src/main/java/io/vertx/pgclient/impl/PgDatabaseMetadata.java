package io.vertx.pgclient.impl;

import java.util.StringTokenizer;

import io.vertx.sqlclient.spi.DatabaseMetadata;

public class PgDatabaseMetadata implements DatabaseMetadata {
  
  private final String fullVersion;
  private final int majorVersion;
  private final int minorVersion;
  
  public PgDatabaseMetadata(String serverVersion) {
    fullVersion = serverVersion;
    StringTokenizer versionTokens = new StringTokenizer(serverVersion, "."); // aaXbb.ccYdd
    majorVersion = integerPart(versionTokens.nextToken()); // aaXbb
    minorVersion = integerPart(versionTokens.nextToken()); // ccYdd
  }
  
  @Override
  public String productName() {
    return "PostgreSQL";
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
  
  // Parse a "dirty" integer surrounded by non-numeric characters
  private static int integerPart(String dirtyString) {
    int start = 0;

    while (start < dirtyString.length() && !Character.isDigit(dirtyString.charAt(start))) {
      ++start;
    }

    int end = start;
    while (end < dirtyString.length() && Character.isDigit(dirtyString.charAt(end))) {
      ++end;
    }

    if (start == end) {
      return 0;
    }

    return Integer.parseInt(dirtyString.substring(start, end));
  }

}
