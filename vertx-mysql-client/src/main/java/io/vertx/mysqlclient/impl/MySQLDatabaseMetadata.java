package io.vertx.mysqlclient.impl;

import io.vertx.core.internal.logging.Logger;
import io.vertx.core.internal.logging.LoggerFactory;
import io.vertx.sqlclient.spi.DatabaseMetadata;

public class MySQLDatabaseMetadata implements DatabaseMetadata {

  private static final Logger LOGGER = LoggerFactory.getLogger(MySQLDatabaseMetadata.class);

  private enum System {
    MYSQL("mysql", "MySQL"),
    MARIA_DB("mariadb", "MariaDB"),
    ;

    final String value;
    final String productName;

    System(String value, String productName) {
      this.value = value;
      this.productName = productName;
    }
  }

  private final System system;
  private final String fullVersion;
  private final int majorVersion;
  private final int minorVersion;
  private final int microVersion;

  private MySQLDatabaseMetadata(System system,
                                String fullVersion,
                                int majorVersion,
                                int minorVersion,
                                int microVersion) {
    this.system = system;
    this.fullVersion = fullVersion;
    this.majorVersion = majorVersion;
    this.minorVersion = minorVersion;
    this.microVersion = microVersion;
  }

  public static MySQLDatabaseMetadata parse(String serverVersion) {
    int majorVersion = 0;
    int minorVersion = 0;
    int microVersion = 0;

    int len = serverVersion.length();
    System system = serverVersion.contains("MariaDB") ? System.MARIA_DB : System.MYSQL;

    String fullServerVersion = serverVersion;
    if (system == System.MARIA_DB) {
      // MariaDB server version < 11.x.x is by default prefixed by "5.5.5-"
      serverVersion = serverVersion.replace("5.5.5-", "");
    }
    String versionToken = null;
    int versionTokenStartIdx = 0;
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

    return new MySQLDatabaseMetadata(system, fullServerVersion, majorVersion, minorVersion, microVersion);
  }

  public String system() {
    return system.value;
  }

  @Override
  public String productName() {
    return system.productName;
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
