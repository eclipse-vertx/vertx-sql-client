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

package io.vertx.mysqlclient.tck;

import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.mysqlclient.MySQLConnectOptions;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.testcontainers.containers.Container.ExecResult;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import static java.lang.String.format;

@RunWith(VertxUnitRunner.class)
public class ProxySQLConnectionTest extends MySQLPreparedQueryTest {

  private static final String MONITORING_USER = "proxysql";
  private static final String MONITORING_USER_PASSWORD = "proxysql1234#";

  private static GenericContainer<?> PROXY_SQL;

  @BeforeClass
  public static void beforeClass() throws Exception {
    PROXY_SQL = new GenericContainer<>("proxysql/proxysql")
      .withCreateContainerCmdModifier(createContainerCmd -> {
        createContainerCmd.getHostConfig().withNetworkMode(rule.network());
      })
      .withExposedPorts(6032, 6033, 6070)
      .waitingFor(Wait.forLogMessage(".*Latest ProxySQL version available.*\\n", 1));

    PROXY_SQL.start();

    execStatement("UPDATE global_variables SET variable_value='false' WHERE variable_name='admin-hash_passwords'");
    execStatement("LOAD ADMIN VARIABLES TO RUNTIME");

    execStatement(format("UPDATE global_variables SET variable_value='%s' WHERE variable_name='mysql-monitor_username'", MONITORING_USER));
    execStatement(format("UPDATE global_variables SET variable_value='%s' WHERE variable_name='mysql-monitor_password'", MONITORING_USER_PASSWORD));

    execStatement("LOAD MYSQL VARIABLES TO RUNTIME");

    execStatement(format("INSERT INTO mysql_servers(hostgroup_id, hostname, port) VALUES (0,'%s',3306)", rule.networkAlias()));
    execStatement("LOAD MYSQL SERVERS TO RUNTIME");

    execStatement(format("INSERT INTO mysql_users (username,password) VALUES ('%s','%s')", rule.options().getUser(), rule.options().getPassword()));
    execStatement("LOAD MYSQL USERS TO RUNTIME");
  }

  private static void execStatement(String statement) throws Exception {
    ExecResult result = PROXY_SQL.execInContainer("mysql", "-u", "admin", "-p" + "admin", "-h", "127.0.0.1", "-P", "6032", "-e", statement);
    if (result.getExitCode() != 0) {
      throw new RuntimeException("Failed to execute statement: " + statement + "\n" + result.getStderr());
    }
  }

  @AfterClass
  public static void afterClass() throws Exception {
    if (PROXY_SQL != null) {
      PROXY_SQL.stop();
    }
  }

  @Override
  protected void initConnector() {
    options = new MySQLConnectOptions(rule.options())
      .setHost(PROXY_SQL.getHost())
      .setPort(PROXY_SQL.getMappedPort(6033));
    connector = ClientConfig.CONNECT.connect(vertx, options);
  }

  @Test
  @Ignore("Fetch command not supported by ProxySQL")
  @Override
  public void testQueryCursor(TestContext ctx) {
    super.testQueryCursor(ctx);
  }

  @Test
  @Ignore("Fetch command not supported by ProxySQL")
  @Override
  public void testQueryCloseCursor(TestContext ctx) {
    super.testQueryCloseCursor(ctx);
  }

  @Test
  @Ignore("Fetch command not supported by ProxySQL")
  @Override
  public void testQueryStreamCloseCursor(TestContext ctx) {
    super.testQueryStreamCloseCursor(ctx);
  }

  @Test
  @Ignore("Fetch command not supported by ProxySQL")
  @Override
  public void testStreamQuery(TestContext ctx) {
    super.testStreamQuery(ctx);
  }

  @Test
  @Ignore("Fetch command not supported by ProxySQL")
  @Override
  public void testStreamQueryPauseInBatch(TestContext ctx) {
    super.testStreamQueryPauseInBatch(ctx);
  }

  @Test
  @Ignore("Fetch command not supported by ProxySQL")
  @Override
  public void testStreamQueryPauseInBatchFromAnotherThread(TestContext ctx) {
    super.testStreamQueryPauseInBatchFromAnotherThread(ctx);
  }

  @Test
  @Ignore("Fetch command not supported by ProxySQL")
  @Override
  public void testStreamQueryPauseResume(TestContext ctx) {
    super.testStreamQueryPauseResume(ctx);
  }
}
