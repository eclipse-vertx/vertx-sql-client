/*
 * Copyright (c) 2011-2019 Contributors to the Eclipse Foundation
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

public class MSSQLRule extends ExternalResource {
  private MSSQLServerContainer<?> server;
  private MSSQLConnectOptions options;

  public static final MSSQLRule SHARED_INSTANCE = new MSSQLRule();

  @Override
  protected void before() {
    if (this.server == null) {
      this.options = startMSSQL();
    }
  }

  @Override
  protected void after() {
    if (this != SHARED_INSTANCE) {
      stopMSSQL();
    }
  }

  private MSSQLConnectOptions startMSSQL() {
    String containerVersion = System.getProperty("mssql-container.version");
    if (containerVersion == null || containerVersion.isEmpty()) {
      containerVersion = "2017-CU23";
    }
    server = new MSSQLServerContainer<>("mcr.microsoft.com/mssql/server:" + containerVersion)
      .acceptLicense()
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
