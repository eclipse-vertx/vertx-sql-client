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
package io.vertx.oracleclient.test.tck;

import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.oracleclient.OracleConnectOptions;
import io.vertx.oracleclient.test.junit.OracleRule;
import io.vertx.sqlclient.tck.ConnectionAutoRetryTestBase;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class OracleConnectionAutoRetryTest extends ConnectionAutoRetryTestBase {
  @ClassRule
  public static OracleRule rule = OracleRule.SHARED_INSTANCE;

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
  protected void initialConnector(int proxyPort) {
    OracleConnectOptions proxyOptions = new OracleConnectOptions(options);
    proxyOptions.setPort(proxyPort);
    proxyOptions.setHost("localhost");
    connectionConnector = ClientConfig.CONNECT.connect(vertx, proxyOptions);
    poolConnector = ClientConfig.POOLED.connect(vertx, proxyOptions);
  }

  @Test
  @Ignore("Connection success, but any request get staled")
  public void testConnExceedingRetryLimit(TestContext ctx) {
    Async async = ctx.async();
    this.options.setReconnectAttempts(1);
    this.options.setReconnectInterval(1000L);
    UnstableProxyServer unstableProxyServer = new UnstableProxyServer(
      2);
    unstableProxyServer.initialize(this.options, ctx.asyncAssertSuccess((v) -> {
      this.initialConnector(unstableProxyServer.port());
      this.connectionConnector.connect(s -> {
        ctx.assertFalse(s.succeeded());
        async.complete();
      });
    }));
  }

  @Test
  @Ignore("Connection success, but any request get staled")
  public void testPoolExceedingRetryLimit(TestContext ctx) {
    this.options.setReconnectAttempts(1);
    this.options.setReconnectInterval(1000L);
    UnstableProxyServer unstableProxyServer = new UnstableProxyServer(
      2);
    unstableProxyServer.initialize(this.options, ctx.asyncAssertSuccess((v) -> {
      this.initialConnector(unstableProxyServer.port());
      this.poolConnector.connect(ctx.asyncAssertFailure((throwable) -> {
      }));
    }));
  }
}
