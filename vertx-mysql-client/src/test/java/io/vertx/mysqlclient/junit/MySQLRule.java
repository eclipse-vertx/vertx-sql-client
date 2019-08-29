/*
 * Copyright (C) 2017 Julien Viet
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package io.vertx.mysqlclient.junit;

import io.vertx.mysqlclient.MySQLConnectOptions;
import org.junit.rules.ExternalResource;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;

public class MySQLRule extends ExternalResource {
  private static final String connectionUri = System.getProperty("connection.uri");

  private GenericContainer server;
  private MySQLConnectOptions options;
  private DatabaseType databaseType;
  private String databaseVersion;

  public static final MySQLRule SHARED_INSTANCE = new MySQLRule();

  public synchronized MySQLConnectOptions startServer(DatabaseType databaseType, String databaseVersion) throws Exception {
    initServer(databaseType, databaseVersion);
    server.start();

    return new MySQLConnectOptions()
      .setPort(server.getMappedPort(3306))
      .setHost(server.getContainerIpAddress())
      .setDatabase("testschema")
      .setUser("mysql")
      .setPassword("password");
  }

  public synchronized void stopServer() throws Exception {
    if (server != null) {
      try {
        server.stop();
      } finally {
        server = null;
      }
    }
  }

  private void initServer(DatabaseType serverType, String version) {
    server = new GenericContainer(serverType.toDockerImageName() + ":" + version)
      .withEnv("MYSQL_USER", "mysql")
      .withEnv("MYSQL_PASSWORD", "password")
      .withEnv("MYSQL_ROOT_PASSWORD", "password")
      .withEnv("MYSQL_DATABASE", "testschema")
      .withCommand("--max_allowed_packet=33554432 --max_prepared_stmt_count=16382 --local_infile=true --character-set-server=utf8mb4 --collation-server=utf8mb4_general_ci")
      .withExposedPorts(3306)
      .withClasspathResourceMapping("init.sql", "/docker-entrypoint-initdb.d/init.sql", BindMode.READ_ONLY);
  }

  private static DatabaseType parseDatabaseTypeString(String databaseInfo) throws IllegalArgumentException {
    switch (databaseInfo.toLowerCase()) {
      case "mysql":
        return DatabaseType.MySQL;
      case "mariadb":
        return DatabaseType.MariaDB;
      default:
        throw new IllegalArgumentException("Unknown database: " + databaseInfo);
    }
  }

  public static boolean isTestingWithExternalDatabase() {
    return isSystemPropertyValid(connectionUri);
  }

  private static boolean isSystemPropertyValid(String systemProperty) {
    return systemProperty != null && !systemProperty.isEmpty();
  }

  public boolean isUsingMariaDB() {
    return databaseType == DatabaseType.MariaDB;
  }

  public boolean isUsingMySQL5_6() {
    return databaseType == DatabaseType.MySQL && databaseVersion.contains("5.6");
  }

  public MySQLConnectOptions options() {
    return new MySQLConnectOptions(options);
  }

  @Override
  protected void before() throws Throwable {
    // use an external database for testing
    if (isTestingWithExternalDatabase()) {
      options = MySQLConnectOptions.fromUri(connectionUri);
      return;
    }

    // We do not need to launch another server if it's a shared instance
    if (this.server != null) {
      return;
    }

    // server type
    String databaseTypeString = System.getProperty("testing.mysql.database.server");
    if (isSystemPropertyValid(databaseTypeString)) {
      databaseType = parseDatabaseTypeString(databaseTypeString);
    } else {
      // MySQL by default
      databaseType = DatabaseType.MySQL;
    }

    // server version
    String databaseVersionString = System.getProperty("testing.mysql.database.version");
    if (isSystemPropertyValid(databaseVersionString)) {
      databaseVersion = databaseVersionString;
    } else {
      if (databaseType == DatabaseType.MySQL) {
        // 5.7 by default for MySQL
        databaseVersion = "5.7";
      } else if (databaseType == DatabaseType.MariaDB) {
        // 10.4 by default for MariaDB
        databaseVersion = "10.4";
      } else {
        throw new IllegalStateException("Unimplemented default version for: " + databaseType);
      }
    }

    options = startServer(databaseType, databaseVersion);
  }

  @Override
  protected void after() {
    if (!isTestingWithExternalDatabase()) {
      try {
        if (this != SHARED_INSTANCE) {
          // we don't shutdown the shared instance to boost testing
          stopServer();
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  private enum DatabaseType {
    MySQL, MariaDB;

    public String toDockerImageName() {
      return this.name().toLowerCase();
    }
  }
}
