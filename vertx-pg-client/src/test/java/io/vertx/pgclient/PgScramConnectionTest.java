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
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.pgclient.junit.ContainerPgRule;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assume.assumeTrue;

@RunWith(VertxUnitRunner.class)
public class PgScramConnectionTest {

  @ClassRule
  public static final ContainerPgRule rule = ContainerPgRule.SHARED_INSTANCE;

  private Vertx vertx;

  private PgConnectOptions options;

  @Before
  public void setup() throws Exception {
    vertx = Vertx.vertx();
    options = rule.options();
  }

  private PgConnectOptions options() {
    return new PgConnectOptions(options);
  }

  @Test
  public void testSaslConnection(TestContext ctx) throws InterruptedException {
    assumeTrue(ContainerPgRule.isAtLeastPg10());
    Async async = ctx.async();
    PgConnectOptions options = new PgConnectOptions(options());
    options.setUser("saslscram");
    options.setPassword("saslscrampwd");

    PgConnection.connect(vertx, options,
        ctx.asyncAssertSuccess(ar -> {
          ctx.assertNotNull(ar);
          async.complete();
        })
    );
  }

  @After
  public void teardown(TestContext ctx) {
    vertx.close(ctx.asyncAssertSuccess());
  }

}
