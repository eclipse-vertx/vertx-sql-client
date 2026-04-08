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
import io.vertx.core.buffer.Buffer;
import io.vertx.core.dns.AddressResolverOptions;
import io.vertx.core.net.ClientSSLOptions;
import io.vertx.core.net.PemTrustOptions;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgConnection;
import io.vertx.pgclient.SslMode;
import io.vertx.pgclient.SslNegotiation;
import io.vertx.sqlclient.Tuple;
import io.vertx.tests.pgclient.junit.ContainerPgRule;
import org.junit.*;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class TLSTest {

  @ClassRule
  public static ContainerPgRule ruleOptionalSll = new ContainerPgRule().ssl(true);

  @ClassRule
  public static ContainerPgRule ruleForceSsl = new ContainerPgRule().ssl(true).forceSsl(true);

  @ClassRule
  public static ContainerPgRule ruleSllOff = new ContainerPgRule().ssl(false);

  private Vertx vertx;

  @Before
  public void setup() {
    vertx = Vertx.vertx();
  }

  @After
  public void teardown(TestContext ctx) {
    vertx.close().onComplete(ctx.asyncAssertSuccess());
  }

  @Test
  public void testTLS(TestContext ctx) {
    Async async = ctx.async();

    PgConnectOptions options = new PgConnectOptions(ruleOptionalSll.options())
      .setSslMode(SslMode.REQUIRE)
      .setSslOptions(new ClientSSLOptions().setTrustOptions(new PemTrustOptions().addCertPath("tls/server.crt")));
    PgConnection.connect(vertx, options.setSslMode(SslMode.REQUIRE)).onComplete(ctx.asyncAssertSuccess(conn -> {
      ctx.assertTrue(conn.isSSL());
      conn
        .query("SELECT * FROM Fortune WHERE id=1")
        .execute()
        .onComplete(ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(1, result.size());
          Tuple row = result.iterator().next();
          ctx.assertEquals(1, row.getInteger(0));
          ctx.assertEquals("fortune: No such file or directory", row.getString(1));
          async.complete();
        }));
    }));
  }

  @Test
  public void testTLSTrustAll(TestContext ctx) {
    Async async = ctx.async();
    PgConnection.connect(vertx, ruleOptionalSll.options().setSslMode(SslMode.REQUIRE).setSslOptions(new ClientSSLOptions().setTrustAll(true))).onComplete(ctx.asyncAssertSuccess(conn -> {
      ctx.assertTrue(conn.isSSL());
      async.complete();
    }));
  }

  @Test
  public void testTLSInvalidCertificate(TestContext ctx) {
    Async async = ctx.async();
    PgConnection.connect(vertx, ruleOptionalSll.options().setSslMode(SslMode.REQUIRE).setSslOptions(new ClientSSLOptions().setTrustOptions(new PemTrustOptions().addCertPath("tls/another.crt")))).onComplete(ctx.asyncAssertFailure(err -> {
//      ctx.assertEquals(err.getClass(), VertxException.class);
      ctx.assertEquals(err.getMessage(), "SSL handshake failed");
      async.complete();
    }));
  }

  @Test
  public void testSslModeDisable(TestContext ctx) {
    Async async = ctx.async();
    PgConnectOptions options = ruleOptionalSll.options()
      .setSslMode(SslMode.DISABLE);
    PgConnection.connect(vertx, new PgConnectOptions(options)).onComplete(ctx.asyncAssertSuccess(conn -> {
      ctx.assertFalse(conn.isSSL());
      async.complete();
    }));
  }

  @Test
  public void testSslModeAllow(TestContext ctx) {
    Async async = ctx.async();
    PgConnectOptions options = ruleOptionalSll.options()
      .setSslMode(SslMode.ALLOW);
    PgConnection.connect(vertx, new PgConnectOptions(options)).onComplete(ctx.asyncAssertSuccess(conn -> {
      ctx.assertFalse(conn.isSSL());
      async.complete();
    }));
  }

  @Test
  public void testSslModeAllowFallback(TestContext ctx) {
    Async async = ctx.async();
    PgConnectOptions options = ruleForceSsl.options()
      .setSslMode(SslMode.ALLOW)
      .setSslOptions(new ClientSSLOptions().setTrustAll(true));
    PgConnection.connect(vertx, new PgConnectOptions(options)).onComplete(ctx.asyncAssertSuccess(conn -> {
      ctx.assertTrue(conn.isSSL());
      async.complete();
    }));
  }

  @Test
  public void testSslModePrefer(TestContext ctx) {
    Async async = ctx.async();
    PgConnectOptions options = ruleOptionalSll.options()
      .setSslMode(SslMode.PREFER)
      .setSslOptions(new ClientSSLOptions().setTrustAll(true));
    PgConnection.connect(vertx, new PgConnectOptions(options)).onComplete(ctx.asyncAssertSuccess(conn -> {
      ctx.assertTrue(conn.isSSL());
      async.complete();
    }));
  }

  @Test
  public void testSslModePreferFallback(TestContext ctx) {
    Async async = ctx.async();
    PgConnectOptions options = ruleSllOff.options()
      .setSslMode(SslMode.PREFER)
      .setSslOptions(new ClientSSLOptions().setTrustAll(true));
    PgConnection.connect(vertx, options).onComplete(ctx.asyncAssertSuccess(conn -> {
      ctx.assertFalse(conn.isSSL());
      async.complete();
    }));
  }

  @Test
  public void testSslModeVerifyCaConf(TestContext ctx) {
    PgConnectOptions options = ruleOptionalSll.options()
      .setSslMode(SslMode.VERIFY_CA)
      .setSslOptions(new ClientSSLOptions().setTrustAll(true));
    PgConnection.connect(vertx, new PgConnectOptions(options)).onComplete(ctx.asyncAssertFailure(error -> {
      ctx.assertEquals("Trust options must be specified under verify-full or verify-ca sslmode", error.getMessage());
    }));
  }

  @Test
  public void testSslModeVerifyFullConf(TestContext ctx) {
    PgConnectOptions options = ruleOptionalSll.options()
      .setSslOptions(new ClientSSLOptions().setTrustOptions(new PemTrustOptions().addCertPath("tls/another.crt")))
      .setSslMode(SslMode.VERIFY_FULL);
    PgConnection.connect(vertx, new PgConnectOptions(options)).onComplete(ctx.asyncAssertFailure(error -> {
      ctx.assertEquals("Host verification algorithm must be specified under verify-full sslmode", error.getMessage());
    }));
  }

  @Test
  public void testSslModeVerifyFullInvalidHostname(TestContext ctx) {
    PgConnectOptions options = ruleOptionalSll.options()
      .setSslMode(SslMode.VERIFY_FULL)
      // The hostname in the test certificate is thebrain.ca, so 'localhost' should make for a failed connection
      .setHost("localhost")
      .setSslOptions(
        new ClientSSLOptions()
          .setHostnameVerificationAlgorithm("HTTPS")
          .setTrustOptions(new PemTrustOptions().addCertPath("tls/server.crt"))
      );

    PgConnection.connect(vertx, options).onComplete( ctx.asyncAssertFailure(err -> {
      ctx.assertEquals(err.getMessage(), "SSL handshake failed");
    }));
  }

  @Test
  public void testSslModeVerifyFullCorrectHostname(TestContext ctx) {
    Vertx vertxWithHosts = Vertx.vertx(
      new VertxOptions()
        .setAddressResolverOptions(
          new AddressResolverOptions()
            .setHostsValue(Buffer.buffer("127.0.0.1 thebrain.ca\n"))
        )
    );

    PgConnectOptions options = ruleOptionalSll.options()
      .setSslMode(SslMode.VERIFY_FULL)
      // The hostname in the test certificate is thebrain.ca
      .setHost("thebrain.ca")
      .setSslOptions(
        new ClientSSLOptions()
          .setHostnameVerificationAlgorithm("HTTPS")
          .setTrustOptions(new PemTrustOptions().addCertPath("tls/server.crt"))
      );

    PgConnection.connect(vertxWithHosts, options).onComplete( ctx.asyncAssertSuccess(conn -> {
      ctx.assertTrue(conn.isSSL());
      vertxWithHosts.close();
    }));
  }

  @Test
  public void testSslNegotiationDirect(TestContext ctx) {
    Assume.assumeTrue("Requires PostgreSQL 17+", ContainerPgRule.isAtLeastPg17());

    Async async = ctx.async();
    PgConnectOptions options = new PgConnectOptions(ruleOptionalSll.options())
      .setSslMode(SslMode.REQUIRE)
      .setSslNegotiation(SslNegotiation.DIRECT)
      .setSslOptions(new ClientSSLOptions().setTrustAll(true));

    PgConnection.connect(vertx, options).onComplete(ctx.asyncAssertSuccess(conn -> {
      ctx.assertTrue(conn.isSSL());
      async.complete();
    }));
  }

  @Test
  public void testSslNegotiationDirectWithVerifyFull(TestContext ctx) {
    Assume.assumeTrue("Requires PostgreSQL 17+", ContainerPgRule.isAtLeastPg17());

    Async async = ctx.async();

    Vertx vertxWithHosts = Vertx.vertx(
      new VertxOptions()
        .setAddressResolverOptions(
          new AddressResolverOptions()
            .setHostsValue(Buffer.buffer("127.0.0.1 thebrain.ca\n"))
        )
    );

    PgConnectOptions options = new PgConnectOptions(ruleOptionalSll.options())
      .setSslMode(SslMode.VERIFY_FULL)
      .setSslNegotiation(SslNegotiation.DIRECT)
      .setHost("thebrain.ca")
      .setSslOptions(
        new ClientSSLOptions()
          .setHostnameVerificationAlgorithm("HTTPS")
          .setTrustOptions(new PemTrustOptions().addCertPath("tls/server.crt"))
      );

    PgConnection.connect(vertxWithHosts, options).onComplete(ctx.asyncAssertSuccess(conn -> {
      ctx.assertTrue(conn.isSSL());
      vertxWithHosts.close();
      async.complete();
    }));
  }
}
