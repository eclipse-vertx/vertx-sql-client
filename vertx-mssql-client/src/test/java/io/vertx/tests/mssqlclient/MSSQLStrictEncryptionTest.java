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
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.mssqlclient.EncryptionMode;
import io.vertx.mssqlclient.MSSQLConnection;
import io.vertx.tests.mssqlclient.junit.MSSQLRule;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.net.ssl.SSLHandshakeException;

import static io.vertx.tests.mssqlclient.junit.MSSQLRule.Config.STRICT_ENCRYPTION;

/**
 * Encryption tests for TDS 8.0 with strict encryption.
 * <p>
 * SQL Server 2022+ with "Force Strict Encryption" enabled requires TDS 8.0 protocol with TLS established before PRELOGIN (encrypt=strict).
 */
@RunWith(VertxUnitRunner.class)
public class MSSQLStrictEncryptionTest extends MSSQLEncryptionTestBase {

  @ClassRule
  public static MSSQLRule rule = new MSSQLRule(STRICT_ENCRYPTION, true);

  @Override
  protected MSSQLRule rule() {
    return rule;
  }

  @Test
  public void testEncryptionWithTrustAllNotAllowed(TestContext ctx) {
    // TDS 8.0 with strict encryption requires proper certificate validation
    setOptions(rule.options()
      .setEncryptionMode(EncryptionMode.STRICT)
      .setSslOptions(new ClientSSLOptions().setTrustAll(true)));
    connect(ctx.asyncAssertFailure(t -> {
      ctx.assertTrue(t.getMessage().contains("Strict encryption mode requires proper certificate validation."));
    }));
  }

  @Test
  public void testEncryptionWithValidCertificate(TestContext ctx) {
    Buffer certValue = vertx.fileSystem().readFileBlocking("mssql.pem");
    setOptions(rule.options()
      .setEncryptionMode(EncryptionMode.STRICT)
      .setSslOptions(new ClientSSLOptions().setTrustOptions(new PemTrustOptions().addCertValue(certValue))));
    asyncAssertConnectionEncrypted(ctx);
  }

  @Test
  public void testHostnameValidationFailsWithInvalidHostname(TestContext ctx) {
    // Even with valid certificate, wrong hostname should fail
    // Certificate has CN=sql1, but we connect to 'localhost'
    Buffer certValue = vertx.fileSystem().readFileBlocking("mssql.pem");
    setOptions(rule.options()
      .setHost("localhost")
      .setEncryptionMode(EncryptionMode.STRICT)
      .setSslOptions(new ClientSSLOptions()
        .setHostnameVerificationAlgorithm("HTTPS")
        .setTrustOptions(new PemTrustOptions().addCertValue(certValue))));
    connect(ctx.asyncAssertFailure(t -> {
      ctx.assertTrue(t instanceof SSLHandshakeException,
        "Expected SSLHandshakeException due to hostname mismatch (localhost != sql1)");
    }));
  }

  @Test
  public void testHostnameValidationSucceedsWithCorrectHostname(TestContext ctx) {
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
    MSSQLConnection.connect(customVertx, rule.options()
        .setHost("sql1")
        .setEncryptionMode(EncryptionMode.STRICT)
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
  public void testEncryptionRejectsNonStrictClient(TestContext ctx) {
    setOptions(rule.options()
      .setSsl(true)
      .setSslOptions(new ClientSSLOptions().setTrustAll(true)));
    connect(ctx.asyncAssertFailure());
  }

  @Test
  public void testEncryptionRejectsPlainClient(TestContext ctx) {
    setOptions(rule.options());
    connect(ctx.asyncAssertFailure());
  }
}
