package io.vertx.mysqlclient.impl;

import io.vertx.sqlclient.spi.DatabaseMetadata;

public class MySQLDatabaseMetadata implements DatabaseMetadata {
  
  private final int majorVersion;
  private final int minorVersion;
  private final int microVersion;
  
  public MySQLDatabaseMetadata(String serverVersion) {
    // we assume the server version follows ${major}.${minor}.${micro} in https://dev.mysql.com/doc/refman/8.0/en/which-version.html
    String[] versionNumbers = serverVersion.split("\\.");
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

  @Override
  public String getProductName() {
    return "MySQL"; // TODO: Should this return MariaDB sometimes?
  }

  @Override
  public int getMajorVersion() {
    return majorVersion;
  }

  @Override
  public int getMinorVersion() {
    return minorVersion;
  }
  
  public int getDatabasMicroVersion() {
    return microVersion;
  }

}
