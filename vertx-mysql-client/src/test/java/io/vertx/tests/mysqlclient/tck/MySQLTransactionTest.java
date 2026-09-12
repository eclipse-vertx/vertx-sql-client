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
package io.vertx.tests.mysqlclient.tck;

import io.vertx.core.Future;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.mysqlclient.MySQLBuilder;
import io.vertx.tests.mysqlclient.junit.MySQLRule;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.tests.sqlclient.tck.TransactionTestBase;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class MySQLTransactionTest extends TransactionTestBase {

  @ClassRule
  public static MySQLRule rule = MySQLRule.SHARED_INSTANCE;

  @Override
  protected Pool createPool() {
    return MySQLBuilder.pool(builder -> builder.with(new PoolOptions().setMaxSize(1)).connectingTo(rule.options()).using(vertx));
  }

  @Override
  protected Pool nonTxPool() {
    return MySQLBuilder.pool(builder -> builder.with(new PoolOptions().setMaxSize(1)).connectingTo(rule.options()).using(vertx));
  }

  @Override
  protected String statement(String... parts) {
    return String.join("?", parts);
  }

  @Override
  protected boolean supportsSavepoints() {
    return true;
  }

  /**
   * MySQL does not put a transaction into a failed state when a statement fails, so
   * the transaction stays usable and the work before the failure is still committed.
   * PostgreSQL fails the whole transaction instead, {@code PgTransactionTest} covers that.
   */
  @Test
  public void testStatementErrorLeavesTransactionUsable(TestContext ctx) {
    Async async = ctx.async();
    connector.accept(ctx.asyncAssertSuccess(res -> {
      insertMutable(res.client, 1, "before")
        .compose(v -> insertMutable(res.client, 1, "duplicate"))
        .transform(ar -> {
          ctx.assertTrue(ar.failed(), "the duplicate key should have failed");
          // no rollback to a savepoint needed, the transaction is still alive
          return insertMutable(res.client, 2, "after");
        })
        .compose(v -> res.tx.commit())
        .compose(v -> assertMutableIds(ctx, 1, 2))
        .onComplete(ctx.asyncAssertSuccess(v -> async.complete()));
    }));
  }

  /**
   * Rolling back to a savepoint after a failed statement still discards the work that
   * followed the savepoint, even though the transaction was never in a failed state.
   */
  @Test
  public void testRollbackToSavepointAfterStatementError(TestContext ctx) {
    Async async = ctx.async();
    connector.accept(ctx.asyncAssertSuccess(res -> {
      insertMutable(res.client, 1, "before")
        .compose(v -> res.tx.createSavepoint())
        .compose(sp -> insertMutable(res.client, 2, "rolled-back")
          .compose(v -> insertMutable(res.client, 1, "duplicate"))
          .transform(ar -> {
            ctx.assertTrue(ar.failed(), "the duplicate key should have failed");
            return sp.rollback();
          })
          .compose(v -> insertMutable(res.client, 3, "after"))
          .compose(v -> res.tx.commit()))
        .compose(v -> assertMutableIds(ctx, 1, 3))
        .onComplete(ctx.asyncAssertSuccess(v -> async.complete()));
    }));
  }
}
