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

package io.vertx.tests.pgclient;

import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgConnection;
import io.vertx.sqlclient.Tuple;
import org.junit.Before;
import org.junit.Test;

import java.util.function.BiConsumer;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class PgPipeliningTest extends PgTestBase {

  Vertx vertx;

  public PgPipeliningTest() {
  }

  @Before
  public void setup() throws Exception {
    super.setup();
    vertx = Vertx.vertx();
  }

  @Test
  public void testPreparedStatementValidationFailure(TestContext ctx) {
    repeat(ctx, (conn, async) -> {
      conn
        .preparedQuery("SELECT $1 :: VARCHAR")
        .execute(Tuple.of(3))
        .onComplete(ctx.asyncAssertFailure(err -> {
          async.countDown();
        }));
    });
  }

  @Test
  public void testPrepareFailure(TestContext ctx) {
    repeat(ctx, (conn, async) -> {
      conn
        .preparedQuery("invalid")
        .execute()
        .onComplete(ctx.asyncAssertFailure(err -> {
          async.countDown();
        }));
    });
  }

  public void repeat(TestContext ctx, BiConsumer<PgConnection, Async> operation) {
    int times = 128;
    Async async = ctx.async(times);
    PgConnectOptions options = new PgConnectOptions(this.options).setPipeliningLimit(1);
    PgConnection.connect(vertx, options).onComplete(ctx.asyncAssertSuccess(conn -> {
      for (int i = 0;i < times;i++) {
        operation.accept(conn, async);
      }
    }));
  }
}
