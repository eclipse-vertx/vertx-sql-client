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

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetSocket;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.mysqlclient.MySQLAuthOptions;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.MySQLConnection;
import io.vertx.mysqlclient.SslMode;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A server that answers with an AuthSwitchRequest, the plugin data that follows the plugin name depends on the plugin:
 * {@code mysql_clear_password} does not use a nonce so the data is empty or ignored, while the other plugins expect a
 * nonce. A real MySQL server does not switch to {@code mysql_clear_password} without an external authentication
 * plugin, hence the fake server.
 */
@RunWith(VertxUnitRunner.class)
public class MySQLAuthSwitchTest {

  private static final int COM_QUIT = 0x01;
  private static final int CLIENT_PROTOCOL_41 = 0x0200;
  private static final int CLIENT_SECURE_CONNECTION = 0x8000;
  private static final int CLIENT_PLUGIN_AUTH = 0x00080000;

  private Vertx vertx;
  private NetServer server;
  private final List<Buffer> clearPasswordResponses = new CopyOnWriteArrayList<>();
  private volatile String switchPlugin = "mysql_clear_password";
  private volatile byte[] switchPluginData = new byte[0];

  @Before
  public void setup(TestContext ctx) {
    vertx = Vertx.vertx();
    server = vertx.createNetServer().connectHandler(this::handleConnection);
    server.listen(0, "localhost").onComplete(ctx.asyncAssertSuccess());
  }

  @After
  public void tearDown(TestContext ctx) {
    vertx.close().onComplete(ctx.asyncAssertSuccess());
  }

  @Test(timeout = 30_000)
  public void testConnect(TestContext ctx) {
    connect().onComplete(ctx.asyncAssertSuccess(conn -> {
      ctx.assertEquals(1, clearPasswordResponses.size());
      assertClearPassword(ctx, clearPasswordResponses.get(0), "secret");
      conn.close().onComplete(ctx.asyncAssertSuccess());
    }));
  }

  @Test(timeout = 30_000)
  public void testConnectWithPluginData(TestContext ctx) {
    switchPluginData = "some trailing plugin data".getBytes(StandardCharsets.UTF_8);
    connect().onComplete(ctx.asyncAssertSuccess(conn -> {
      ctx.assertEquals(1, clearPasswordResponses.size());
      assertClearPassword(ctx, clearPasswordResponses.get(0), "secret");
      conn.close().onComplete(ctx.asyncAssertSuccess());
    }));
  }

  @Test(timeout = 30_000)
  public void testChangeUser(TestContext ctx) {
    connect().onComplete(ctx.asyncAssertSuccess(conn -> {
      conn.changeUser(otherUser()).onComplete(ctx.asyncAssertSuccess(v -> {
        ctx.assertEquals(2, clearPasswordResponses.size());
        assertClearPassword(ctx, clearPasswordResponses.get(1), "other-secret");
        conn.close().onComplete(ctx.asyncAssertSuccess());
      }));
    }));
  }

  @Test(timeout = 30_000)
  public void testChangeUserWithPluginData(TestContext ctx) {
    switchPluginData = "some trailing plugin data".getBytes(StandardCharsets.UTF_8);
    connect().onComplete(ctx.asyncAssertSuccess(conn -> {
      conn.changeUser(otherUser()).onComplete(ctx.asyncAssertSuccess(v -> {
        ctx.assertEquals(2, clearPasswordResponses.size());
        assertClearPassword(ctx, clearPasswordResponses.get(1), "other-secret");
        conn.close().onComplete(ctx.asyncAssertSuccess());
      }));
    }));
  }

  @Test(timeout = 30_000)
  public void testConnectWithShortNonce(TestContext ctx) {
    switchPlugin = "mysql_native_password";
    switchPluginData = new byte[5];
    connect().onComplete(ctx.asyncAssertFailure(failure -> assertShortNonce(ctx, failure, 5)));
  }

  @Test(timeout = 30_000)
  public void testChangeUserWithShortNonce(TestContext ctx) {
    connect().onComplete(ctx.asyncAssertSuccess(conn -> {
      switchPlugin = "caching_sha2_password";
      switchPluginData = new byte[7];
      conn.changeUser(otherUser()).onComplete(ctx.asyncAssertFailure(failure -> assertShortNonce(ctx, failure, 7)));
    }));
  }

