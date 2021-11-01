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
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.time.Duration;

public class OracleRule extends ExternalResource {

  static final String IMAGE = "gvenzl/oracle-xe";
  static final String PASSWORD = "vertx";
  static final int PORT = 1521;

  static final GenericContainer<?> ORACLE_DB;

  static {
    String containerVersion = System.getProperty("oracle-container.version");
    if (containerVersion == null || containerVersion.isEmpty()) {
      containerVersion = "18-slim";
    }

    String image = IMAGE + ":" + containerVersion;

    ORACLE_DB = new GenericContainer<>(image)
      .withEnv("ORACLE_PASSWORD", PASSWORD)
      .withExposedPorts(PORT)
      .withFileSystemBind("src/test/resources/tck", "/container-entrypoint-initdb.d")
      .withLogConsumer(of -> System.out.print("[ORACLE] " + of.getUtf8String()))
      .waitingFor(
        Wait.forLogMessage(".*DATABASE IS READY TO USE!.*\\n", 1)
      )
      .withStartupTimeout(Duration.ofMinutes(15));

    ORACLE_DB.start();
  }

  public static String getPassword() {
    return PASSWORD;
  }

  public static String getUser() {
    return "sys as sysdba";
  }

  public static String getDatabaseHost() {
    return ORACLE_DB.getHost();
  }

  public static int getDatabasePort() {
    return ORACLE_DB.getMappedPort(1521);
  }

  public static String getDatabase() {
    return "xe";
  }

  private OracleConnectOptions options;

  public static final OracleRule SHARED_INSTANCE = new OracleRule();

  public synchronized OracleConnectOptions getOptions() throws Exception {
    return new OracleConnectOptions()
      .setPort(getDatabasePort())
      .setHost(getDatabaseHost())
      .setUser(getUser())
      .setPassword(getPassword())
      .setDatabase(getDatabase());
  }

  public OracleConnectOptions options() {
    return new OracleConnectOptions(options);
  }

  @Override
  protected void before() throws Throwable {
    options = getOptions();
  }

  @Override
  protected void after() {

  }
}
