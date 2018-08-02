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
package io.reactiverse.pgclient;

import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Test;

public class PgTransactionTest extends PgClientTestBase<PgTransaction> {

  private PgPool pool;

  public PgTransactionTest() {
    connector = handler -> {
      if (pool == null) {
        pool = PgClient.pool(vertx, new PgPoolOptions(options).setMaxSize(1));
      }
      pool.begin(handler);
    };
  }

  @Test
  public void testReleaseConnectionOnCommit(TestContext ctx) {
    Async async = ctx.async();
    connector.accept(ctx.asyncAssertSuccess(conn -> {
      conn.query("UPDATE Fortune SET message = 'Whatever' WHERE id = 9", ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(1, result.rowCount());
        conn.commit(ctx.asyncAssertSuccess(v1 -> {
          // Try acquire a connection
          pool.getConnection(ctx.asyncAssertSuccess(v2 -> {
            async.complete();
          }));
        }));
      }));
    }));
  }

  @Test
  public void testReleaseConnectionOnRollback(TestContext ctx) {
    Async async = ctx.async();
    connector.accept(ctx.asyncAssertSuccess(conn -> {
      conn.query("UPDATE Fortune SET message = 'Whatever' WHERE id = 9", ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(1, result.rowCount());
        conn.rollback(ctx.asyncAssertSuccess(v1 -> {
          // Try acquire a connection
          pool.getConnection(ctx.asyncAssertSuccess(v2 -> {
            async.complete();
          }));
        }));
      }));
    }));
  }

  @Test
  public void testReleaseConnectionOnSetRollback(TestContext ctx) {
    Async async = ctx.async();
    connector.accept(ctx.asyncAssertSuccess(conn -> {
      conn.query("SELECT whatever from DOES_NOT_EXIST", ctx.asyncAssertFailure(result -> {
        // Try acquire a connection
        pool.getConnection(ctx.asyncAssertSuccess(v2 -> {
          async.complete();
        }));
      }));
    }));
  }
}
