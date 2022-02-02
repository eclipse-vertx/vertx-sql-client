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
package io.vertx.oracleclient.test.junit;

import io.vertx.oracleclient.OracleConnectOptions;
import org.junit.rules.ExternalResource;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.InternetProtocol;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.IOException;
import java.time.Duration;

public class OracleRule extends ExternalResource {

  public static final OracleRule SHARED_INSTANCE = new OracleRule();

  static final String IMAGE = "gvenzl/oracle-xe";
  static final String PASSWORD = "vertx";
  static final int PORT = 1521;

  private ServerContainer<?> server;
  private OracleConnectOptions options;

  @Override
  protected void before() throws IOException {
    String connectionUri = System.getProperty("connection.uri");
    if (!isNullOrEmpty(connectionUri)) {
      // use an external database for testing
      options = OracleConnectOptions.fromUri(connectionUri);
    } else if (server == null) {
      options = startOracle();
    }
  }

  private boolean isNullOrEmpty(String s) {
    return s == null || s.isEmpty();
  }

  @Override
  protected void after() {
    if (isNullOrEmpty(System.getProperty("connection.uri")) || this != SHARED_INSTANCE) {
      stopOracle();
    }
  }

  private OracleConnectOptions startOracle() throws IOException {
    String containerVersion = System.getProperty("oracle-container.version");
    containerVersion = isNullOrEmpty(containerVersion) ? "18-slim" : containerVersion;

    String image = IMAGE + ":" + containerVersion;

    server = new ServerContainer<>(image)
      .withEnv("ORACLE_PASSWORD", PASSWORD)
      .withExposedPorts(PORT)
      .withClasspathResourceMapping("tck/import.sql", "/container-entrypoint-initdb.d/import.sql", BindMode.READ_ONLY)
      .withLogConsumer(of -> System.out.print("[ORACLE] " + of.getUtf8String()))
      .waitingFor(
        Wait.forLogMessage(".*DATABASE IS READY TO USE!.*\\n", 1)
      )
      .withStartupTimeout(Duration.ofMinutes(15));
    if (System.getProperties().containsKey("containerFixedPort")) {
      server.withFixedExposedPort(PORT, PORT);
    }

    server.start();

    return new OracleConnectOptions()
      .setHost(server.getContainerIpAddress())
      .setPort(server.getMappedPort(PORT))
      .setUser("sys as sysdba")
      .setPassword(PASSWORD)
      .setDatabase("xe");
  }

  private void stopOracle() {
    if (server != null) {
      try {
        server.stop();
      } finally {
        server = null;
      }
    }
  }

  public OracleConnectOptions options() {
    return new OracleConnectOptions(options);
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
