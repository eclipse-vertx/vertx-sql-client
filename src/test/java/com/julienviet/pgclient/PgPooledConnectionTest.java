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

package com.julienviet.pgclient;

import com.julienviet.pgclient.impl.PgConnectionFactory;
import io.vertx.core.Future;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Test;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class PgPooledConnectionTest extends PgConnectionTestBase {

  private PgPool pool;

  public PgPooledConnectionTest() {
    connector = (handler) -> {
      if (pool == null) {
        pool = PgPool.pool(vertx, new PgPoolOptions(options).setMaxSize(1));
      }
      pool.connect(handler);
    };
  }

  @Override
  public void teardown(TestContext ctx) {
    if (pool != null) {
      pool.close();
    }
    super.teardown(ctx);
  }

  @Override
  public void testBatchUpdate(TestContext ctx) {
  }

  @Override
  public void testClose(TestContext ctx) {
  }

  @Override
  public void testCloseWithErrorInProgress(TestContext ctx) {
  }

  @Override
  public void testCloseWithQueryInProgress(TestContext ctx) {
  }

  @Override
  public void testQueueQueries(TestContext ctx) {
  }

  @Test
  public void testThatPoolReconnect(TestContext ctx) {
  }

  @Test
  public void testTransactionRollbackUnfinishedOnRecycle(TestContext ctx) {
    Async done = ctx.async(2);
    connector.accept(ctx.asyncAssertSuccess(conn1 -> {
      conn1.begin();
      conn1.query("INSERT INTO TxTest (id) VALUES (5)", ctx.asyncAssertSuccess());
      conn1.query("SELECT txid_current()", ctx.asyncAssertSuccess(result -> {
        Long txid1 = result.iterator().next().getLong(0);
        conn1.close();
        // It will be the same connection
        connector.accept(ctx.asyncAssertSuccess(conn2 -> {
          conn2.query("SELECT id FROM TxTest WHERE id=5", ctx.asyncAssertSuccess(result2 -> {
            ctx.assertEquals(0, result2.size());
            done.countDown();
          }));
          conn2.query("SELECT txid_current()", ctx.asyncAssertSuccess(result2 -> {
            Long txid2 = result.iterator().next().getLong(0);
            ctx.assertEquals(txid1, txid2);
            done.countDown();
          }));
        }));
      }));
    }));
  }
}
