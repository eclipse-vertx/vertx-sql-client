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

public class PreparedStatementCachedTest extends PreparedStatementTestBase {

  @Override
  protected PgConnectOptions options() {
    return new PgConnectOptions(options).setCachePreparedStatements(true);
  }

  @Test
  public void testConcurrent(TestContext ctx) {
    PgClient.connect(vertx, options(), ctx.asyncAssertSuccess(conn -> {
      Async[] asyncs = new Async[10];
      for (int i = 0;i < 10;i++) {
        asyncs[i] = ctx.async();
      }
      for (int i = 0;i < 10;i++) {
        Async async = asyncs[i];
        conn.prepare("SELECT * FROM Fortune WHERE id=$1", ctx.asyncAssertSuccess(ps -> {
          ps.execute(Tuple.of(1), ctx.asyncAssertSuccess(results -> {
            ctx.assertEquals(1, results.size());
            Tuple row = results.iterator().next();
            ctx.assertEquals(1, row.getInteger(0));
            ctx.assertEquals("fortune: No such file or directory", row.getString(1));
            async.complete();
          }));
        }));
      }
    }));
  }

}
