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

package io.vertx.tests.mssqlclient;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.dns.AddressResolverOptions;
import io.vertx.core.net.ClientSSLOptions;
import io.vertx.core.net.PemTrustOptions;
import io.vertx.ext.unit.TestContext;
import io.vertx.mssqlclient.MSSQLConnection;
import org.junit.Test;

import javax.net.ssl.SSLHandshakeException;

/**
 * Base class for TDS 7.x encryption tests.
 */
public abstract class MSSQLTds7EncryptionTestBase extends MSSQLEncryptionTestBase {

  @Test
  public void testHostnameValidationFails(TestContext ctx) {
    // Certificate has CN=sql1, but we connect to 'localhost'
    // With hostname verification enabled, this should fail
    Buffer certValue = vertx.fileSystem().readFileBlocking("mssql.pem");
    setOptions(rule().options()
      .setHost("localhost")
      .setSsl(true)
      .setSslOptions(new ClientSSLOptions()
        .setHostnameVerificationAlgorithm("HTTPS")
        .setTrustOptions(new PemTrustOptions().addCertValue(certValue))));
    connect(ctx.asyncAssertFailure(t -> {
      ctx.assertTrue(t instanceof SSLHandshakeException,
        "Expected SSLHandshakeException due to hostname mismatch (localhost != sql1)");
    }));
  }

  @Test
  public void testHostnameValidationSucceeds(TestContext ctx) {
    // Use custom DNS resolution to map sql1 -> 127.0.0.1
    // This allows connecting to 'sql1' (which matches the certificate CN)
    // while actually reaching localhost
    Vertx customVertx = Vertx.vertx(
      new VertxOptions()
        .setAddressResolverOptions(
          new AddressResolverOptions()
            .setHostsValue(Buffer.buffer("127.0.0.1 sql1\n"))
        )
    );

    Buffer certValue = vertx.fileSystem().readFileBlocking("mssql.pem");
    MSSQLConnection.connect(customVertx, rule().options()
        .setHost("sql1")
        .setSsl(true)
        .setSslOptions(new ClientSSLOptions()
          .setHostnameVerificationAlgorithm("HTTPS")
          .setTrustOptions(new PemTrustOptions().addCertValue(certValue))))
      .onComplete(ctx.asyncAssertSuccess(conn -> {
        ctx.assertTrue(conn.isSSL());
        conn.close().onComplete(ctx.asyncAssertSuccess(v -> {
          customVertx.close().onComplete(ctx.asyncAssertSuccess());
        }));
      }));
  }

  @Test
  public void testTrustAll(TestContext ctx) {
    setOptions(rule().options()
      .setSsl(true)
      .setSslOptions(new ClientSSLOptions().setTrustAll(true)));
    asyncAssertConnectionEncrypted(ctx);
  }

  @Test
  public void testTrustOptions(TestContext ctx) {
    Buffer certValue = vertx.fileSystem().readFileBlocking("mssql.pem");
    setOptions(rule().options()
      .setSsl(true)
      .setSslOptions(new ClientSSLOptions().setTrustOptions(new PemTrustOptions().addCertValue(certValue))));
    asyncAssertConnectionEncrypted(ctx);
  }
}
