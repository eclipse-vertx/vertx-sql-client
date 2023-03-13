/*
 * Copyright (C) 2020 IBM Corporation
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
 */
package io.vertx.pgclient.tck;

import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgException;
import io.vertx.pgclient.PgPool;
import io.vertx.pgclient.junit.ContainerPgRule;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.tck.TransactionTestBase;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class PgTransactionTest extends TransactionTestBase {

  @ClassRule
  public static ContainerPgRule rule = new ContainerPgRule();

  @Override
  protected Pool createPool() {
    return PgPool.pool(vertx, new PgConnectOptions(rule.options()), new PoolOptions().setMaxSize(1));
  }

  @Override
  protected Pool nonTxPool() {
    return PgPool.pool(vertx, new PgConnectOptions(rule.options()), new PoolOptions().setMaxSize(1));
  }

  @Override
  protected String statement(String... parts) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < parts.length; i++) {
      if (i > 0) {
        sb.append("$").append((i));
      }
      sb.append(parts[i]);
    }
    return sb.toString();
  }

  /**
   * PostgreSQL specific behavior that rolls back the transaction when a query fails.
   */
  @Test
  public void testFailureWithPendingQueries(TestContext ctx) {
    Async async = ctx.async();
    connector.accept(ctx.asyncAssertSuccess(res -> {
      res.client
        .query("SELECT whatever from DOES_NOT_EXIST")
        .execute()
        .onComplete(ctx.asyncAssertFailure(v -> {
      }));
      res.client
        .query("SELECT id, val FROM mutable")
        .execute()
        .onComplete(ctx.asyncAssertFailure(err -> {
        res.tx
          .rollback()
          .onComplete(ctx.asyncAssertSuccess(v -> {
          async.complete();
        }));
      }));
    }));
  }

  @Test
  public void testDeferredConstraintTriggersRollbackOnCommit(TestContext ctx) {
    connector.accept(ctx.asyncAssertSuccess(res -> {
      res.client.query("INSERT INTO deferred_constraints (name, parent) values ('john', 'mike')")
        .execute()
        .compose(ok -> res.tx.commit())
        .onComplete(ctx.asyncAssertFailure(failure -> {
          PgException pgEx = (PgException) failure;
          // foreign key constraint violation
          ctx.assertEquals("23503", pgEx.getSqlState());
        }));
    }));
  }

  @Test
  public void testLongTransaction(TestContext ctx) {
    Async async = ctx.async(2);
    connector.accept(ctx.asyncAssertSuccess(res -> {
      res.client.query("set idle_in_transaction_session_timeout = 500")
        .execute()
        .onComplete(ctx.asyncAssertSuccess(v -> {
          res.client.exceptionHandler(err -> {
            PgException pgErr = (PgException) err;
            ctx.assertEquals("25P03", pgErr.getSqlState());
            async.countDown();
          });
          res.client.closeHandler(v2 -> {
            async.countDown();
          });
        }));
    }));
  }
}
