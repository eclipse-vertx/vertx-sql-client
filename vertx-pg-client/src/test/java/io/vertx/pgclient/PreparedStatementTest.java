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

import io.vertx.ext.unit.TestContext;
import io.vertx.sqlclient.Tuple;
import org.junit.Test;

import java.time.Duration;

public class PreparedStatementTest extends PreparedStatementTestBase {

  @Override
  protected PgConnectOptions options() {
    return new PgConnectOptions(options).setCachePreparedStatements(false);
  }

  @Test
  public void testPrepareExecuteValidationErrorDefaultExtractor(TestContext ctx) {
    PgConnection.connect(vertx, options(), ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT $1 :: INTERVAL \"Interval\"", ctx.asyncAssertSuccess(ps -> {
        ps.query().execute(Tuple.of(Duration.ofHours(3)), ctx.asyncAssertFailure());
      }));
    }));
  }

  @Test
  public void testPrepareExecuteValidationErrorDefaultExtractor_(TestContext ctx) {
    PgConnection.connect(vertx, options(), ctx.asyncAssertSuccess(conn -> {
      conn
        .preparedQuery("SELECT $1 :: INTERVAL \"Interval\"")
        .execute(Tuple.of(Duration.ofHours(3)), ctx.asyncAssertFailure());
    }));
  }
}
