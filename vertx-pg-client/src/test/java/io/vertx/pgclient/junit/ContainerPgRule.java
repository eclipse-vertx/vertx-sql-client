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
package io.vertx.pgclient.junit;

import io.vertx.pgclient.PgConnectOptions;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.SqlHost;
import org.junit.rules.ExternalResource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.MountableFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * Postgresql test database based on https://www.testcontainers.org
 * Require Docker
 * Testing can also be done on an external database by setting the system properties :
 *  - connection.uri
 *  - tls.connection.uri
 */
public class ContainerPgRule extends ExternalResource {

  private static final String connectionUri = System.getProperty("connection.uri");
  private static final String tlsConnectionUri = System.getProperty("tls.connection.uri");

  private List<PostgreSQLContainer<?>> servers;
  private PgConnectOptions options;
  private String databaseVersion;
  private boolean ssl;
  private int serverCount = 1;

  public ContainerPgRule ssl(boolean ssl) {
    this.ssl = ssl;
    return this;
  }

  public ContainerPgRule setServerCount(int i) {
    this.serverCount = i;
    return this;
  }

  public PgConnectOptions options() {
    return new PgConnectOptions(options);
  }

  public PoolOptions poolOptions() {
    return new PoolOptions();
  }

  private PostgreSQLContainer<?> initServer(String version) throws Exception {
    File setupFile = getTestResource("resources" + File.separator + "create-postgres.sql");

    final PostgreSQLContainer<?> server = new PostgreSQLContainer<>("postgres:" + version)
      .withDatabaseName("postgres")
      .withUsername("postgres")
      .withPassword("postgres")
      .withCopyFileToContainer(MountableFile.forHostPath(setupFile.toPath()), "/docker-entrypoint-initdb.d/create-postgres.sql");
    if (ssl) {
      server.withCopyFileToContainer(MountableFile.forHostPath(getTestResource("resources" + File.separator + "server.crt").toPath()), "/server.crt")
        .withCopyFileToContainer(MountableFile.forHostPath(getTestResource("resources" + File.separator + "server.key").toPath()), "/server.key")
        .withCopyFileToContainer(MountableFile.forHostPath(getTestResource("ssl.sh").toPath()), "/docker-entrypoint-initdb.d/ssl.sh");
    }
    return server;
  }

  private static File getTestResource(String name) throws Exception {
    File file = null;
    try(InputStream in = new FileInputStream(new File("docker" + File.separator + "postgres" + File.separator + name))) {
      Path path = Files.createTempFile("pg-client", ".tmp");
      Files.copy(in, path, StandardCopyOption.REPLACE_EXISTING);
      file = path.toFile();
      file.deleteOnExit();
    }
    return file;
  }

  public static boolean isTestingWithExternalDatabase() {
    return isSystemPropertyValid(connectionUri) || isSystemPropertyValid(tlsConnectionUri);
  }

  private static boolean isSystemPropertyValid(String systemProperty) {
    return systemProperty != null && !systemProperty.isEmpty();
  }

  public PgConnectOptions startServer(String databaseVersion) throws Exception {
    return startServers(databaseVersion, 1);
  }

  public synchronized PgConnectOptions startServers(String databaseVersion, int serverCount) throws Exception {
    this.servers = new ArrayList<>();
    for (int i = 0; i < serverCount; i++) {
      final PostgreSQLContainer<?> server = initServer(databaseVersion);
      server.start();
      servers.add(server);
    }

    final List<SqlHost> hosts = servers
      .stream()
      .map(s -> new SqlHost(s.getContainerIpAddress(), s.getMappedPort(PostgreSQLContainer.POSTGRESQL_PORT)))
      .collect(toList());
    return new PgConnectOptions()
        .setHosts(hosts)
        .setDatabase("postgres")
        .setUser("postgres")
        .setPassword("postgres");
  }

  private static String getPostgresVersion() {
    String specifiedVersion = System.getProperty("embedded.postgres.version");
    String version;
    if (specifiedVersion == null || specifiedVersion.isEmpty()) {
      // if version is not specified then V10.10 will be used by default
      version = "10.10";
    } else {
      version = specifiedVersion;
    }

    return version;
  }

  public synchronized void stopServers() throws Exception {
    if (servers != null) {
      try {
        for (PostgreSQLContainer<?> server : servers) {
          server.stop();
        }
      } finally {
        servers = null;
      }
    }
  }

  @Override
  protected void before() throws Throwable {
    // use an external database for testing
    if (isTestingWithExternalDatabase()) {

      if (ssl) {
        options =  PgConnectOptions.fromUri(tlsConnectionUri);
      }
      else {
        options = PgConnectOptions.fromUri(connectionUri);
      }

      return;
    }

    // We do not need to launch another set of servers if it's a shared instance
    if (this.servers != null) {
      return;
    }

    this.databaseVersion = getPostgresVersion();
    options = startServers(databaseVersion, serverCount);
  }

  public static boolean isAtLeastPg10() {
    // hackish ;-)
    return !getPostgresVersion().startsWith("9.");
  }

  @Override
  protected void after() {
    if (!isTestingWithExternalDatabase()) {
      try {
        stopServers();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

}
