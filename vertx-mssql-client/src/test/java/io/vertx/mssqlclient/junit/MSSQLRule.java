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

import io.vertx.core.impl.Arguments;
import io.vertx.mssqlclient.MSSQLConnectOptions;
import org.junit.rules.ExternalResource;
import org.testcontainers.containers.Container.ExecResult;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.InternetProtocol;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import static io.vertx.mssqlclient.MSSQLConnectOptions.DEFAULT_PORT;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.WRITE;
import static org.testcontainers.containers.BindMode.READ_ONLY;

public class MSSQLRule extends ExternalResource {

  public static final MSSQLRule SHARED_INSTANCE = new MSSQLRule(false, false);

  private static final String USER = "SA";
  private static final String PASSWORD = "A_Str0ng_Required_Password";
  private static final String INIT_SQL = "/opt/data/init.sql";

  private final boolean tls;
  private final boolean forceEncryption;

  private ServerContainer<?> server;
  private MSSQLConnectOptions options;
  private Path conf;

  public MSSQLRule(boolean tls, boolean forceEncryption) {
    Arguments.require(!forceEncryption || tls, "Cannot force encryption without TLS support");
    this.tls = tls;
    this.forceEncryption = forceEncryption;
  }

  @Override
  protected void before() throws IOException {
    String connectionUri = System.getProperty("connection.uri");
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
    if (isNullOrEmpty(System.getProperty("connection.uri")) && this != SHARED_INSTANCE) {
      stopMSSQL();
    }
  }

  private MSSQLConnectOptions startMSSQL() throws IOException {
    String containerVersion = System.getProperty("mssql-container.version");
    if (containerVersion == null || containerVersion.isEmpty()) {
      containerVersion = "2017-latest";
    }
    server = new ServerContainer<>("mcr.microsoft.com/mssql/server:" + containerVersion)
      .withEnv("ACCEPT_EULA", "Y")
      .withEnv("TZ", "UTC")
      .withEnv("SA_PASSWORD", PASSWORD)
      .withClasspathResourceMapping("init.sql", INIT_SQL, READ_ONLY)
      .waitingFor(Wait.forLogMessage(".*Service Broker manager has started.*\\n", 1));

    if (System.getProperties().containsKey("containerFixedPort")) {
      server.withFixedExposedPort(DEFAULT_PORT, DEFAULT_PORT);
    } else {
      server.withExposedPorts(DEFAULT_PORT);
    }
    if (tls) {
      conf = createConf();
      server
        .withFileSystemBind(conf.toAbsolutePath().toString(), "/var/opt/mssql/mssql.conf", READ_ONLY)
        .withClasspathResourceMapping("mssql.key", "/etc/ssl/certs/mssql.key", READ_ONLY)
        .withClasspathResourceMapping("mssql.pem", "/etc/ssl/certs/mssql.pem", READ_ONLY);
    }
    server.start();

    initDb();

    return new MSSQLConnectOptions()
      .setHost(server.getContainerIpAddress())
      .setPort(server.getMappedPort(DEFAULT_PORT))
      .setUser("SA")
      .setPassword(PASSWORD);
  }

  private void initDb() throws IOException {
    try {
      ExecResult execResult = server.execInContainer(
        "/opt/mssql-tools/bin/sqlcmd",
        "-S", "localhost",
        "-U", USER,
        "-P", PASSWORD,
        "-i", INIT_SQL
      );
      if (execResult.getExitCode() != 0) {
        throw new RuntimeException(String.format("Failure while initializing database%nstdout:%s%nstderr:%s%n", execResult.getStdout(), execResult.getStderr()));
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException(e);
    }
  }

  private Path createConf() throws IOException {
    Path path = Files.createTempFile("mssql.conf", null);
    URL resource = Objects.requireNonNull(getClass().getClassLoader().getResource("mssql.conf"));
    try (InputStream is = resource.openStream()) {
      Files.copy(is, path, REPLACE_EXISTING);
    }
    if (forceEncryption) {
      Files.write(path, "forceencryption = 1".getBytes(StandardCharsets.UTF_8), WRITE, APPEND);
    }
    return path;
  }

  private void stopMSSQL() {
    if (server != null) {
      try {
        server.stop();
      } finally {
        server = null;
      }
    }
    if (conf != null) {
      try {
        Files.delete(conf);
      } catch (IOException ignored) {
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
