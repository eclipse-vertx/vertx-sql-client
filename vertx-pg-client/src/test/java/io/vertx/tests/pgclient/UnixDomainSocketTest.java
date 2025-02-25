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
package io.vertx.tests.pgclient;

import io.vertx.pgclient.PgBuilder;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.SslMode;
import io.vertx.tests.pgclient.junit.PgRule;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.SqlConnectOptions;
import io.vertx.sqlclient.impl.ConnectionFactoryBase;
import org.junit.*;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
  private Vertx vertx;

  @Before
  public void before() {
    vertx = Vertx.vertx(new VertxOptions().setPreferNativeTransport(true));
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
    if (vertx != null) {
      vertx.close().onComplete(ctx.asyncAssertSuccess());
    }
    if (client != null) {
      client.close().onComplete(ctx.asyncAssertSuccess());
    }
  }

  @Test
  public void uriTest(TestContext context) {
    assumeTrue(options.isUsingDomainSocket());
    String uri = "postgresql://postgres:postgres@/postgres?host=" + options.getHost() + "&port=" + options.getPort();
    client = PgBuilder.pool().connectingTo(uri).using(vertx).build();
    client
      .getConnection()
      .onComplete(context.asyncAssertSuccess(SqlClient::close));
  }

  @Test
  public void simpleConnect(TestContext context) {
    assumeTrue(options.isUsingDomainSocket());
    client = PgBuilder.pool().connectingTo(new PgConnectOptions(options)).using(vertx).build();
    client
      .getConnection()
      .onComplete(context.asyncAssertSuccess(pgConnection -> pgConnection.close().onComplete(context.asyncAssertSuccess())));
  }

  @Test
  public void connectWithVertxInstance(TestContext context) {
    assumeTrue(options.isUsingDomainSocket());
    client = PgBuilder.pool().connectingTo(new PgConnectOptions(options)).using(vertx).build();
    client
      .getConnection()
      .onComplete(context.asyncAssertSuccess(pgConnection -> {
        pgConnection.close();
      }));
  }

  @Ignore
  @Test
  public void testIgnoreSslMode(TestContext context) {
    assumeTrue(options.isUsingDomainSocket());
    client = PgBuilder.pool().connectingTo(new PgConnectOptions(options).setSslMode(SslMode.REQUIRE)).using(vertx).build();
    client
      .getConnection()
      .onComplete(context.asyncAssertSuccess(pgConnection -> {
      assertFalse(pgConnection.isSSL());
      pgConnection.close();
    }));
  }

  @Test
  public void testNativeTransportMustBeEnabled(TestContext ctx) {
    Pool pool = PgBuilder.pool().connectingTo(SqlConnectOptions.fromUri("postgresql:///dbname?host=/var/lib/postgresql")).build();
    pool.getConnection().onComplete(ctx.asyncAssertFailure(err -> {
      assertEquals(ConnectionFactoryBase.NATIVE_TRANSPORT_REQUIRED, err.getMessage());
    }));
  }
}
