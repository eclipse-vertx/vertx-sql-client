/*
 * Copyright (c) 2011-2021 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.mssqlclient;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.PemTrustOptions;
import io.vertx.ext.unit.TestContext;
import io.vertx.mssqlclient.junit.MSSQLRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.net.ssl.SSLHandshakeException;

public abstract class MSSQLEncryptionTestBase {

  protected Vertx vertx;
  private MSSQLConnectOptions options;
  private MSSQLConnection connection;

  protected abstract MSSQLRule rule();

  protected void setOptions(MSSQLConnectOptions options) {
    this.options = options;
  }

  @Before
  public void setup() {
    vertx = Vertx.vertx();
  }

  @After
  public void tearDown(TestContext ctx) {
    if (connection != null) {
      connection.close(ctx.asyncAssertSuccess());
    }
    vertx.close(ctx.asyncAssertSuccess());
  }

  protected void connect(Handler<AsyncResult<MSSQLConnection>> handler) {
    MSSQLConnection.connect(vertx, options)
      .onSuccess(conn -> connection = conn)
      .onComplete(handler);
  }

  protected void asyncAssertConnectionEncrypted(TestContext ctx) {
    asyncAssertConnectionEncrypted(ctx, true);
  }

  protected void asyncAssertConnectionUnencrypted(TestContext ctx) {
    asyncAssertConnectionEncrypted(ctx, false);
  }

  private void asyncAssertConnectionEncrypted(TestContext ctx, boolean expected) {
    connect(ctx.asyncAssertSuccess(conn -> {
      conn.query("SELECT encrypt_option FROM sys.dm_exec_connections WHERE session_id = @@SPID")
        .execute(ctx.asyncAssertSuccess(rows -> {
          ctx.assertEquals(1, rows.size());
          ctx.assertEquals(expected, Boolean.parseBoolean(rows.iterator().next().getString("encrypt_option")));
        }));
    }));
  }

  @Test
  public void testHostnameValidationFails(TestContext ctx) {
    // If the client requires SSL
    // Hostname validation must be performed
    setOptions(rule().options()
      .setSsl(true));
    connect(ctx.asyncAssertFailure(t -> {
      ctx.assertTrue(t instanceof SSLHandshakeException);
    }));
  }

  @Test
  public void testTrustAll(TestContext ctx) {
    setOptions(rule().options()
      .setSsl(true)
      .setTrustAll(true));
    asyncAssertConnectionEncrypted(ctx);
  }

  @Test
  public void testTrustOptions(TestContext ctx) {
    Buffer certValue = vertx.fileSystem().readFileBlocking("mssql.pem");
    setOptions(rule().options()
      .setSsl(true)
      .setPemTrustOptions(new PemTrustOptions().addCertValue(certValue)));
    asyncAssertConnectionEncrypted(ctx);
  }
}
