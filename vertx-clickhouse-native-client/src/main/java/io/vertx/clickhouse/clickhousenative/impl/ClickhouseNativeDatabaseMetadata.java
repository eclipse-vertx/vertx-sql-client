package io.vertx.clickhouse.clickhousenative.impl;

import io.vertx.sqlclient.spi.DatabaseMetadata;

import java.nio.charset.Charset;
import java.time.Duration;
import java.time.ZoneId;
import java.util.Map;

public class ClickhouseNativeDatabaseMetadata implements DatabaseMetadata {
  private final String productName;
  private final String fullVersion;
  private final int major;
  private final int minor;
  private final int revision;
  private final int patchVersion;
  private final String displayName;
  private final ZoneId timezone;
  private final String fullClientName;
  private final Charset stringCharset;
  private final Map<String, String> properties;
  private final Duration yearDuration;
  private final Duration quarterDuration;
  private final Duration monthDuration;

  public ClickhouseNativeDatabaseMetadata(String productName, String fullVersion, int major, int minor, int revision,
                                          int patchVersion, String displayName, ZoneId timezone, String fullClientName,
                                          Map<String, String> properties, Charset stringCharset, Duration yearDuration,
                                          Duration quarterDuration, Duration monthDuration) {
    this.productName = productName;
    this.fullVersion = fullVersion;
    this.major = major;
    this.minor = minor;
    this.revision = revision;
    this.patchVersion = patchVersion;
    this.displayName = displayName;
    this.timezone = timezone;
    this.fullClientName = fullClientName;
    this.properties = properties;
    this.stringCharset = stringCharset;
    this.yearDuration = yearDuration;
    this.quarterDuration = quarterDuration;
    this.monthDuration = monthDuration;
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
    return major;
  }

  @Override
  public int minorVersion() {
    return minor;
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

  public ZoneId getZoneId() {
    return timezone;
  }

  public String getFullClientName() {
    return fullClientName;
  }

  public Map<String, String> getProperties() {
    return properties;
  }

  public Charset getStringCharset() {
    return stringCharset;
  }

  public Duration yearDuration() {
    return yearDuration;
  }

  public Duration quarterDuration() {
    return quarterDuration;
  }

  public Duration monthDuration() {
    return monthDuration;
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
