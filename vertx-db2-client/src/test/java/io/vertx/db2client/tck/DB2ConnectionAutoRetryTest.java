/*
 * Copyright (c) 2011-2020 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.db2client.tck;

import io.vertx.db2client.DB2ConnectOptions;
import io.vertx.db2client.junit.DB2Resource;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.sqlclient.SqlConnectOptions;
import io.vertx.sqlclient.SqlHost;
import io.vertx.sqlclient.tck.ConnectionAutoRetryTestBase;
import org.junit.ClassRule;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

@RunWith(VertxUnitRunner.class)
public class DB2ConnectionAutoRetryTest extends ConnectionAutoRetryTestBase {
  @ClassRule
  public static DB2Resource rule = DB2Resource.SHARED_INSTANCE;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    options = rule.options();
  }

  @Override
  public void tearDown(TestContext ctx) {
    connectionConnector.close();
    poolConnector.close();
    super.tearDown(ctx);
  }

  @Override
  protected void initialConnector(int... proxyPorts) {
    SqlConnectOptions proxyOptions = new DB2ConnectOptions(options);
    List<SqlHost> hosts = IntStream.of(proxyPorts).mapToObj(port -> new SqlHost("localhost", port)).collect(toList());
    proxyOptions.setHosts(hosts);
    connectionConnector = ClientConfig.CONNECT.connect(vertx, proxyOptions);
    poolConnector = ClientConfig.POOLED.connect(vertx, proxyOptions);
  }
}
