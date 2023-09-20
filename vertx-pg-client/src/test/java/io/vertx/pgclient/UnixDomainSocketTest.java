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

import io.vertx.ext.unit.Async;
import io.vertx.pgclient.junit.PgRule;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.impl.ConnectionFactoryBase;
import org.junit.*;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

@RunWith(VertxUnitRunner.class)
public class UnixDomainSocketTest {

  private static final String unixSocketDirectory = System.getProperty("unix.socket.directory");
  private static final String unixSocketPort = System.getProperty("unix.socket.port");
  private static final boolean nativeTransportEnabled;

  static {
    Vertx vertx = Vertx.vertx(new VertxOptions().setPreferNativeTransport(true));
    nativeTransportEnabled = vertx.isNativeTransportEnabled();
    vertx.close();
  }

  @ClassRule
  public static PgRule rule = new PgRule().domainSockets(nativeTransportEnabled);
  private Pool client;
  private PgConnectOptions options;

  @Before
  public void before() {
    options = rule.options();
    if (unixSocketDirectory != null && !unixSocketDirectory.isEmpty()) {
      options.setHost(unixSocketDirectory);
    }
    if (unixSocketPort != null && !unixSocketPort.isEmpty()) {
      options.setPort(Integer.parseInt(unixSocketPort));
    }
  }

  @After
  public void after(TestContext ctx) {
    if (client != null) {
      client.close(ctx.asyncAssertSuccess());
    }
  }

  @Test
  public void uriTest(TestContext context) {
    assumeTrue(options.isUsingDomainSocket());
    String uri = "postgresql://postgres:postgres@/postgres?host=" + options.getHost() + "&port=" + options.getPort();
    PgConnectOptions connectOptions = PgConnectOptions.fromUri("postgresql://postgres:postgres@/postgres?host=" + options.getHost() + "&port=" + options.getPort());
    client = PgBuilder.pool(builder -> builder.connectingTo(connectOptions));
    client.getConnection(context.asyncAssertSuccess(pgConnection -> pgConnection.close()));
  }

  @Test
  public void simpleConnect(TestContext context) {
    assumeTrue(options.isUsingDomainSocket());
    client = PgBuilder.pool(builder -> builder.connectingTo(new PgConnectOptions(options)));
    client.getConnection(context.asyncAssertSuccess(pgConnection -> pgConnection.close(context.asyncAssertSuccess())));
  }

  @Test
  public void connectWithVertxInstance(TestContext context) {
    assumeTrue(options.isUsingDomainSocket());
    Vertx vertx = Vertx.vertx(new VertxOptions().setPreferNativeTransport(true));
    try {
      client = PgBuilder.pool(builder -> builder.connectingTo(new PgConnectOptions(options)).using(vertx));
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
    assumeTrue(options.isUsingDomainSocket());
    client = PgBuilder.pool(builder -> builder.connectingTo(new PgConnectOptions(options).setSslMode(SslMode.REQUIRE)));
    client.getConnection(context.asyncAssertSuccess(pgConnection -> {
      assertFalse(pgConnection.isSSL());
      pgConnection.close();
    }));
  }

  @Test
  public void testNativeTransportMustBeEnabled(TestContext ctx) {
    Vertx vertx = Vertx.vertx();
    try {
      Pool pool = PgBuilder.pool(builder -> builder
        .connectingTo(PgConnectOptions.fromUri("postgresql:///dbname?host=/var/lib/postgresql"))
        .using(vertx));
      Async async = ctx.async();
      pool.getConnection().onComplete(ctx.asyncAssertFailure(err -> {
        assertEquals(ConnectionFactoryBase.NATIVE_TRANSPORT_REQUIRED, err.getMessage());
        async.complete();
      }));
      async.await(20_000);
    } finally {
      vertx.close();
    }
  }
}
