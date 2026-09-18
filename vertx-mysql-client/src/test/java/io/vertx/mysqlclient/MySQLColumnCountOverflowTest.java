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

package io.vertx.mysqlclient;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.sqlclient.ProxyServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicBoolean;

@RunWith(VertxUnitRunner.class)
public class MySQLColumnCountOverflowTest extends MySQLTestBase {

  Vertx vertx;

  @Before
  public void setup() {
    vertx = Vertx.vertx();
  }

  @After
  public void teardown(TestContext ctx) {
    vertx.close().onComplete(ctx.asyncAssertSuccess());
  }

  @Test
  public void testHugeColumnCountIsRejected(TestContext ctx) {
    Async async = ctx.async(2);
    int proxyPort = 18321;
    AtomicBoolean intercepting = new AtomicBoolean(false);

    ProxyServer proxy = ProxyServer.create(vertx, options.getPort(), options.getHost());
    proxy.proxyHandler(conn -> {
      conn.serverHandler(buff -> {
        if (intercepting.get()) {
          Buffer fakePacket = Buffer.buffer(new byte[]{
            0x04, 0x00, 0x00, 0x01,
            (byte) 0xFD,
            (byte) 0xA0, (byte) 0x86, 0x01
          });
          conn.clientSocket().write(fakePacket);
          conn.serverSocket().close();
          intercepting.set(false);
        } else {
          conn.clientSocket().write(buff);
        }
      });
      conn.connect();
    });

    proxy.listen(proxyPort, "localhost", ctx.asyncAssertSuccess(v -> {
      MySQLConnectOptions proxyOptions = new MySQLConnectOptions(options)
        .setHost("localhost")
        .setPort(proxyPort);
      MySQLConnection.connect(vertx, proxyOptions).onComplete(ctx.asyncAssertSuccess(conn -> {
        conn.closeHandler(v2 -> async.countDown());
        intercepting.set(true);
        conn.query("SELECT 1").execute().onComplete(ctx.asyncAssertFailure(err -> {
          ctx.assertTrue(err.getMessage().contains("Invalid column count"),
            "Expected error about invalid column count but got: " + err.getMessage());
          async.countDown();
        }));
      }));
    }));
  }
}
