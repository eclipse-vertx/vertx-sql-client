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

import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.mssqlclient.junit.MSSQLRule;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Encryption tests for a server that forces encryption.
 */
@RunWith(VertxUnitRunner.class)
public class MSSQLForcedEncryptionTest extends MSSQLEncryptionTestBase {

  @ClassRule
  public static MSSQLRule rule = new MSSQLRule(true, true);

  @Override
  protected MSSQLRule rule() {
    return rule;
  }

  @Test
  public void testFullEncryption(TestContext ctx) {
    // The server forces encryption but the client did not request it
    // The connection will be encrypted even after login
    // but the client shall trust all certificates
    setOptions(rule.options());
    asyncAssertConnectionEncrypted(ctx);
  }
}
