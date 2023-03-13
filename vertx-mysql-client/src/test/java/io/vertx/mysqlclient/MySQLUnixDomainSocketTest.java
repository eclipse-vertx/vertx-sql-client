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
package io.vertx.mysqlclient;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.SqlClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import static org.junit.Assert.assertFalse;
import static org.junit.Assume.assumeTrue;

@RunWith(VertxUnitRunner.class)
public class MySQLUnixDomainSocketTest extends MySQLTestBase {

  private static final String unixSocketFile = System.getProperty("unix.socket.file");

  private MySQLPool client;
  private MySQLConnectOptions options;

  @Before
  public void setUp() {
    String osName = System.getProperty("os.name");
    assumeTrue(osName != null && (osName.startsWith("Linux") || osName.startsWith("LINUX")));
    options = new MySQLConnectOptions(MySQLTestBase.options);
    if (unixSocketFile != null && !unixSocketFile.isEmpty()) {
      options.setHost(unixSocketFile);
    } else {
      options.setHost(rule.domainSocketPath());
    }
    assumeTrue(options.isUsingDomainSocket());
  }

  @After
  public void after() {
    if (client != null) {
      client.close();
    }
  }

  @Test
  public void uriSocketHostTest(TestContext context) throws UnsupportedEncodingException {
    String path = URLEncoder.encode(options.getHost(), "UTF-8");
    uriTest(context, "mysql://" + options.getUser() + ":" + options.getPassword() + "@" + path);
  }

  @Test
  public void uriSocketAttributeTest(TestContext context) throws UnsupportedEncodingException {
    String path = URLEncoder.encode(options.getHost(), "UTF-8");
    uriTest(context, "mysql://" + options.getUser() + ":" + options.getPassword() + "@192.168.0.67?socket=" + path);
  }

  private void uriTest(TestContext context, String uri) throws UnsupportedEncodingException {
    client = MySQLPool.pool(uri);
    client
      .getConnection()
      .onComplete(context.asyncAssertSuccess(SqlClient::close));
  }

  @Test
  public void simpleConnect(TestContext context) {
    client = MySQLPool.pool(new MySQLConnectOptions(options), new PoolOptions());
    client
      .getConnection()
      .onComplete(context.asyncAssertSuccess(SqlClient::close));
  }

  @Test
  public void connectWithVertxInstance(TestContext context) {
    Vertx vertx = Vertx.vertx(new VertxOptions().setPreferNativeTransport(true));
    try {
      client = MySQLPool.pool(vertx, new MySQLConnectOptions(options), new PoolOptions());
      Async async = context.async();
      client
        .getConnection()
        .onComplete(context.asyncAssertSuccess(conn -> {
        async.complete();
        conn.close();
      }));
      async.await();
    } finally {
      vertx.close();
    }
  }

  @Test
  public void testIgnoreSslMode(TestContext context) {
    client = MySQLPool.pool(new MySQLConnectOptions(options).setSslMode(SslMode.REQUIRED), new PoolOptions());
    client
      .getConnection()
      .onComplete(context.asyncAssertSuccess(conn -> {
      assertFalse(conn.isSSL());
      conn.close();
    }));
  }
}
