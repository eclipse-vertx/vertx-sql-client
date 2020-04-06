/*
 * Copyright (c) 2011-2019 Contributors to the Eclipse Foundation
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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class MySQLConnectionTest extends MySQLTestBase {
  Vertx vertx;
  MySQLConnectOptions options;

  @Before
  public void setup() {
    vertx = Vertx.vertx();
    options = new MySQLConnectOptions(MySQLTestBase.options);
  }

  @After
  public void tearDown(TestContext ctx) {
    vertx.close(ctx.asyncAssertSuccess());
  }

  @Test
  public void testAuthenticationWithEncryptPasswordByServerPublicKey(TestContext ctx) {
    options.setServerRsaPublicKeyValue(Buffer.buffer("-----BEGIN PUBLIC KEY-----\n" +
      "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA3yvG5s0qrV7jxVlp0sMj\n" +
      "xP0a6BuLKCMjb0o88hDsJ3xz7PpHNKazuEAfPxiRFVAV3edqfSiXoQw+lJf4haEG\n" +
      "HQe12Nfhs+UhcAeTKXRlZP/JNmI+BGoBduQ1rCId9bKYbXn4pvyS/a1ft7SwFkhx\n" +
      "aogCur7iIB0WUWvwkQ0fEj/Mlhw93lLVyx7hcGFq4FOAKFYr3A0xrHP1IdgnD8QZ\n" +
      "0fUbgGLWWLOossKrbUP5HWko1ghLPIbfmU6o890oj1ZWQewj1Rs9Er92/UDj/JXx\n" +
      "7ha1P+ZOgPBlV037KDQMS6cUh9vTablEHsMLhDZanymXzzjBkL+wH/b9cdL16LkQ\n" +
      "5QIDAQAB\n" +
      "-----END PUBLIC KEY-----\n"));
    MySQLConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.close();
    }));
  }

  @Test
  public void testAuthenticationWithEncryptPasswordByServerPublicKeyInPath(TestContext ctx) {
    options.setServerRsaPublicKeyPath("tls/files/public_key.pem");
    MySQLConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.close();
    }));
  }

  @Test
  public void testConnectWithEmptyPassword(TestContext ctx) {
    options.setUser("emptypassuser")
      .setPassword("")
      .setDatabase("emptyschema");
    MySQLConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.close();
    }));
  }
}
