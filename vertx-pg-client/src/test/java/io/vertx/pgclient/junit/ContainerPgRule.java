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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import io.vertx.sqlclient.utils.OperatingSystemUtils;
import org.junit.rules.ExternalResource;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.MountableFile;

import io.vertx.pgclient.PgConnectOptions;

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

  private PostgreSQLContainer server;
  private PgConnectOptions options;
  private String databaseVersion;
  private boolean ssl;
  private boolean domainSocket;

  public ContainerPgRule ssl(boolean ssl) {
    this.ssl = ssl;
    return this;
  }

  public ContainerPgRule domainSocket(boolean domainSocket) {
    this.domainSocket = domainSocket;
    return this;
  }

  public PgConnectOptions options() {
    return new PgConnectOptions(options);
  }

  private void initServer(String version) throws Exception {
    if (domainSocket && ssl) {
      throw new IllegalStateException("Can not use Unix domain socket with TLS at the same time.");
    }
    File setupFile = getTestResource("resources" + File.separator + "create-postgres.sql");

    server = (PostgreSQLContainer) new PostgreSQLContainer("postgres:" + version)
        .withDatabaseName("postgres")
        .withUsername("postgres")
        .withPassword("postgres")
        .withCopyFileToContainer(MountableFile.forHostPath(setupFile.toPath()), "/docker-entrypoint-initdb.d/create-postgres.sql");
    if(ssl) {
      server.withCopyFileToContainer(MountableFile.forHostPath(getTestResource("resources" +File.separator + "server.crt").toPath()), "/server.crt")
            .withCopyFileToContainer(MountableFile.forHostPath(getTestResource("resources" +File.separator + "server.key").toPath()), "/server.key")
            .withCopyFileToContainer(MountableFile.forHostPath(getTestResource("ssl.sh").toPath()), "/docker-entrypoint-initdb.d/ssl.sh");
    }
    if (domainSocket) {
      server.withFileSystemBind("/var/run/postgresql", "/var/run/postgresql", BindMode.READ_WRITE);
    }
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

  private static boolean isSystemPropertyValid(String systemProperty) {
    return systemProperty != null && !systemProperty.isEmpty();
  }

  public synchronized PgConnectOptions startServer(String databaseVersion) throws Exception {
    initServer(databaseVersion);
    server.start();

    PgConnectOptions pgConnectOptions = new PgConnectOptions()
      .setPort(server.getMappedPort(PostgreSQLContainer.POSTGRESQL_PORT))
      .setHost(server.getContainerIpAddress())
      .setDatabase("postgres")
      .setUser("postgres")
      .setPassword("postgres");

    if (domainSocket) {
      pgConnectOptions.setHost("/var/run/postgresql")
        .setPort(5432);
    }
    return pgConnectOptions;
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

  public synchronized void stopServer() throws Exception {
    if (server != null) {
      try {
        server.stop();
      } finally {
        server = null;
      }
    }
  }

  @Override
  protected void before() throws Throwable {
    // use an external database for testing
    if (!ssl && isSystemPropertyValid(connectionUri)) {
      options = PgConnectOptions.fromUri(connectionUri);
      return;
    } else if (ssl && isSystemPropertyValid(tlsConnectionUri)) {
      options = PgConnectOptions.fromUri(tlsConnectionUri);
      return;
    }

    // We do not need to launch another server if it's a shared instance
    if (this.server != null) {
      return;
    }

    this.databaseVersion = getPostgresVersion();
    options = startServer(databaseVersion);
  }

  public static boolean isSaslAuthenticationSupported() {
    String version = getPostgresVersion();
    String[] versionStrings = version.split("\\.");
    int majorVersionNumber = Integer.parseInt(versionStrings[0]);
    return majorVersionNumber > 9;
  }

  @Override
  protected void after() {
    if (!isTestingWithExternalDatabase()) {
      try {
        stopServer();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  private boolean isTestingWithExternalDatabase() {
    return isTestingTlsWithExternalDatabase() || isCommonTestingWithExternalDatabase();
  }

  /**
   * check whether TLS tests are testing with external database
   */
  private boolean isTestingTlsWithExternalDatabase() {
    return ssl && isSystemPropertyValid(tlsConnectionUri);
  }

  /**
   * check whether non-TLS tests are testing with external database
   */
  private boolean isCommonTestingWithExternalDatabase() {
    return !ssl && isSystemPropertyValid(connectionUri);
  }

}
