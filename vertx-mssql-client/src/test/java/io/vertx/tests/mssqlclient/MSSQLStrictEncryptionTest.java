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

import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.ClientSSLOptions;
import io.vertx.core.net.PemTrustOptions;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.mssqlclient.EncryptionMode;
import io.vertx.tests.mssqlclient.junit.MSSQLRule;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;

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
