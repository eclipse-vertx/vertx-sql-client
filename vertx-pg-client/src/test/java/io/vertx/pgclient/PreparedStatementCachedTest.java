/*
 * Copyright (C) 2017 Julien Viet
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.vertx.pgclient;

import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;
import org.junit.Ignore;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

public class PreparedStatementCachedTest extends PreparedStatementTestBase {

  @Override
  protected PgConnectOptions options() {
    return new PgConnectOptions(options).setCachePreparedStatements(true);
  }

  // Error seems to be different for some implementations
  @Test
  public void testOneShotPreparedQueryCacheRefreshOnTableSchemaChange(TestContext ctx) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options()).onComplete(ctx.asyncAssertSuccess(conn -> {
      conn
        .preparedQuery("SELECT * FROM unstable WHERE id=$1")
        .execute(Tuple.of(1))
        .onComplete(ctx.asyncAssertSuccess(res1 -> {
        ctx.assertEquals(1, res1.size());
        Tuple row1 = res1.iterator().next();
        ctx.assertEquals(1, row1.getInteger(0));
        ctx.assertEquals("fortune: No such file or directory", row1.getString(1));

        // change table schema
        conn
          .query("ALTER TABLE unstable DROP COLUMN message")
          .execute()
          .onComplete(ctx.asyncAssertSuccess(dropColumn -> {
          // failure due to schema change
          conn
            .preparedQuery("SELECT * FROM unstable WHERE id=$1")
            .execute(Tuple.of(1))
            .onComplete(ctx.asyncAssertFailure(failure -> {
            // recover because the cache is refreshed
            conn
              .preparedQuery("SELECT * FROM unstable WHERE id=$1")
              .execute(Tuple.of(1))
              .onComplete(ctx.asyncAssertSuccess(res2 -> {
              ctx.assertEquals(1, res2.size());
              Tuple row2 = res2.iterator().next();
              ctx.assertEquals(1, row2.getInteger(0));
              ctx.assertEquals(null, row2.getString(1)); // the message column is removed
              conn.close();
              async.complete();
            }));
          }));
        }));
      }));
    }));
  }

  @Test
  public void testMaxPreparedStatementEviction(TestContext ctx) {
    testPreparedStatements(ctx, options().setCachePreparedStatements(true).setPreparedStatementCacheMaxSize(16), 128, 16);
  }

  @Test
  public void testOneShotPreparedStatements(TestContext ctx) {
    testPreparedStatements(ctx, options().setCachePreparedStatements(false), 128, 0);
  }

  @Test
  public void testPreparedStatementCacheFiltering(TestContext ctx) {
    AtomicInteger count = new AtomicInteger();
    testPreparedStatements(ctx, options()
      .setCachePreparedStatements(true)
      .setPreparedStatementCacheSqlFilter(sql -> count.getAndIncrement() % 2 == 0), 128, 64);
  }

  private void testPreparedStatements(TestContext ctx, PgConnectOptions options, int num, int expected) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options).onComplete(ctx.asyncAssertSuccess(conn -> {
      conn
        .query("SELECT * FROM pg_prepared_statements")
        .execute()
        .onComplete(ctx.asyncAssertSuccess(res1 -> {
        ctx.assertEquals(0, res1.size());
        AtomicInteger count = new AtomicInteger(num);
        for (int i = 0;i < num;i++) {
          int val = i;
          conn
            .preparedQuery("SELECT " + i)
            .execute(Tuple.tuple())
            .onComplete(ctx.asyncAssertSuccess(res2 -> {
            ctx.assertEquals(1, res2.size());
            ctx.assertEquals(val, res2.iterator().next().getInteger(0));
            if (count.decrementAndGet() == 0) {
              ctx.assertEquals(num - 1, val);
              conn
                .query("SELECT * FROM pg_prepared_statements")
                .execute()
                .onComplete(ctx.asyncAssertSuccess(res3 -> {
                ctx.assertEquals(expected, res3.size());
                conn.close().onComplete(ctx.asyncAssertSuccess(v -> {
                  async.complete();
                }));
              }));
            }
          }));
        }
      }));
    }));
  }
}
