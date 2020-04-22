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

  @Test
  public void testClosedPreparedStatementEvictedFromCache(TestContext ctx) {
    Async async = ctx.async();
    options.setCachePreparedStatements(true);
    options.setPreparedStatementCacheMaxSize(1024);

    connector.connect(ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT * FROM immutable", ctx.asyncAssertSuccess(preparedStatement1 -> {
        preparedStatement1.query().execute(ctx.asyncAssertSuccess(res1 -> {
          ctx.assertEquals(12, res1.size());
          preparedStatement1.close(); // no response from server, we need to wait for some time here
          vertx.setTimer(2000, id -> {
            conn.prepare("SELECT * FROM immutable", ctx.asyncAssertSuccess(preparedStatement2 -> {
              preparedStatement2.query().execute(ctx.asyncAssertSuccess(res2 -> {
                ctx.assertEquals(12, res2.size());
                conn.close();
                async.complete();
              }));
            }));
          });
        }));
      }));
    }));
    async.await();
  }

  @Test
  public void testConcurrentClose(TestContext ctx) {
    connector.connect(ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT * FROM immutable", ctx.asyncAssertSuccess(preparedStatement1 -> {
        preparedStatement1.query().execute(ctx.asyncAssertSuccess(res1 -> {
          ctx.assertEquals(12, res1.size());
          preparedStatement1.close();
          // send another prepare command directly
          conn.prepare("SELECT * FROM immutable", ctx.asyncAssertSuccess(preparedStatement2 -> {
            preparedStatement2.query().execute(ctx.asyncAssertSuccess(res2 -> {
              ctx.assertEquals(12, res2.size());
              conn.close();
            }));
          }));
        }));
      }));
    }));
  }

  @Test
  public void testSqlLimitDoesNotAffectQuery(TestContext ctx) {
    options.setPreparedStatementCacheSqlLimit(1);
    connector.connect(ctx.asyncAssertSuccess(conn -> {
      conn.preparedQuery("SELECT * FROM immutable").execute(ctx.asyncAssertSuccess(res -> {
        ctx.assertEquals(12, res.size());
        conn.close();
      }));
    }));
  }

  @Test
  public void testEvictedStmtClosing(TestContext ctx) {
    options.setCachePreparedStatements(true);
    options.setPreparedStatementCacheMaxSize(1);

    connector.connect(ctx.asyncAssertSuccess(conn -> {
      conn.preparedQuery("SELECT * FROM immutable").execute(ctx.asyncAssertSuccess(res1 -> {
        ctx.assertEquals(12, res1.size());
        conn.preparedQuery("SELECT * FROM mutable").execute(ctx.asyncAssertSuccess(res2 -> {
          // the first stmt should be evicted, query again to check if it's ok
          conn.preparedQuery("SELECT * FROM immutable").execute(ctx.asyncAssertSuccess(res3 -> {
            ctx.assertEquals(12, res1.size());
            conn.close();
          }));
        }));
      }));
    }));
  }
}
