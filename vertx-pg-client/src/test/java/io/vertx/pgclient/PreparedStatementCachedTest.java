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
import io.vertx.sqlclient.Tuple;
import org.junit.Ignore;
import org.junit.Test;

public class PreparedStatementCachedTest extends PreparedStatementTestBase {

  @Override
  protected PgConnectOptions options() {
    return new PgConnectOptions(options).setCachePreparedStatements(true);
  }

  // Error seems to be different for some implementations
  @Ignore
  @Test
  public void testOneShotPreparedQueryCacheRefreshOnTableSchemaChange(TestContext ctx) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options(), ctx.asyncAssertSuccess(conn -> {
      conn.preparedQuery("SELECT * FROM unstable WHERE id=$1").execute(Tuple.of(1), ctx.asyncAssertSuccess(res1 -> {
        ctx.assertEquals(1, res1.size());
        Tuple row1 = res1.iterator().next();
        ctx.assertEquals(1, row1.getInteger(0));
        ctx.assertEquals("fortune: No such file or directory", row1.getString(1));

        // change table schema
        conn.query("ALTER TABLE unstable DROP COLUMN message").execute(ctx.asyncAssertSuccess(dropColumn -> {
          // failure due to schema change
          conn.preparedQuery("SELECT * FROM unstable WHERE id=$1").execute(Tuple.of(1), ctx.asyncAssertFailure(failure -> {
            // recover because the cache is refreshed
            conn.preparedQuery("SELECT * FROM unstable WHERE id=$1").execute(Tuple.of(1), ctx.asyncAssertSuccess(res2 -> {
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

}
