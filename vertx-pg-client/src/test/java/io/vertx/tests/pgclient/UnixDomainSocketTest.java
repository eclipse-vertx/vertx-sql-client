/*
 * Copyright (c) 2011-2026 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */
package io.vertx.tests.pgclient;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.pgclient.PgBuilder;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.SslMode;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.SqlConnectOptions;
import io.vertx.sqlclient.impl.ConnectionFactoryBase;
import io.vertx.tests.pgclient.junit.ContainerPgRule;
import org.junit.*;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assume.assumeTrue;

@RunWith(VertxUnitRunner.class)
public class UnixDomainSocketTest {

  @ClassRule
  public static final ContainerPgRule rule = ContainerPgRule.SHARED_INSTANCE;
  private Pool client;
  private PgConnectOptions options;
  private Vertx vertx;

  @Before
  public void before() {
    Assume.assumeNotNull(rule.domainSocketPath());
    // Only use native transport for JDK < 16 (Unix domain socket support added in JDK 16)
    boolean useNativeTransport = Runtime.version().feature() < 16;
    vertx = Vertx.vertx(new VertxOptions().setPreferNativeTransport(useNativeTransport));
    assertEquals(useNativeTransport, vertx.isNativeTransportEnabled());
    options = rule.options().setHost(rule.domainSocketPath()).setPort(5432);
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
    String uri = "postgresql://postgres:postgres@/postgres?host=" + options.getHost() + "&port=" + options.getPort();
    client = PgBuilder.pool().connectingTo(uri).using(vertx).build();
    client
      .getConnection()
      .onComplete(context.asyncAssertSuccess(SqlClient::close));
  }

  @Test
  public void simpleConnect(TestContext context) {
    client = PgBuilder.pool().connectingTo(new PgConnectOptions(options)).using(vertx).build();
    client
      .getConnection()
      .onComplete(context.asyncAssertSuccess(pgConnection -> pgConnection.close().onComplete(context.asyncAssertSuccess())));
  }

  @Test
  public void connectWithVertxInstance(TestContext context) {
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
    assumeTrue("Unix Domain Sockets are supported on Java 16+", Runtime.version().feature() < 16);
    Pool pool = PgBuilder.pool().connectingTo(SqlConnectOptions.fromUri("postgresql:///dbname?host=/var/lib/postgresql")).build();
    pool.getConnection().onComplete(ctx.asyncAssertFailure(err -> {
      assertEquals(ConnectionFactoryBase.UDS_NOT_SUPPORTED, err);
    }));
  }
}
