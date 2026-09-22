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

package io.vertx.mssqlclient;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.mssqlclient.junit.MSSQLRule;
import io.vertx.sqlclient.ProxyServer;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicBoolean;

@RunWith(VertxUnitRunner.class)
public class MSSQLMaxMessageSizeTest {

  @ClassRule
  public static MSSQLRule rule = MSSQLRule.SHARED_INSTANCE;

  private static final int MAX_MESSAGE_SIZE = 64 * 1024;

  private Vertx vertx;

  @Before
  public void setup() {
    vertx = Vertx.vertx();
  }

  @After
  public void tearDown(TestContext ctx) {
    vertx.close().onComplete(ctx.asyncAssertSuccess());
  }

  private static Buffer craftTdsPackets(int count, int payloadSize) {
    Buffer flood = Buffer.buffer();
    for (int i = 0; i < count; i++) {
      int totalLength = 8 + payloadSize;
      flood.appendByte((byte) 4); // type: TABULAR_RESULT
      flood.appendByte((byte) 0x00); // status: NORMAL (not END_OF_MESSAGE)
      flood.appendShort((short) totalLength); // length (big-endian)
      flood.appendShort((short) 0); // SPID
      flood.appendByte((byte) 0); // packet ID
      flood.appendByte((byte) 0); // window
      flood.appendBytes(new byte[payloadSize]); // payload
    }
    return flood;
  }

  @Test
  public void testPreLoginAttack(TestContext ctx) {
    Async async = ctx.async();
    MSSQLConnectOptions serverOptions = rule.options();

    ProxyServer proxy = ProxyServer.create(vertx, serverOptions.getPort(), serverOptions.getHost());
    proxy.proxyHandler(conn -> {
      conn.serverHandler(buff -> {
        int payloadSize = 1024;
        int packetsNeeded = (MAX_MESSAGE_SIZE / (8 + payloadSize)) + 10;
        conn.clientSocket().write(craftTdsPackets(packetsNeeded, payloadSize));
      });
      conn.connect();
    });

    proxy.listen(8080, "localhost", ctx.asyncAssertSuccess(v -> {
      MSSQLConnectOptions proxyOptions = new MSSQLConnectOptions(serverOptions)
        .setPort(8080)
        .setHost("localhost")
        .setMaxMessageSize(MAX_MESSAGE_SIZE);
      MSSQLConnection.connect(vertx, proxyOptions).onComplete(ctx.asyncAssertFailure(err -> {
        ctx.assertTrue(err.getMessage().contains("TDS message size exceeds limit"), err.getMessage());
        async.complete();
      }));
    }));
  }

  @Test
  public void testPostLoginAttack(TestContext ctx) {
    Async async = ctx.async();
    MSSQLConnectOptions serverOptions = rule.options();

    AtomicBoolean loginComplete = new AtomicBoolean(false);

    ProxyServer proxy = ProxyServer.create(vertx, serverOptions.getPort(), serverOptions.getHost());
    proxy.proxyHandler(conn -> {
      conn.serverHandler(buff -> {
        conn.clientSocket().write(buff);
      });
      conn.clientHandler(buff -> {
        if (loginComplete.get()) {
          conn.serverHandler(ignored -> {
            // Ignore real server response
          });
          int payloadSize = 1024;
          int packetsNeeded = (MAX_MESSAGE_SIZE / (8 + payloadSize)) + 10;
          conn.clientSocket().write(craftTdsPackets(packetsNeeded, payloadSize));
        } else {
          conn.serverSocket().write(buff);
        }
      });
      conn.connect();
    });

    proxy.listen(8080, "localhost", ctx.asyncAssertSuccess(v -> {
      MSSQLConnectOptions proxyOptions = new MSSQLConnectOptions(serverOptions)
        .setPort(8080)
        .setHost("localhost")
        .setMaxMessageSize(MAX_MESSAGE_SIZE);
      MSSQLConnection.connect(vertx, proxyOptions).onComplete(ctx.asyncAssertSuccess(conn -> {
        loginComplete.set(true);
        conn.query("SELECT 1").execute().onComplete(ctx.asyncAssertFailure(err -> {
          ctx.assertTrue(err.getMessage().contains("TDS message size exceeds limit"), err.getMessage());
          async.complete();
        }));
      }));
    }));
  }
}
