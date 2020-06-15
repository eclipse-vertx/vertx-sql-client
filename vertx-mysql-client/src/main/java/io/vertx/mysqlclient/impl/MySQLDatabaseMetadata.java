package io.vertx.mysqlclient.impl;

import java.util.Objects;

import io.vertx.sqlclient.spi.DatabaseMetadata;

public class MySQLDatabaseMetadata implements DatabaseMetadata {
  
  private final String fullVersion;
  private final boolean isMariaDB;
  private final int majorVersion;
  private final int minorVersion;
  private final int microVersion;
  
  public MySQLDatabaseMetadata(String serverVersion) {
    fullVersion = serverVersion;
    isMariaDB = serverVersion.toUpperCase().contains("MARIADB");  
    String[] versionNumbers;
    if (!isMariaDB) {
      // we assume the server version follows ${major}.${minor}.${micro} in https://dev.mysql.com/doc/refman/8.0/en/which-version.html
      versionNumbers = serverVersion.split("\\.");
    } else {
      // server version follows ${junk}-${major}.${minor}.${micro}-MariaDB-${junk}
      String[] parts = serverVersion.split("-");
      versionNumbers = parts[1].split("\\.");
    }
    majorVersion = Integer.parseInt(versionNumbers[0]);
    minorVersion = Integer.parseInt(versionNumbers[1]);
    // we should truncate the possible suffixes here
    String releaseVersion = versionNumbers[2];
    int indexOfFirstSeparator = releaseVersion.indexOf("-");
    if (indexOfFirstSeparator != -1) {
      // handle unstable release suffixes
      String releaseNumberString = releaseVersion.substring(0, indexOfFirstSeparator);
      microVersion = Integer.parseInt(releaseNumberString);
    } else {
      microVersion = Integer.parseInt(versionNumbers[2]);
    }
  }
  
  public boolean isMariaDB() {
    return isMariaDB;
  }

  @Override
  public String productName() {
    return isMariaDB ? "MariaDB" : "MySQL";
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
  
  public int getDatabasMicroVersion() {
    return microVersion;
  }

}
