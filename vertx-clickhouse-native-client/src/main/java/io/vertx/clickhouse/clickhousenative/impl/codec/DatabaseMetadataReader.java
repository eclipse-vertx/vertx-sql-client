package io.vertx.clickhouse.clickhousenative.impl.codec;

import io.netty.buffer.ByteBuf;
import io.vertx.clickhouse.clickhousenative.ClickhouseConstants;
import io.vertx.clickhouse.clickhousenative.impl.ClickhouseNativeDatabaseMetadata;

import java.nio.charset.Charset;
import java.time.Duration;
import java.time.ZoneId;
import java.util.Map;

public class DatabaseMetadataReader {
  private final String fullClientName;
  private final Map<String, String> properties;

  private String productName;
  private Integer major;
  private Integer minor;
  private Integer revision;
  private String serverZoneIdName;
  private String displayName;
  private Integer patchVersion;

  public DatabaseMetadataReader(String fullClientName, Map<String, String> properties) {
    assert(fullClientName != null);
    assert(properties != null);
    this.fullClientName = fullClientName;
    this.properties = properties;
  }

  public ClickhouseNativeDatabaseMetadata readFrom(ByteBuf in) {
    if (productName == null) {
      productName = ByteBufUtils.readPascalString(in);
      if (productName == null) {
        return null;
      }
    }
    if (major == null) {
      major = ByteBufUtils.readULeb128(in);
      if (major == null) {
        return null;
      }
    }
    if (minor == null) {
      minor = ByteBufUtils.readULeb128(in);
      if (minor == null) {
        return null;
      }
    }
    if (revision == null) {
      revision = ByteBufUtils.readULeb128(in);
      if (revision == null) {
        return null;
      }
    }
    if (serverZoneIdName == null && revision >= ClickhouseConstants.DBMS_MIN_REVISION_WITH_SERVER_TIMEZONE) {
      serverZoneIdName = ByteBufUtils.readPascalString(in);
      if (serverZoneIdName == null) {
        return null;
      }
    }
    if (displayName == null && revision >= ClickhouseConstants.DBMS_MIN_REVISION_WITH_SERVER_DISPLAY_NAME ) {
      displayName = ByteBufUtils.readPascalString(in);
      if (displayName == null) {
        return null;
      }
    }

    if (revision >= ClickhouseConstants.DBMS_MIN_REVISION_WITH_VERSION_PATCH) {
      if (patchVersion == null) {
        patchVersion = ByteBufUtils.readULeb128(in);
        if (patchVersion == null) {
          return null;
        }
      }
    } else {
      patchVersion = revision;
    }
    int daysInYear = Integer.parseInt(properties.getOrDefault(ClickhouseConstants.OPTION_YEAR_DURATION, "365"));
    int daysInQuarter = Integer.parseInt(properties.getOrDefault(ClickhouseConstants.OPTION_QUARTER_DURATION, "120"));
    int daysInMonth = Integer.parseInt(properties.getOrDefault(ClickhouseConstants.OPTION_MONTH_DURATION, "30"));
    ZoneId serverZoneId = serverZoneIdName == null ? null : ZoneId.of(serverZoneIdName);
    ZoneId defaultZoneId = getDefaultZoneId(serverZoneId);
    String extraNanos = properties.getOrDefault(ClickhouseConstants.OPTION_DATETIME64_EXTRA_NANOS_MODE, "throw");
    boolean saturateExtraNanos = "saturate".equals(extraNanos);
    boolean removeTrailingZerosInFixedStringsStr = Boolean.parseBoolean(properties.getOrDefault(ClickhouseConstants.OPTION_REMOVE_TRAILING_ZEROS_WHEN_ENCODE_FIXED_STRINGS, "true"));
    return new ClickhouseNativeDatabaseMetadata(productName,
      String.format("%d.%d.%d", major, minor, revision),
      major, minor, revision, patchVersion, displayName, serverZoneId, defaultZoneId, fullClientName, properties, charset(),
      Duration.ofDays(daysInYear), Duration.ofDays(daysInQuarter), Duration.ofDays(daysInMonth), saturateExtraNanos, removeTrailingZerosInFixedStringsStr);
  }

  private ZoneId getDefaultZoneId(ZoneId serverZoneId) {
    String defaultZoneId = properties.get(ClickhouseConstants.OPTION_DEFAULT_ZONE_ID);
    if (defaultZoneId == null || "from_server".equals(defaultZoneId)) {
      return serverZoneId;
    } else if ("system_default".equals(defaultZoneId)) {
      return ZoneId.systemDefault();
    } else {
      return ZoneId.of(defaultZoneId);
    }
  }

  private Charset charset() {
    String desiredCharset = properties.get(ClickhouseConstants.OPTION_STRING_CHARSET);
    if (desiredCharset == null || "system_default".equals(desiredCharset)) {
      return Charset.defaultCharset();
    } else {
      return Charset.forName(desiredCharset);
    }
  }
}
