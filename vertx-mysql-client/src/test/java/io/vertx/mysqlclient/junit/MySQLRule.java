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

import com.github.dockerjava.api.model.Ulimit;
import io.vertx.mysqlclient.MySQLConnectOptions;
import org.junit.rules.ExternalResource;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class MySQLRule extends ExternalResource {
  private static final String connectionUri = System.getProperty("connection.uri");
  private static final String tlsConnectionUri = System.getProperty("tls.connection.uri");

  private Network network;
  private GenericContainer<?> server;
  private MySQLConnectOptions options;
  private DatabaseServerInfo databaseServerInfo;
  private File mysqldDir;

  private boolean ssl;

  public static final MySQLRule SHARED_INSTANCE = new MySQLRule();
  public static final MySQLRule SHARED_TLS_INSTANCE = new MySQLRule().ssl(true);

  public MySQLRule ssl(boolean ssl) {
    this.ssl = ssl;
    return this;
  }

  public synchronized MySQLConnectOptions startServer() throws Exception {
    initServer();
    server.start();

    return new MySQLConnectOptions()
      .setPort(server.getMappedPort(3306))
      .setHost(server.getHost())
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

  private void initServer() throws IOException {
    network = Network.builder().driver("bridge").build();

    server = new GenericContainer<>(databaseServerInfo.getDatabaseType().toDockerImageName() + ":" + databaseServerInfo.getDockerImageTag())
      .withEnv("MYSQL_USER", "mysql")
      .withEnv("MYSQL_PASSWORD", "password")
      .withEnv("MYSQL_ROOT_PASSWORD", "password")
      .withEnv("MYSQL_DATABASE", "testschema")
      .withCreateContainerCmdModifier(createContainerCmd -> {
        createContainerCmd.getHostConfig().withUlimits(new Ulimit[]{new Ulimit("nofile", 262144L, 262144L)});
      })
      .withNetwork(network)
      .withNetworkAliases(networkAlias())
      .withExposedPorts(3306)
      .withClasspathResourceMapping("init.sql", "/docker-entrypoint-initdb.d/init.sql", BindMode.READ_ONLY)
      .withReuse(true);

    mysqldDir = Files.createTempDirectory("mysqld").toFile();
    mysqldDir.deleteOnExit();
    mysqldDir.setReadable(true, false);
    mysqldDir.setWritable(true, false);
    mysqldDir.setExecutable(true, false);
    server.withFileSystemBind(mysqldDir.getAbsolutePath(), "/var/run/mysqld");

    if (ssl) {
      server.withClasspathResourceMapping("tls/conf", "/etc/mysql/conf.d", BindMode.READ_ONLY);
      server.withClasspathResourceMapping("tls/files", "/etc/mysql/tls", BindMode.READ_ONLY);
    } else {
      server.withClasspathResourceMapping("tls/files", "/etc/mysql/tls", BindMode.READ_ONLY);
      String cmd = "--disable-ssl --max_allowed_packet=33554432 --max_prepared_stmt_count=1024 --local_infile=true --character-set-server=utf8mb4 --collation-server=utf8mb4_general_ci";
      if (isUsingMySQL8()) {
        // introduced in MySQL 8.0.3
        cmd += " --caching-sha2-password-public-key-path=/etc/mysql/tls/public_key.pem --caching-sha2-password-private-key-path=/etc/mysql/tls/private_key.pem";
      }
      server.withCommand(cmd);
    }
  }

  public String network() {
    return network != null ? network.getId() : "mysql_default";
  }

  public String networkAlias() {
    return network != null ? Integer.toHexString(System.identityHashCode(this)) : "test-mysql";
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

  public static boolean isTlsTestingWithExternalDatabase() {
    return isSystemPropertyValid(tlsConnectionUri);
  }

  private static boolean isSystemPropertyValid(String systemProperty) {
    return systemProperty != null && !systemProperty.isEmpty();
  }

  public boolean isUsingMariaDB() {
    return databaseServerInfo.getDatabaseType() == DatabaseType.MariaDB;
  }

  public boolean isUsingMySQL5_6() {
    return databaseServerInfo == DatabaseServerInfo.MySQL_V5_6;
  }

  public boolean isUsingMySQL8() {
    return databaseServerInfo == DatabaseServerInfo.MySQL_V8_0;
  }

  public MySQLConnectOptions options() {
    return new MySQLConnectOptions(options);
  }

  public String domainSocketPath() {
    return new File(mysqldDir, "mysqld.sock").getAbsolutePath();
  }

  @Override
  protected void before() throws Throwable {
    // use an external database for testing
    if (isTestingWithExternalDatabase() && !ssl) {
      options = MySQLConnectOptions.fromUri(connectionUri);
      databaseServerInfo = DatabaseServerInfo.EXTERNAL;
      return;
    }

    // use an external database for tls testing
    if (isTlsTestingWithExternalDatabase() && ssl) {
      options = MySQLConnectOptions.fromUri(tlsConnectionUri);
      databaseServerInfo = DatabaseServerInfo.EXTERNAL;
      return;
    }

    // We do not need to launch another server if it's a shared instance
    if (this.server != null) {
      return;
    }

    // server type
    DatabaseType databaseType;
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
      this.databaseServerInfo = DatabaseServerInfo.valueOf(databaseType, databaseVersionString);
    } else {
      // use default version for testing servers
      if (databaseType == DatabaseType.MySQL) {
        // 5.7 by default for MySQL
        this.databaseServerInfo = DatabaseServerInfo.MySQL_V5_7;
      } else if (databaseType == DatabaseType.MariaDB) {
        // 10.4 by default for MariaDB
        this.databaseServerInfo = DatabaseServerInfo.MariaDB_V10_4;
      } else {
        throw new IllegalStateException("Unimplemented default version for: " + databaseType);
      }
    }

    options = startServer();
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

  private enum DatabaseServerInfo {
    MySQL_V5_6(DatabaseType.MySQL, "5.6"),
    MySQL_V5_7(DatabaseType.MySQL, "5.7"),
    MySQL_V8_0(DatabaseType.MySQL, "8.0"),
    MySQL_LATEST(DatabaseType.MySQL, "latest"),
    MariaDB_V10_4(DatabaseType.MariaDB, "10.4"),
    MariaDB_LATEST(DatabaseType.MariaDB, "latest"),
    EXTERNAL(null, null);

    private final DatabaseType databaseType;
    private final String dockerImageTag;

    DatabaseServerInfo(DatabaseType databaseType, String dockerImageTag) {
      this.databaseType = databaseType;
      this.dockerImageTag = dockerImageTag;
    }

    public String getDockerImageTag() {
      return dockerImageTag;
    }

    public DatabaseType getDatabaseType() {
      return databaseType;
    }

    public static DatabaseServerInfo valueOf(DatabaseType databaseType, String dockerImageTag) {
      switch (databaseType) {
        case MySQL:
          if (dockerImageTag.startsWith("5.6")) {
            return MySQL_V5_6;
          } else if (dockerImageTag.startsWith("5.7")) {
            return MySQL_V5_7;
          } else if (dockerImageTag.startsWith("8")) {
            return MySQL_V8_0;
          } else if (dockerImageTag.equalsIgnoreCase("latest")) {
            return MySQL_LATEST;
          } else {
            throw new IllegalArgumentException("Unsupported docker image tag for MySQL server, tag: " + dockerImageTag);
          }
        case MariaDB:
          if (dockerImageTag.startsWith("10.4")) {
            return MariaDB_V10_4;
          } else if (dockerImageTag.equalsIgnoreCase("latest")) {
            return MariaDB_LATEST;
          }
        default:
          throw new IllegalStateException("Unsupported database type: " + databaseType.toDockerImageName());
      }
    }
  }
}
