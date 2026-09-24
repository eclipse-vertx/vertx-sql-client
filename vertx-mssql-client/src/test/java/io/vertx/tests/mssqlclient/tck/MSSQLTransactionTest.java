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
package io.vertx.tests.mssqlclient.tck;

import io.vertx.core.Future;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.mssqlclient.MSSQLBuilder;
import io.vertx.mssqlclient.MSSQLConnectOptions;
import io.vertx.tests.mssqlclient.junit.MSSQLRule;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.tests.sqlclient.tck.TransactionTestBase;
import org.junit.AssumptionViolatedException;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class MSSQLTransactionTest extends TransactionTestBase {

  @ClassRule
  public static MSSQLRule rule = MSSQLRule.SHARED_INSTANCE;

  @Override
  protected Pool createPool() {
    return MSSQLBuilder.pool(builder -> builder.with(new PoolOptions().setMaxSize(1)).connectingTo(new MSSQLConnectOptions(rule.options())).using(vertx));
  }

  @Override
  protected Pool nonTxPool() {
    return MSSQLBuilder.pool(builder -> builder.with(new PoolOptions().setMaxSize(1)).connectingTo(new MSSQLConnectOptions(rule.options())).using(vertx));
  }

  @Override
  protected String statement(String... parts) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < parts.length; i++) {
      if (i > 0) {
        sb.append("@p").append((i));
      }
      sb.append(parts[i]);
    }
    return sb.toString();
  }

  @Test
  public void testDelayedCommit(TestContext ctx) {
    throw new AssumptionViolatedException("MSSQL holds write locks on inserted row with isolation level = 2");
  }

  @Override
  protected boolean supportsSavepoints() {
    return true;
  }

  @Override
  protected boolean supportsSavepointRelease() {
    return false;
  }

  /**
   * SQL Server drops the savepoint once the transaction has been rolled back to it.
   */
  @Override
  protected boolean supportsRepeatedRollbackToSavepoint() {
    return false;
  }

  /**
   * A savepoint is a mark inside the current transaction, it must not open a nested one:
   * @@TRANCOUNT stays at 1 after SAVE TRANSACTION and after rolling back to it.
   */
  @Test
  public void testSavepointDoesNotNestTheTransaction(TestContext ctx) {
    Async async = ctx.async();
    connector.accept(ctx.asyncAssertSuccess(res -> {
      trancount(res.client)
        .compose(before -> {
          ctx.assertEquals(1, before, "the transaction should be the only one open");
          return res.tx.createSavepoint();
        })
        .compose(sp -> trancount(res.client)
          .compose(afterSave -> {
            ctx.assertEquals(1, afterSave, "SAVE TRANSACTION must not nest a transaction");
            return insertMutable(res.client, 1, "rolled-back");
          })
          .compose(v -> sp.rollback())
          .compose(v -> trancount(res.client))
          .compose(afterRollback -> {
            ctx.assertEquals(1, afterRollback, "rolling back to a savepoint must keep the transaction open");
            return insertMutable(res.client, 2, "kept");
          })
          .compose(v -> res.tx.commit()))
        .compose(v -> assertMutableIds(ctx, 2))
        .onComplete(ctx.asyncAssertSuccess(v -> async.complete()));
    }));
  }

  private Future<Integer> trancount(SqlConnection client) {
    return client.query("SELECT @@TRANCOUNT AS c").execute().map(rows -> rows.iterator().next().getInteger("c"));
  }
}
