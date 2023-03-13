/*
 * Copyright (C) 2017 Julien Viet
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
import io.vertx.core.net.PemTrustOptions;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.pgclient.junit.ContainerPgRule;
import io.vertx.sqlclient.Tuple;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class TLSTest {

  @ClassRule
  public static ContainerPgRule rule = new ContainerPgRule().ssl(true);

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

    PgConnectOptions options = new PgConnectOptions(rule.options())
      .setSslMode(SslMode.REQUIRE)
      .setPemTrustOptions(new PemTrustOptions().addCertPath("tls/server.crt"));
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
    PgConnection.connect(vertx, rule.options().setSslMode(SslMode.REQUIRE).setTrustAll(true)).onComplete(ctx.asyncAssertSuccess(conn -> {
      ctx.assertTrue(conn.isSSL());
      async.complete();
    }));
  }

  @Test
  public void testTLSInvalidCertificate(TestContext ctx) {
    Async async = ctx.async();
    PgConnection.connect(vertx, rule.options().setSslMode(SslMode.REQUIRE)).onComplete(ctx.asyncAssertFailure(err -> {
//      ctx.assertEquals(err.getClass(), VertxException.class);
      ctx.assertEquals(err.getMessage(), "SSL handshake failed");
      async.complete();
    }));
  }

  @Test
  public void testSslModeDisable(TestContext ctx) {
    Async async = ctx.async();
    PgConnectOptions options = rule.options()
      .setSslMode(SslMode.DISABLE);
    PgConnection.connect(vertx, new PgConnectOptions(options)).onComplete(ctx.asyncAssertSuccess(conn -> {
      ctx.assertFalse(conn.isSSL());
      async.complete();
    }));
  }

  @Test
  public void testSslModeAllow(TestContext ctx) {
    Async async = ctx.async();
    PgConnectOptions options = rule.options()
      .setSslMode(SslMode.ALLOW);
    PgConnection.connect(vertx, new PgConnectOptions(options)).onComplete(ctx.asyncAssertSuccess(conn -> {
      ctx.assertFalse(conn.isSSL());
      async.complete();
    }));
  }

  @Test
  public void testSslModePrefer(TestContext ctx) {
    Async async = ctx.async();
    PgConnectOptions options = rule.options()
      .setSslMode(SslMode.PREFER)
      .setTrustAll(true);
    PgConnection.connect(vertx, new PgConnectOptions(options)).onComplete(ctx.asyncAssertSuccess(conn -> {
      ctx.assertTrue(conn.isSSL());
      async.complete();
    }));
  }

  @Test
  public void testSslModeVerifyCaConf(TestContext ctx) {
    PgConnectOptions options = rule.options()
      .setSslMode(SslMode.VERIFY_CA)
      .setTrustAll(true);
    PgConnection.connect(vertx, new PgConnectOptions(options)).onComplete(ctx.asyncAssertFailure(error -> {
      ctx.assertEquals("Trust options must be specified under verify-full or verify-ca sslmode", error.getMessage());
    }));
  }

  @Test
  public void testSslModeVerifyFullConf(TestContext ctx) {
    PgConnectOptions options = rule.options()
      .setSslMode(SslMode.VERIFY_FULL);
    PgConnection.connect(vertx, new PgConnectOptions(options)).onComplete(ctx.asyncAssertFailure(error -> {
      ctx.assertEquals("Host verification algorithm must be specified under verify-full sslmode", error.getMessage());
    }));
  }
}
