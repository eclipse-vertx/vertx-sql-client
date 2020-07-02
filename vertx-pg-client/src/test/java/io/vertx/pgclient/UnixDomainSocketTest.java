/*
 * Copyright (C) 2018 Julien Viet
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package io.vertx.pgclient;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.pgclient.junit.ContainerPgRule;
import io.vertx.sqlclient.PoolOptions;
import org.apache.commons.lang3.SystemUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertFalse;
import static org.junit.Assume.assumeTrue;

@RunWith(VertxUnitRunner.class)
public class UnixDomainSocketTest {

  private static final String unixSocketDirectory = System.getProperty("unix.socket.directory");
  private static final String unixSocketPort = System.getProperty("unix.socket.port");

  @ClassRule
  public static ContainerPgRule rule = new ContainerPgRule().unixDomainSocket();

  private PgPool client;
  private PgConnectOptions options;

  @Before
  public void before() {
    assumeTrue(SystemUtils.IS_OS_UNIX);
    options = rule.options();
    if (unixSocketDirectory != null && !unixSocketDirectory.isEmpty()) {
      options.setHost(unixSocketDirectory);
    }
    if (unixSocketPort != null && !unixSocketPort.isEmpty()) {
      options.setPort(Integer.parseInt(unixSocketPort));
    }
  }

  @After
  public void after() {
    if (client != null) {
      client.close();
    }
  }

  @Test
  public void uriTest(TestContext context) {
    String uri = "postgresql://postgres:postgres@/postgres?host=" + options.getHost() + "&port=" + options.getPort();
    client = PgPool.pool(uri);
    client.getConnection(context.asyncAssertSuccess(pgConnection -> pgConnection.close()));
  }

  @Test
  public void simpleConnect(TestContext context) {
    client = PgPool.pool(new PgConnectOptions(options), new PoolOptions());
    client.getConnection(context.asyncAssertSuccess(pgConnection -> pgConnection.close()));
  }

  @Test
  public void connectWithVertxInstance(TestContext context) {
    Vertx vertx = Vertx.vertx(new VertxOptions().setPreferNativeTransport(true));
    try {
      client = PgPool.pool(vertx, new PgConnectOptions(options), new PoolOptions());
      Async async = context.async();
      client.getConnection(context.asyncAssertSuccess(pgConnection -> {
        async.complete();
        pgConnection.close();
      }));
      async.await();
    } finally {
      vertx.close();
    }
  }

  @Test
  public void testIgnoreSslMode(TestContext context) {
    client = PgPool.pool(new PgConnectOptions(options).setSslMode(SslMode.REQUIRE), new PoolOptions());
    client.getConnection(context.asyncAssertSuccess(pgConnection -> {
      assertFalse(pgConnection.isSSL());
      pgConnection.close();
    }));
  }
}
