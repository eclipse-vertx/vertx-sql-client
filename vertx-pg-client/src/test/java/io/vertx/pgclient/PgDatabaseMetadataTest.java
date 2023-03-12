/*
 * Copyright (c) 2020 Contributors to the Eclipse Foundation
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
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(VertxUnitRunner.class)
public class PgDatabaseMetadataTest {

  private Vertx vertx;

  @Before
  public void setUp() {
    vertx = Vertx.vertx();
  }

  @After
  public void tearDown(TestContext ctx) {
    vertx.close().onComplete(ctx.asyncAssertSuccess());
  }

  @Test
  public void test1(TestContext ctx) throws Exception {
    test(ctx, "9.6.18", "9.6.18", 9, 6);
  }

  @Test
  public void test2(TestContext ctx) throws Exception {
    test(ctx, "12.3", "12.3 (Debian 12.3-1.pgdg100+1)", 12, 3);
  }

  @Test
  public void test3(TestContext ctx) throws Exception {
    test(ctx, "13-beta2", "13beta2 (Debian 13~beta2-1.pgdg100+1)", 13, 0);
  }

  private void test(TestContext ctx,
                    String containerVersion,
                    String expectedFull, int expectedMajor,
                    int expectedMinor) throws Exception {
    ContainerPgRule rule = new ContainerPgRule();
    Async async = ctx.async();
    try {
      PgConnectOptions options = rule.startServer(containerVersion);
      PgConnection.connect(vertx, options).onComplete(ctx.asyncAssertSuccess(conn -> {
        ctx.assertEquals(expectedFull, conn.databaseMetadata().fullVersion());
        ctx.assertEquals(expectedMajor, conn.databaseMetadata().majorVersion());
        ctx.assertEquals(expectedMinor, conn.databaseMetadata().minorVersion());
        async.complete();
      }));
      async.await(20_000);
    } finally {
      rule.stopServer();
    }
  }
}
