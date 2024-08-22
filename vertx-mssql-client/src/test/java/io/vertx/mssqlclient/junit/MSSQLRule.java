/*
 * Copyright (c) 2011-2022 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.mssqlclient.junit;

import io.vertx.mssqlclient.MSSQLConnectOptions;
import org.junit.rules.ExternalResource;
import org.testcontainers.containers.Container.ExecResult;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.InternetProtocol;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.IOException;
import java.util.Objects;

import static io.vertx.mssqlclient.MSSQLConnectOptions.DEFAULT_PORT;
import static io.vertx.mssqlclient.junit.MSSQLRule.Config.STANDARD;
import static org.testcontainers.containers.BindMode.READ_ONLY;

public class MSSQLRule extends ExternalResource {

  public enum Config {
    STANDARD("connection.uri", null),
    TLS("tls.connection.uri", "mssql-tls.conf"),
    FORCE_ENCRYPTION("force.encryption.connection.uri", "mssql-force-encryption.conf");

    private final String connectionUriSystemProperty;
    private final String mssqlConf;

    Config(String connectionUriSystemProperty, String mssqlConf) {
      this.connectionUriSystemProperty = connectionUriSystemProperty;
      this.mssqlConf = mssqlConf;
    }
  }

  public static final MSSQLRule SHARED_INSTANCE = new MSSQLRule(STANDARD);

  private static final String USER = "SA";
  private static final String PASSWORD = "A_Str0ng_Required_Password";
  private static final String INIT_SQL = "/opt/data/init.sql";

  private final Config config;

  private ServerContainer<?> server;
  private MSSQLConnectOptions options;

  public MSSQLRule(Config config) {
    this.config = Objects.requireNonNull(config);
  }

  @Override
  protected void before() throws IOException {
    String connectionUri = System.getProperty(config.connectionUriSystemProperty);
    if (!isNullOrEmpty(connectionUri)) {
      // use an external database for testing
      options = MSSQLConnectOptions.fromUri(connectionUri);
    } else if (this.server == null) {
      options = startMSSQL();
    }
  }

  private boolean isNullOrEmpty(String connectionUri) {
    return connectionUri == null || connectionUri.isEmpty();
  }

  @Override
  protected void after() {
    if (isNullOrEmpty(System.getProperty(config.connectionUriSystemProperty)) && this != SHARED_INSTANCE) {
      stopMSSQL();
    }
  }

  private MSSQLConnectOptions startMSSQL() throws IOException {
    String containerVersion = System.getProperty("mssql-container.version");
    if (isNullOrEmpty(containerVersion)) {
      containerVersion = "2019-latest";
    }
    server = new ServerContainer<>("mcr.microsoft.com/mssql/server:" + containerVersion)
      .withEnv("ACCEPT_EULA", "Y")
      .withEnv("TZ", "UTC")
      .withEnv("SA_PASSWORD", PASSWORD)
      .withClasspathResourceMapping("init.sql", INIT_SQL, READ_ONLY)
      .waitingFor(Wait.forLogMessage(".*The tempdb database has \\d+ data file\\(s\\).*\\n", 2));

    if (System.getProperties().containsKey("containerFixedPort")) {
      server.withFixedExposedPort(DEFAULT_PORT, DEFAULT_PORT);
    } else {
      server.withExposedPorts(DEFAULT_PORT);
    }
    if (!isNullOrEmpty(config.mssqlConf)) {
      server
        .withClasspathResourceMapping(config.mssqlConf, "/var/opt/mssql/mssql.conf", READ_ONLY)
        .withClasspathResourceMapping("mssql.key", "/etc/ssl/certs/mssql.key", READ_ONLY)
        .withClasspathResourceMapping("mssql.pem", "/etc/ssl/certs/mssql.pem", READ_ONLY);
    }
    server.start();

    initDb();

    return new MSSQLConnectOptions()
      .setHost(server.getHost())
      .setPort(server.getMappedPort(DEFAULT_PORT))
      .setUser("SA")
      .setPassword(PASSWORD);
  }

  private void initDb() throws IOException {
    try {
      ExecResult execResult = server.execInContainer(
        "/opt/mssql-tools18/bin/sqlcmd",
        "-S", "localhost",
        "-U", USER,
        "-P", PASSWORD,
        "-i", INIT_SQL,
        "-C", "-No"
      );
      if (execResult.getExitCode() != 0) {
        throw new RuntimeException(String.format("Failure while initializing database%nstdout:%s%nstderr:%s%n", execResult.getStdout(), execResult.getStderr()));
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException(e);
    }
  }

  private void stopMSSQL() {
    if (server != null) {
      try {
        server.stop();
      } finally {
        server = null;
      }
    }
  }

  public MSSQLConnectOptions options() {
    return new MSSQLConnectOptions(options);
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
