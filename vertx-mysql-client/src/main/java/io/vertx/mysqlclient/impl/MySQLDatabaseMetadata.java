package io.vertx.mysqlclient.impl;

import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.sqlclient.spi.DatabaseMetadata;

public class MySQLDatabaseMetadata implements DatabaseMetadata {

  private static final Logger LOGGER = LoggerFactory.getLogger(MySQLDatabaseMetadata.class);

  private final String fullVersion;
  private final String productName;
  private final int majorVersion;
  private final int minorVersion;
  private final int microVersion;

  private MySQLDatabaseMetadata(String fullVersion,
                                String productName,
                                int majorVersion,
                                int minorVersion,
                                int microVersion) {
    this.fullVersion = fullVersion;
    this.productName = productName;
    this.majorVersion = majorVersion;
    this.minorVersion = minorVersion;
    this.microVersion = microVersion;
  }

  public static MySQLDatabaseMetadata parse(String serverVersion) {
    int majorVersion = 0;
    int minorVersion = 0;
    int microVersion = 0;

    int len = serverVersion.length();
    boolean isMariaDb = serverVersion.contains("MariaDB");
    String productName = isMariaDb ? "MariaDB" : "MySQL";

    String versionToken = null;
    int versionTokenStartIdx = isMariaDb ? 6 : 0; // MariaDB server version is by default prefixed by "5.5.5-"
    int versionTokenEndIdx = versionTokenStartIdx;
    while (versionTokenEndIdx < len) {
      char c = serverVersion.charAt(versionTokenEndIdx);
      if (c == '-' || c == ' ') {
        versionToken = serverVersion.substring(versionTokenStartIdx, versionTokenEndIdx);
        break;
      }
      versionTokenEndIdx++;
    }
    if (versionToken == null) {
      // if there's no '-' char
      versionToken = serverVersion;
    }

    // we assume the server version tokens follows the syntax: ${major}.${minor}.${micro}
    String[] versionTokens = versionToken.split("\\.");
    try {
      majorVersion = Integer.parseInt(versionTokens[0]);
      minorVersion = Integer.parseInt(versionTokens[1]);
      microVersion = Integer.parseInt(versionTokens[2]);
    } catch (Exception ex) {
      // make sure it does fail the connection phase
      LOGGER.warn("Incorrect parsing server version tokens", ex);
    }

    return new MySQLDatabaseMetadata(serverVersion, productName, majorVersion, minorVersion, microVersion);
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

  public int microVersion() {
    return microVersion;
  }
}
