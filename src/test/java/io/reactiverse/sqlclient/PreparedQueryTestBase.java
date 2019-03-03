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
package io.reactiverse.sqlclient;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.TestContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public abstract class PreparedQueryTestBase {

  protected Vertx vertx;
  protected Connector<SqlConnection> connector;

  protected void connect(Handler<AsyncResult<SqlConnection>> handler) {
    connector.connect(handler);
  }

  protected abstract String statement(String... parts);

  @Before
  public void setUp() throws Exception {
    vertx = Vertx.vertx();
  }

  @After
  public void tearDown(TestContext ctx) {
    vertx.close(ctx.asyncAssertSuccess());
  }

  @Test
  public void testPrepare(TestContext ctx) {
    connect(ctx.asyncAssertSuccess(conn -> {
      conn.prepare(statement("SELECT id, message from immutable where id=", ""), ctx.asyncAssertSuccess(result -> {
      }));
    }));
  }

  @Test
  public void testPrepareError(TestContext ctx) {
    connect(ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT whatever from DOES_NOT_EXIST", ctx.asyncAssertFailure(err -> {
      }));
    }));
  }

  @Test
  public void testPreparedQuery(TestContext ctx) {
    connect(ctx.asyncAssertSuccess(conn -> {
      conn.preparedQuery(statement("SELECT * FROM immutable WHERE id=", ""), Tuple.of(1), ctx.asyncAssertSuccess(rowSet -> {
        ctx.assertEquals(1, rowSet.size());
        Tuple row = rowSet.iterator().next();
        ctx.assertEquals(1, row.getInteger(0));
        ctx.assertEquals("fortune: No such file or directory", row.getString(1));
      }));
    }));
  }

  @Test
  public void testPreparedQueryParamCoercionTypeError(TestContext ctx) {
    connect(ctx.asyncAssertSuccess(conn -> {
      conn.prepare(statement("SELECT * FROM immutable WHERE id=", ""), ctx.asyncAssertSuccess(ps -> {
        ps.execute(Tuple.of("1"), ctx.asyncAssertFailure(rowSet -> {
        }));
      }));
    }));
  }

  @Test
  public void testPreparedQueryParamCoercionQuantityError(TestContext ctx) {
    connect(ctx.asyncAssertSuccess(conn -> {
      conn.prepare(statement("SELECT * FROM immutable WHERE id=", ""), ctx.asyncAssertSuccess(ps -> {
        ps.execute(Tuple.of(1, 2), ctx.asyncAssertFailure(rowSet -> {
        }));
      }));
    }));
  }
}
