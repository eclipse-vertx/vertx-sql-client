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
import org.junit.Test;

import javax.net.ssl.SSLHandshakeException;

/**
 * Base class for TDS 7.x encryption tests.
 */
public abstract class MSSQLTds7EncryptionTestBase extends MSSQLEncryptionTestBase {

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
