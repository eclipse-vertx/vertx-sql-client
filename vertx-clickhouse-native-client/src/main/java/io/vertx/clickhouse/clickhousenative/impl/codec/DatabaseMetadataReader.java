package io.vertx.clickhouse.clickhousenative.impl.codec;

import io.netty.buffer.ByteBuf;
import io.vertx.clickhouse.clickhousenative.ClickhouseConstants;
import io.vertx.clickhouse.clickhousenative.impl.ClickhouseNativeDatabaseMetadata;

import java.nio.charset.Charset;
import java.time.Duration;
import java.time.ZoneId;
import java.util.Map;

import io.vertx.clickhouse.clickhousenative.ClickhouseConstants;

public class DatabaseMetadataReader {
  private final String fullClientName;
  private final Map<String, String> properties;

  private String productName;
  private Integer major;
  private Integer minor;
  private Integer revision;
  private String timezone;
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
    if (timezone == null && revision >= ClickhouseConstants.DBMS_MIN_REVISION_WITH_SERVER_TIMEZONE) {
      timezone = ByteBufUtils.readPascalString(in);
      if (timezone == null) {
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
    return new ClickhouseNativeDatabaseMetadata(productName,
      String.format("%d.%d.%d", major, minor, revision),
      major, minor, revision, patchVersion, displayName, timezone == null ? null : ZoneId.of(timezone), fullClientName, properties, charset(),
      Duration.ofDays(daysInYear), Duration.ofDays(daysInQuarter), Duration.ofDays(daysInMonth));
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
