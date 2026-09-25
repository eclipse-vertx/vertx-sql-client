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
import io.vertx.sqlclient.ProxyServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;

public class PgArrayDimensionOverflowTest extends PgTestBase {

  private Vertx vertx;

  @Before
  public void setup() throws Exception {
    super.setup();
    vertx = Vertx.vertx();
  }

  @After
  public void tearDown(TestContext ctx) {
    vertx.close().onComplete(ctx.asyncAssertSuccess());
  }

  @Test
  public void testHugeArrayDimensionIsRejected(TestContext ctx) {
    Async async = ctx.async();
    AtomicBoolean intercepting = new AtomicBoolean(false);
    ProxyServer proxy = ProxyServer.create(vertx, options.getPort(), options.getHost());
    proxy.proxyHandler(conn -> {
      conn.serverHandler(buff -> {
        if (!intercepting.get()) {
          conn.clientSocket().write(buff);
        } else {
          conn.clientSocket().write(craftMaliciousResponse());
          conn.serverSocket().close();
        }
      });
      conn.connect();
    });
    int proxyPort = 18932;
    proxy.listen(proxyPort, "localhost", ctx.asyncAssertSuccess(v -> {
      PgConnectOptions proxyOptions = new PgConnectOptions(options)
        .setHost("localhost")
        .setPort(proxyPort);
      PgConnection.connect(vertx, proxyOptions).onComplete(ctx.asyncAssertSuccess(conn -> {
        conn.closeHandler(v2 -> async.complete());
        intercepting.set(true);
        conn.query("SELECT ARRAY[1,2,3]").execute().onComplete(ctx.asyncAssertFailure());
      }));
    }));
  }

  @Test
  public void testHugeArrayElementLengthIsRejected(TestContext ctx) {
    Async async = ctx.async();
    AtomicBoolean intercepting = new AtomicBoolean(false);
    ProxyServer proxy = ProxyServer.create(vertx, options.getPort(), options.getHost());
    proxy.proxyHandler(conn -> {
      conn.serverHandler(buff -> {
        if (!intercepting.get()) {
          conn.clientSocket().write(buff);
        } else {
          conn.clientSocket().write(craftMaliciousElementLengthResponse());
          conn.serverSocket().close();
        }
      });
      conn.connect();
    });
    int proxyPort = 18933;
    proxy.listen(proxyPort, "localhost", ctx.asyncAssertSuccess(v -> {
      PgConnectOptions proxyOptions = new PgConnectOptions(options)
        .setHost("localhost")
        .setPort(proxyPort);
      PgConnection.connect(vertx, proxyOptions).onComplete(ctx.asyncAssertSuccess(conn -> {
        conn.closeHandler(v2 -> async.complete());
        intercepting.set(true);
        conn.query("SELECT ARRAY[1]").execute().onComplete(ctx.asyncAssertFailure(err -> {
          err.printStackTrace();
        }));
      }));
    }));
  }

  private Buffer craftMaliciousResponse() {
    Buffer resp = Buffer.buffer();

    // RowDescription message (type 0x54)
    resp.appendByte((byte) 0x54);
    resp.appendInt(26); // length (4 + 2 + 20)
    resp.appendShort((short) 1); // field count
    // field descriptor
    resp.appendByte((byte) 'a');
    resp.appendByte((byte) 0); // null-terminated name
    resp.appendInt(0); // table OID
    resp.appendShort((short) 0); // column number
    resp.appendInt(1007); // type OID (int4[])
    resp.appendShort((short) -1); // type size
    resp.appendInt(-1); // type modifier
    resp.appendShort((short) 1); // format code (binary)

    // DataRow message (type 0x44)
    resp.appendByte((byte) 0x44);
    resp.appendInt(30); // length (4 + 2 + 4 + 20)
    resp.appendShort((short) 1); // column count
    resp.appendInt(20); // column data length (array header only)
    // Array header (20 bytes)
    resp.appendInt(1); // ndim
    resp.appendInt(0); // flags
    resp.appendInt(23); // elemtype (int4)
    resp.appendInt(0x7FFFFFFF); // dim length (huge)
    resp.appendInt(1); // lower bound

    return resp;
  }

  private Buffer craftMaliciousElementLengthResponse() {
    Buffer resp = Buffer.buffer();

    // RowDescription message (type 0x54)
    resp.appendByte((byte) 0x54);
    resp.appendInt(26);
    resp.appendShort((short) 1);
    resp.appendByte((byte) 'a');
    resp.appendByte((byte) 0);
    resp.appendInt(0);
    resp.appendShort((short) 0);
    resp.appendInt(1007);
    resp.appendShort((short) -1);
    resp.appendInt(-1);
    resp.appendShort((short) 1);

    // DataRow: 1 element with huge element length
    resp.appendByte((byte) 0x44);
    resp.appendInt(34); // length (4 + 2 + 4 + 24)
    resp.appendShort((short) 1); // column count
    resp.appendInt(24); // column data length (header + 1 element length prefix)
    // Array header
    resp.appendInt(1); // ndim
    resp.appendInt(0); // flags
    resp.appendInt(23); // elemtype (int4)
    resp.appendInt(1); // dim length (1 element)
    resp.appendInt(1); // lower bound
    // Element with huge length
    resp.appendInt(0x7FFFFFFF); // element data length (huge)

    return resp;
  }
}
