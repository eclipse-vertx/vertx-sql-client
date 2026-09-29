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

package io.vertx.pgclient;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.pgclient.junit.ContainerPgRule;
import io.vertx.sqlclient.ProxyServer;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@RunWith(VertxUnitRunner.class)
public class PgMaxMessageSizeTest {

  static final int MAX_MESSAGE_LENGTH = 0x3FFFFFFF;
  static final int MAX_SMALL_MESSAGE_LENGTH = 10_000;
  static final int MAX_PRE_AUTH_MESSAGE_LENGTH = 30_000;

  @ClassRule
  public static final ContainerPgRule rule = ContainerPgRule.SHARED_INSTANCE;

  private Vertx vertx;
  private PgConnectOptions options;
  private final AtomicInteger proxyPort = new AtomicInteger(18080);

  @Before
  public void setUp() {
    vertx = Vertx.vertx();
    options = rule.options();
  }

  @After
  public void tearDown(TestContext ctx) {
    vertx.close().onComplete(ctx.asyncAssertSuccess());
  }

  private static Buffer craftSpoofedPgMessage(byte type, int declaredLength) {
    Buffer buf = Buffer.buffer();
    buf.appendByte(type);
    buf.appendInt(declaredLength);
    buf.appendBytes(new byte[1024]);
    return buf;
  }

  @Test
  public void testDataRowExceedsMaxMessageLength(TestContext ctx) {
    Async async = ctx.async();
    int port = proxyPort.getAndIncrement();
    ProxyServer proxy = ProxyServer.create(vertx, options.getPort(), options.getHost());
    proxy.proxyHandler(conn -> {
      conn.serverHandler(buff -> {
        conn.clientSocket().write(craftSpoofedPgMessage((byte) 'D', MAX_MESSAGE_LENGTH + 1));
      });
      conn.connect();
    });
    proxy.listen(port, "localhost", ctx.asyncAssertSuccess(v -> {
      PgConnectOptions proxyOptions = new PgConnectOptions(options)
        .setPort(port)
        .setHost("localhost");
      PgConnection.connect(vertx, proxyOptions)
        .onComplete(ctx.asyncAssertFailure(err -> {
          ctx.assertTrue(err.getMessage().contains("exceeds maximum allowed " + MAX_MESSAGE_LENGTH),
            "Expected error about max message length, got: " + err.getMessage());
          async.complete();
        }));
    }));
  }

  @Test
  public void testControlMessageExceedsSmallLimit(TestContext ctx) {
    Async async = ctx.async();
    int port = proxyPort.getAndIncrement();
    AtomicBoolean authenticated = new AtomicBoolean(false);
    ProxyServer proxy = ProxyServer.create(vertx, options.getPort(), options.getHost());
    proxy.proxyHandler(conn -> {
      conn.serverHandler(buff -> {
        if (authenticated.get()) {
          conn.clientSocket().write(craftSpoofedPgMessage((byte) 'Z', MAX_SMALL_MESSAGE_LENGTH + 1));
        } else {
          conn.clientSocket().write(buff);
        }
      });
      conn.clientHandler(buff -> {
        conn.serverSocket().write(buff);
      });
      conn.connect();
    });
    proxy.listen(port, "localhost", ctx.asyncAssertSuccess(v -> {
      PgConnectOptions proxyOptions = new PgConnectOptions(options)
        .setPort(port)
        .setHost("localhost");
      PgConnection.connect(vertx, proxyOptions)
        .onComplete(ctx.asyncAssertSuccess(pgConn -> {
          authenticated.set(true);
          pgConn.query("SELECT 1").execute()
            .onComplete(ctx.asyncAssertFailure(err -> {
              ctx.assertTrue(err.getMessage().contains("exceeds maximum allowed " + MAX_SMALL_MESSAGE_LENGTH),
                "Expected error about small message length, got: " + err.getMessage());
              async.complete();
            }));
        }));
    }));
  }

  @Test
  public void testAuthMessageExceedsPreAuthLimit(TestContext ctx) {
    Async async = ctx.async();
    int port = proxyPort.getAndIncrement();
    ProxyServer proxy = ProxyServer.create(vertx, options.getPort(), options.getHost());
    proxy.proxyHandler(conn -> {
      conn.serverHandler(buff -> {
        conn.clientSocket().write(craftSpoofedPgMessage((byte) 'R', MAX_PRE_AUTH_MESSAGE_LENGTH + 1));
      });
      conn.connect();
    });
    proxy.listen(port, "localhost", ctx.asyncAssertSuccess(v -> {
      PgConnectOptions proxyOptions = new PgConnectOptions(options)
        .setPort(port)
        .setHost("localhost");
      PgConnection.connect(vertx, proxyOptions)
        .onComplete(ctx.asyncAssertFailure(err -> {
          ctx.assertTrue(err.getMessage().contains("exceeds maximum allowed " + MAX_PRE_AUTH_MESSAGE_LENGTH),
            "Expected error about pre-auth message length, got: " + err.getMessage());
          async.complete();
        }));
    }));
  }

  @Test
  public void testValidQueryNotRejected(TestContext ctx) {
    Async async = ctx.async();
    int port = proxyPort.getAndIncrement();
    ProxyServer proxy = ProxyServer.create(vertx, options.getPort(), options.getHost());
    proxy.proxyHandler(conn -> {
      conn.connect();
    });
    proxy.listen(port, "localhost", ctx.asyncAssertSuccess(v -> {
      PgConnectOptions proxyOptions = new PgConnectOptions(options)
        .setPort(port)
        .setHost("localhost");
      PgConnection.connect(vertx, proxyOptions)
        .onComplete(ctx.asyncAssertSuccess(pgConn -> {
          pgConn.query("SELECT 1").execute()
            .onComplete(ctx.asyncAssertSuccess(rs -> {
              ctx.assertEquals(1, rs.size());
              pgConn.close();
              async.complete();
            }));
        }));
    }));
  }
}
