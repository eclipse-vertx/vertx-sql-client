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
package io.vertx.tests.pgclient.junit;

import io.vertx.pgclient.PgConnectOptions;
import io.vertx.sqlclient.PoolOptions;
import org.junit.rules.ExternalResource;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.InternetProtocol;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import static io.vertx.pgclient.PgConnectOptions.DEFAULT_PORT;

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
  private static final String tlsForceConnectionUri = System.getProperty("tls.force.connection.uri");

  private ServerContainer<?> server;
  private PgConnectOptions options;
  private String databaseVersion;
  private boolean ssl;
  private boolean forceSsl;
  private String user = "postgres";

  public ContainerPgRule ssl(boolean ssl) {
    this.ssl = ssl;
    return this;
  }

  public ContainerPgRule forceSsl(boolean forceSsl) {
    this.forceSsl = forceSsl;
    return this;
  }

  public PgConnectOptions options() {
    return new PgConnectOptions(options);
  }

  public PoolOptions poolOptions() {
    return new PoolOptions();
  }

  public ContainerPgRule user(String user) {
    if (user == null) {
      throw new NullPointerException();
    }
    this.user = user;
    return this;
  }

  private void initServer(String version) throws Exception {
    server = new ServerContainer<>("postgres:" + version)
      .withEnv("POSTGRES_DB", "postgres")
      .withEnv("POSTGRES_USER", user)
      .withEnv("POSTGRES_PASSWORD", "postgres")
      .waitingFor(new LogMessageWaitStrategy()
        .withRegEx(".*database system is ready to accept connections.*\\s")
        .withTimes(2)
        .withStartupTimeout(Duration.of(60, ChronoUnit.SECONDS)))
      .withCommand("postgres", "-c", "fsync=off")
      .withClasspathResourceMapping("create-postgres.sql", "/docker-entrypoint-initdb.d/create-postgres.sql", BindMode.READ_ONLY);
    if (ssl) {
      server
        .withClasspathResourceMapping("tls/server.crt", "/server.crt", BindMode.READ_ONLY)
        .withClasspathResourceMapping("tls/server.key", "/server.key", BindMode.READ_ONLY)
        .withClasspathResourceMapping("tls/ssl.sh", "/docker-entrypoint-initdb.d/ssl.sh", BindMode.READ_ONLY);
        if (forceSsl) {
          server
            .withClasspathResourceMapping("tls/pg_hba.conf", "/tmp/pg_hba.conf", BindMode.READ_ONLY)
            .withClasspathResourceMapping("tls/force_ssl.sh", "/docker-entrypoint-initdb.d/force_ssl.sh", BindMode.READ_ONLY);
        }
    }
    if (System.getProperties().containsKey("containerFixedPort")) {
      server.withFixedExposedPort(DEFAULT_PORT, DEFAULT_PORT);
    } else {
      server.withExposedPorts(DEFAULT_PORT);
    }
  }

  public static boolean isTestingWithExternalDatabase() {
    return isSystemPropertyValid(connectionUri) || isSystemPropertyValid(tlsConnectionUri) || isSystemPropertyValid(tlsForceConnectionUri);
  }

  private static boolean isSystemPropertyValid(String systemProperty) {
    return systemProperty != null && !systemProperty.isEmpty();
  }

  public synchronized PgConnectOptions startServer(String databaseVersion) throws Exception {
    initServer(databaseVersion);
    server.start();

    return new PgConnectOptions()
      .setPort(server.getMappedPort(DEFAULT_PORT))
      .setHost(server.getHost())
        .setDatabase("postgres")
        .setUser(user)
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
    if (isTestingWithExternalDatabase()) {

      if (ssl) {
        if (forceSsl) {
          options = PgConnectOptions.fromUri(tlsForceConnectionUri);
        } else {
          options = PgConnectOptions.fromUri(tlsConnectionUri);
        }
      }
      else {
        options = PgConnectOptions.fromUri(connectionUri);
      }

      return;
    }

    // We do not need to launch another server if it's a shared instance
    if (this.server != null) {
      return;
    }

    this.databaseVersion =  getPostgresVersion();
    options = startServer(databaseVersion);
  }

  public static boolean isAtLeastPg10() {
    // hackish ;-)
    return !getPostgresVersion().startsWith("9.");
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

  private static class ServerContainer<SELF extends ServerContainer<SELF>> extends GenericContainer<SELF> {

    public ServerContainer(String dockerImageName) {
      super(dockerImageName);
    }

    public SELF withFixedExposedPort(int hostPort, int containerPort) {
      super.addFixedExposedPort(hostPort, containerPort, InternetProtocol.TCP);
      return self();
    }
  }
}
