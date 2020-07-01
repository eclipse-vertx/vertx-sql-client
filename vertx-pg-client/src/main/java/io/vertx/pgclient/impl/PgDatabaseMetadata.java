package io.vertx.pgclient.impl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.vertx.sqlclient.spi.DatabaseMetadata;

public class PgDatabaseMetadata implements DatabaseMetadata {

  private static final Pattern VERSION_PATTERN = Pattern.compile(
    "^(\\d+)(?:\\.(\\d+))?");

  private final String fullVersion;
  private int majorVersion;
  private int minorVersion;

  public PgDatabaseMetadata(String serverVersion) {
    fullVersion = serverVersion;
    Matcher matcher = VERSION_PATTERN.matcher(serverVersion);
    if (matcher.find()) {
      majorVersion = Integer.parseInt(matcher.group(1));
      String minorPart = matcher.group(2);
      if (minorPart != null) {
        minorVersion = Integer.parseInt(minorPart);
      }
    }
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
}
