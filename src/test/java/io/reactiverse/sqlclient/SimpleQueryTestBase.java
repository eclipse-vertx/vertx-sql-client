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

import java.util.Iterator;

public abstract class SimpleQueryTestBase {

  protected Vertx vertx;
  protected Connector<SqlClient> connector;

  protected void connect(Handler<AsyncResult<SqlClient>> handler) {
    connector.connect(handler);
  }

  @Before
  public void setUp() throws Exception {
    vertx = Vertx.vertx();
  }

  @After
  public void tearDown(TestContext ctx) {
    vertx.close(ctx.asyncAssertSuccess());
  }

  @Test
  public void testQuery(TestContext ctx) {
    connect(ctx.asyncAssertSuccess(conn -> {
      conn.query("SELECT id, randomnumber from world", ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(10000, result.size());
        Iterator<Row> it = result.iterator();
        for (int i = 0; i < 10000; i++) {
          Row row = it.next();
          ctx.assertEquals(2, row.size());
          ctx.assertTrue(row.getValue(0) instanceof Integer);
          ctx.assertEquals(row.getValue("id"), row.getValue(0));
          ctx.assertTrue(row.getValue(1) instanceof Integer);
          ctx.assertEquals(row.getValue("randomnumber"), row.getValue(1));
        }
      }));
    }));
  }

  @Test
  public void testQueryError(TestContext ctx) {
    connect(ctx.asyncAssertSuccess(conn -> {
      conn.query("SELECT whatever from DOES_NOT_EXIST", ctx.asyncAssertFailure(err -> {
      }));
    }));
  }
}
