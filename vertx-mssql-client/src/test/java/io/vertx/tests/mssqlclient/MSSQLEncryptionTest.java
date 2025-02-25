/*
 * Copyright (c) 2011-2024 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.tests.mssqlclient;

import io.vertx.core.net.ClientSSLOptions;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.tests.mssqlclient.junit.MSSQLRule;
import io.vertx.sqlclient.Tuple;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;

import static io.vertx.tests.mssqlclient.junit.MSSQLRule.Config.TLS;

/**
 * Encryption tests for a server that does not force encryption.
 */
@RunWith(VertxUnitRunner.class)
public class MSSQLEncryptionTest extends MSSQLEncryptionTestBase {

  @ClassRule
  public static MSSQLRule rule = new MSSQLRule(TLS);

  @Override
  protected MSSQLRule rule() {
    return rule;
  }

  @Test
  public void testEncryptionLoginOnly(TestContext ctx) {
    // If the client options are left to defaults
    // the connection is upgraded to SSL before credentials are sent
    // but downgraded after login
    // Also, the client shall trust all certificates
    setOptions(rule.options());
    asyncAssertConnectionUnencrypted(ctx);
  }

  @Test
  public void testSmallerPacketSize(TestContext ctx) {
    setOptions(rule.options()
      .setSsl(true)
      .setSslOptions(new ClientSSLOptions().setTrustAll(true))
      .setPacketSize(512));

    char[] chars = new char[200];
    Arrays.fill(chars, 'a');
    String str = new String(chars);

    connect(ctx.asyncAssertSuccess(conn -> {
      conn.query("CREATE TABLE #TestSmallerPacketSize (text NVARCHAR(MAX))").execute().onComplete(ctx.asyncAssertSuccess(v -> {
        conn.preparedQuery("INSERT INTO #TestSmallerPacketSize (text) VALUES (@p1)").execute(Tuple.of(str)).onComplete(ctx.asyncAssertSuccess());
      }));
    }));
  }
}
