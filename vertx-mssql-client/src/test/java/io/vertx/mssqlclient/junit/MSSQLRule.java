/*
 * Copyright (c) 2011-2021 Contributors to the Eclipse Foundation
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
import org.testcontainers.containers.InternetProtocol;
import org.testcontainers.containers.MSSQLServerContainer;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneId;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.WRITE;
import static org.testcontainers.containers.BindMode.READ_ONLY;
import static org.testcontainers.containers.MSSQLServerContainer.MS_SQL_SERVER_PORT;

public class MSSQLRule extends ExternalResource {

  public static final MSSQLRule SHARED_INSTANCE = new MSSQLRule(false, false);

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
    if (isNullOrEmpty(System.getProperty("connection.uri")) || this != SHARED_INSTANCE) {
      stopMSSQL();
    }
  }

  private MSSQLConnectOptions startMSSQL() throws IOException {
    String containerVersion = System.getProperty("mssql-container.version");
    if (containerVersion == null || containerVersion.isEmpty()) {
      containerVersion = "2017-latest";
    }
    server = new ServerContainer<>("mcr.microsoft.com/mssql/server:" + containerVersion)
      .acceptLicense()
      .withEnv("TZ", ZoneId.systemDefault().toString())
      .withInitScript("init.sql");
    if (System.getProperties().containsKey("containerFixedPort")) {
      server.withFixedExposedPort(MS_SQL_SERVER_PORT, MS_SQL_SERVER_PORT);
    } else {
      server.withExposedPorts(MS_SQL_SERVER_PORT);
    }
    if (tls) {
      conf = createConf();
      server
        .withFileSystemBind(conf.toAbsolutePath().toString(), "/var/opt/mssql/mssql.conf", READ_ONLY)
        .withClasspathResourceMapping("mssql.key", "/etc/ssl/certs/mssql.key", READ_ONLY)
        .withClasspathResourceMapping("mssql.pem", "/etc/ssl/certs/mssql.pem", READ_ONLY);
    }
    server.start();

    return new MSSQLConnectOptions()
      .setHost(server.getContainerIpAddress())
      .setPort(server.getMappedPort(MS_SQL_SERVER_PORT))
      .setUser(server.getUsername())
      .setPassword(server.getPassword());
  }

  private Path createConf() throws IOException {
    Path path = Files.createTempFile("mssql.conf", null);
    URL resource = getClass().getClassLoader().getResource("mssql.conf");
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

  private static class ServerContainer<SELF extends ServerContainer<SELF>> extends MSSQLServerContainer<SELF> {

    public ServerContainer(String dockerImageName) {
      super(dockerImageName);
    }

    public SELF withFixedExposedPort(int hostPort, int containerPort) {
      super.addFixedExposedPort(hostPort, containerPort, InternetProtocol.TCP);
      return self();
    }
  }
}
