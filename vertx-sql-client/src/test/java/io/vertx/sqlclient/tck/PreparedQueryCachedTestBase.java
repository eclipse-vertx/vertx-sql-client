/*
 * Copyright (c) 2011-2020 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.sqlclient.tck;

import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.sqlclient.Tuple;
import org.junit.Test;

public abstract class PreparedQueryCachedTestBase extends PreparedQueryTestBase {
  @Override
  public void setUp(TestContext ctx) throws Exception {
    super.setUp(ctx);
    options.setCachePreparedStatements(true);
  }

  @Test
  public void testConcurrent(TestContext ctx) {
    connector.connect(ctx.asyncAssertSuccess(conn -> {
      Async[] asyncs = new Async[10];
      for (int i = 0; i < 10; i++) {
        asyncs[i] = ctx.async();
      }
      for (int i = 0; i < 10; i++) {
        Async async = asyncs[i];
        conn.prepare(statement("SELECT * FROM Fortune WHERE id=", ""), ctx.asyncAssertSuccess(ps -> {
          ps.query().execute(Tuple.of(1), ctx.asyncAssertSuccess(results -> {
            ctx.assertEquals(1, results.size());
            Tuple row = results.iterator().next();
            ctx.assertEquals(1, row.getInteger(0));
            ctx.assertEquals("fortune: No such file or directory", row.getString(1));
            async.complete();
          }));
        }));
      }
    }));
  }
}