  private MySQLAuthOptions otherUser() {
    return new MySQLAuthOptions().setUser("other").setPassword("other-secret");
  }

  private Future<MySQLConnection> connect() {
    MySQLConnectOptions options = new MySQLConnectOptions()
      .setHost("localhost")
      .setPort(server.actualPort())
      .setUser("user")
      .setPassword("secret")
      .setDatabase("db")
      .setSslMode(SslMode.DISABLED);
    return MySQLConnection.connect(vertx, options);
  }

  private void assertClearPassword(TestContext ctx, Buffer payload, String password) {
    // the clear password followed by a 0x00 byte
    ctx.assertEquals(Buffer.buffer(password.getBytes(StandardCharsets.UTF_8)).appendByte((byte) 0), payload);
  }

  private void assertShortNonce(TestContext ctx, Throwable failure, int received) {
    ctx.assertTrue(failure.getMessage().contains("expected 20 bytes of plugin data but received " + received), failure.getMessage());
  }

  private void handleConnection(NetSocket so) {
    so.write(handshake());
    // the server first waits for a client request (the handshake response or COM_CHANGE_USER), answers it with an auth
    // switch request, then waits for the response to the auth switch request
    boolean[] awaitingAuthSwitchResponse = {false};
    Buffer[] pending = {Buffer.buffer()};
    so.handler(chunk -> {
      pending[0].appendBuffer(chunk);
      while (pending[0].length() >= 4) {
        int length = pending[0].getUnsignedMediumLE(0);
        if (pending[0].length() < 4 + length) {
          return;
        }
        int sequenceId = pending[0].getUnsignedByte(3);
        Buffer payload = pending[0].getBuffer(4, 4 + length);
        pending[0] = pending[0].getBuffer(4 + length, pending[0].length());
        if (length == 1 && payload.getUnsignedByte(0) == COM_QUIT) {
          so.close();
          return;
        }
        if (awaitingAuthSwitchResponse[0]) {
          awaitingAuthSwitchResponse[0] = false;
          clearPasswordResponses.add(payload);
          so.write(packet(sequenceId + 1, Buffer.buffer().appendByte((byte) 0).appendBytes(new byte[]{0, 0, 2, 0, 0, 0})));
        } else {
          awaitingAuthSwitchResponse[0] = true;
          so.write(authSwitchRequest(sequenceId + 1));
        }
      }
    });
  }

  private static Buffer handshake() {
    Buffer payload = Buffer.buffer();
    payload.appendByte((byte) 10); // protocol version
    payload.appendString("8.0.30").appendByte((byte) 0); // server version
    payload.appendIntLE(1); // connection id
    payload.appendBytes(new byte[8]); // auth plugin data, part 1
    payload.appendByte((byte) 0); // filler
    int capabilities = CLIENT_PROTOCOL_41 | CLIENT_SECURE_CONNECTION | CLIENT_PLUGIN_AUTH;
    payload.appendUnsignedShortLE(capabilities & 0xFFFF);
    payload.appendByte((byte) 45); // character set
    payload.appendUnsignedShortLE(2); // status flags
    payload.appendUnsignedShortLE(capabilities >>> 16);
    payload.appendByte((byte) 21); // length of the auth plugin data
    payload.appendBytes(new byte[10]); // reserved
    payload.appendBytes(new byte[12]); // auth plugin data, part 2
    payload.appendByte((byte) 0); // reserved
    payload.appendString("mysql_native_password").appendByte((byte) 0);
    return packet(0, payload);
  }

  private Buffer authSwitchRequest(int sequenceId) {
    Buffer payload = Buffer.buffer();
    payload.appendByte((byte) 0xFE);
    payload.appendString(switchPlugin).appendByte((byte) 0);
    payload.appendBytes(switchPluginData);
    return packet(sequenceId, payload);
  }

  private static Buffer packet(int sequenceId, Buffer payload) {
    return Buffer.buffer().appendMediumLE(payload.length()).appendByte((byte) sequenceId).appendBuffer(payload);
  }
}
