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

import io.vertx.mssqlclient.MSSQLConnectOptions;
import org.junit.rules.ExternalResource;
import org.testcontainers.containers.MSSQLServerContainer;

import java.time.ZoneId;

public class MSSQLRule extends ExternalResource {
  private MSSQLServerContainer<?> server;
  private MSSQLConnectOptions options;

  public static final MSSQLRule SHARED_INSTANCE = new MSSQLRule();

  @Override
  protected void before() {
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

  private MSSQLConnectOptions startMSSQL() {
    String containerVersion = System.getProperty("mssql-container.version");
    if (containerVersion == null || containerVersion.isEmpty()) {
      containerVersion = "2017-latest";
    }
    server = new MSSQLServerContainer<>("mcr.microsoft.com/mssql/server:" + containerVersion)
      .acceptLicense()
      .withEnv("TZ", ZoneId.systemDefault().toString())
      .withInitScript("init.sql")
      .withExposedPorts(MSSQLServerContainer.MS_SQL_SERVER_PORT);
    server.start();

    return new MSSQLConnectOptions()
      .setHost(server.getContainerIpAddress())
      .setPort(server.getMappedPort(MSSQLServerContainer.MS_SQL_SERVER_PORT))
      .setUser(server.getUsername())
      .setPassword(server.getPassword());
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
}
