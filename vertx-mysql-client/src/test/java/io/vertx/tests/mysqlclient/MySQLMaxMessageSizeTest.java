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
package io.vertx.tests.mysqlclient;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.MySQLConnection;
import io.vertx.tests.mysqlclient.junit.MySQLRule;
import io.vertx.tests.sqlclient.ProxyServer;
import org.junit.*;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicBoolean;

@RunWith(VertxUnitRunner.class)
public class MySQLMaxMessageSizeTest {

  @ClassRule
  public static MySQLRule rule = MySQLRule.SHARED_INSTANCE;

  private static final int MAX_ALLOWED_PACKET = 64 * 1024;
  private static final int FULL_PACKET_PAYLOAD = 0xFFFFFF;

  private Vertx vertx;

  @Before
  public void setUp() {
    vertx = Vertx.vertx();
  }

  @After
  public void tearDown(TestContext ctx) {
    vertx.close().onComplete(ctx.asyncAssertSuccess());
  }

  private static Buffer craftReassemblyFlood() {
    Buffer flood = Buffer.buffer(FULL_PACKET_PAYLOAD + 4 + 100 + 4);
    // First packet: payload length = 0xFFFFFF triggers multi-packet reassembly
    flood.appendUnsignedByte((short) (FULL_PACKET_PAYLOAD & 0xFF));
    flood.appendUnsignedByte((short) ((FULL_PACKET_PAYLOAD >> 8) & 0xFF));
    flood.appendUnsignedByte((short) ((FULL_PACKET_PAYLOAD >> 16) & 0xFF));
    flood.appendByte((byte) 0);
    flood.appendBytes(new byte[FULL_PACKET_PAYLOAD]);
    // Second packet: triggers the cumulative size check in MySQLDecoder
    int smallPayload = 100;
    flood.appendUnsignedByte((short) (smallPayload & 0xFF));
    flood.appendUnsignedByte((short) ((smallPayload >> 8) & 0xFF));
    flood.appendUnsignedByte((short) ((smallPayload >> 16) & 0xFF));
    flood.appendByte((byte) 1);
    flood.appendBytes(new byte[smallPayload]);
    return flood;
  }

  @Test
  public void testPreLoginAttack(TestContext ctx) {
    Async async = ctx.async();
    MySQLConnectOptions options = rule.options();

    ProxyServer proxy = ProxyServer.create(vertx, options.getPort(), options.getHost());
    proxy.proxyHandler(conn -> {
      conn.serverHandler(buff -> conn.clientSocket().write(craftReassemblyFlood()));
      conn.connect();
    });

    proxy.listen(8080, "localhost", ctx.asyncAssertSuccess(v -> {
      MySQLConnectOptions proxyOptions = new MySQLConnectOptions(options)
        .setHost("localhost")
        .setPort(8080)
        .setMaxAllowedPacket(MAX_ALLOWED_PACKET);

      MySQLConnection.connect(vertx, proxyOptions).onComplete(ctx.asyncAssertFailure(err -> {
        ctx.assertTrue(err.getMessage().contains("exceeds limit"), "Expected exceeds limit error but got: " + err.getMessage());
        async.complete();
      }));
    }));
  }

  @Test
  public void testPostLoginAttack(TestContext ctx) {
    Async async = ctx.async();
    MySQLConnectOptions options = rule.options();
    AtomicBoolean authenticated = new AtomicBoolean(false);

    ProxyServer proxy = ProxyServer.create(vertx, options.getPort(), options.getHost());
    proxy.proxyHandler(conn -> {
      conn.serverHandler(buff -> {
        if (authenticated.get()) {
          conn.clientSocket().write(craftReassemblyFlood());
        } else {
          conn.clientSocket().write(buff);
        }
      });
      conn.connect();
    });

    proxy.listen(8080, "localhost", ctx.asyncAssertSuccess(v -> {
      MySQLConnectOptions proxyOptions = new MySQLConnectOptions(options)
        .setHost("localhost")
        .setPort(8080)
        .setMaxAllowedPacket(MAX_ALLOWED_PACKET);

      MySQLConnection.connect(vertx, proxyOptions).onComplete(ctx.asyncAssertSuccess(conn -> {
        authenticated.set(true);
        conn.query("SELECT 1").execute().onComplete(ctx.asyncAssertFailure(err -> {
          ctx.assertTrue(err.getMessage().contains("exceeds limit"), "Expected exceeds limit error but got: " + err.getMessage());
          async.complete();
        }));
      }));
    }));
  }
}
