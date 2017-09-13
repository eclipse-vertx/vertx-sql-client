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

import com.julienviet.pgclient.impl.PreparedQueryWithParams;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.sql.SQLRowStream;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.LinkedList;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@RunWith(VertxUnitRunner.class)
public class PreparedQueryWithParamsTest extends PgTestBase {

  Vertx vertx;

  @Before
  public void setup() {
    vertx = Vertx.vertx();
  }

  @After
  public void teardown(TestContext ctx) {
    vertx.close(ctx.asyncAssertSuccess());
  }

  @Test
  public void testQuery(TestContext ctx) {
    Async async = ctx.async();
    PgClient client = PgClient.create(vertx, options);
    client.connect(ctx.asyncAssertSuccess(conn -> {
      PgPreparedStatement ps = conn.prepare("SELECT * FROM Fortune WHERE id=$1 OR id=$2 OR id=$3 OR id=$4 OR id=$5 OR id=$6");
      PgQuery query = ps.query(1, 8, 4, 11, 2, 9);
      query.execute(ctx.asyncAssertSuccess(results -> {
        ctx.assertEquals(6, results.getNumRows());
        ps.close(ctx.asyncAssertSuccess(result -> {
          async.complete();
        }));
      }));
    }));
  }

  @Test
  public void testQueryStream(TestContext ctx) {
    Async async = ctx.async();
    PgClient client = PgClient.create(vertx, options);
    client.connect(ctx.asyncAssertSuccess(conn -> {
      PgPreparedStatement ps = conn.prepare("SELECT * FROM Fortune WHERE id=$1 OR id=$2 OR id=$3 OR id=$4 OR id=$5 OR id=$6");
      SQLRowStream stream = (SQLRowStream) ps.query(1, 8, 4, 11, 2, 9);
      LinkedList<JsonArray> results = new LinkedList<>();
      stream.handler(results::add);
      stream.exceptionHandler(ctx::fail);
      stream.endHandler(v -> {
        ctx.assertEquals(6, results.size());
        ps.close(ctx.asyncAssertSuccess(result -> {
          async.complete();
        }));
      });
      stream.moreResults();
    }));
  }

  @Test
  public void testQueryParseError(TestContext ctx) {
    Async async = ctx.async();
    PgClient client = PgClient.create(vertx, options);
    client.connect(ctx.asyncAssertSuccess(conn -> {
      PgPreparedStatement ps = conn.prepare("invalid");
      PgQuery query = ps.query(1, 8, 4, 11, 2, 9);
      query.execute(ctx.asyncAssertFailure(err -> {
        PgException pgErr = (PgException) err;
        ctx.assertEquals(ErrorCodes.syntax_error, pgErr.getCode());
        async.complete();
      }));
    }));
  }

  @Test
  public void testQueryStreamParseError(TestContext ctx) {
    Async async = ctx.async();
    PgClient client = PgClient.create(vertx, options);
    client.connect(ctx.asyncAssertSuccess(conn -> {
      PgPreparedStatement ps = conn.prepare("invalid");
      SQLRowStream stream = (SQLRowStream) ps.query(1, 8, 4, 11, 2, 9);
      stream.handler(row -> ctx.fail());
      stream.endHandler(v -> ctx.fail());
      stream.exceptionHandler(err -> {
        PgException pgErr = (PgException) err;
        ctx.assertEquals(ErrorCodes.syntax_error, pgErr.getCode());
        async.complete();
      });
      stream.moreResults();
    }));
  }

  @Test
  public void testQueryBindError(TestContext ctx) {
    Async async = ctx.async();
    PgClient client = PgClient.create(vertx, options);
    client.connect(ctx.asyncAssertSuccess(conn -> {
      PgPreparedStatement ps = conn.prepare("SELECT * FROM Fortune WHERE id=$1");
      PgQuery query = ps.query("invalid-id");
      query.execute(ctx.asyncAssertFailure(err -> {
        PgException pgErr = (PgException) err;
        ctx.assertEquals(ErrorCodes.invalid_text_representation, pgErr.getCode());
        async.complete();
      }));
    }));
  }

  // Need to test partial query close or abortion ?
  @Test
  public void testQueryCursor(TestContext ctx) {
    Async async = ctx.async();
    PgClient client = PgClient.create(vertx, options);
    client.connect(ctx.asyncAssertSuccess(conn -> {
      conn.query("BEGIN", ctx.asyncAssertSuccess(begin -> {
        PgPreparedStatement ps = conn.prepare("SELECT * FROM Fortune WHERE id=$1 OR id=$2 OR id=$3 OR id=$4 OR id=$5 OR id=$6");
        PgQuery query = ps.query(1, 8, 4, 11, 2, 9);
        query.fetch(4);
        ctx.assertFalse(query.inProgress());
        ctx.assertFalse(query.completed());
        query.execute(ctx.asyncAssertSuccess(results -> {
          ctx.assertEquals(4, results.getNumRows());
          ctx.assertTrue(query.inProgress());
          ctx.assertFalse(query.completed());
          query.execute(ctx.asyncAssertSuccess(results2 -> {
            ctx.assertNotNull(results2.getColumnNames());
            ctx.assertEquals(2, results2.getNumRows());
            ctx.assertFalse(query.inProgress());
            ctx.assertTrue(query.completed());
            ps.close(ctx.asyncAssertSuccess(v2 -> {
              async.complete();
            }));
          }));
        }));
      }));
    }));
  }

  @Test
  public void testQueryStreamCursor(TestContext ctx) {
    Async async = ctx.async();
    PgClient client = PgClient.create(vertx, options);
    client.connect(ctx.asyncAssertSuccess(conn -> {
      conn.query("BEGIN", ctx.asyncAssertSuccess(begin -> {
        PgPreparedStatement ps = conn.prepare("SELECT * FROM Fortune WHERE id=$1 OR id=$2 OR id=$3 OR id=$4 OR id=$5 OR id=$6");
        PreparedQueryWithParams stream = (PreparedQueryWithParams) ps.query(1, 8, 4, 11, 2, 9);
        stream.fetch(4);
        LinkedList<JsonArray> results = new LinkedList<>();
        stream.handler(results::add);
        stream.exceptionHandler(ctx::fail);
        stream.resultSetClosedHandler(v -> {
          ctx.assertEquals(4, results.size());
          stream.moreResults();
        });
        stream.endHandler(v -> {
          ctx.assertEquals(6, results.size());
          ps.close(ctx.asyncAssertSuccess(result -> {
            async.complete();
          }));
        });
        stream.moreResults();
      }));
    }));
  }

  @Test
  public void testQueryCloseCursor(TestContext ctx) {
    Async async = ctx.async();
    PgClient client = PgClient.create(vertx, options);
    client.connect(ctx.asyncAssertSuccess(conn -> {
      conn.query("BEGIN", ctx.asyncAssertSuccess(begin -> {
        PgPreparedStatement ps = conn.prepare("SELECT * FROM Fortune WHERE id=$1 OR id=$2 OR id=$3 OR id=$4 OR id=$5 OR id=$6");
        PgQuery query = ps.query(1, 8, 4, 11, 2, 9);
        query.fetch(4);
        query.execute(ctx.asyncAssertSuccess(results -> {
          ctx.assertEquals(4, results.getNumRows());
          ctx.assertTrue(query.inProgress());
          ctx.assertFalse(query.completed());
          query.close(ctx.asyncAssertSuccess(v1 -> {
            ctx.assertFalse(query.inProgress());
            ctx.assertTrue(query.completed());
            ps.close(ctx.asyncAssertSuccess(v2 -> {
              async.complete();
            }));
          }));
        }));
      }));
    }));
  }

  @Test
  public void testQueryStreamCloseCursor(TestContext ctx) {
    Async async = ctx.async();
    PgClient client = PgClient.create(vertx, options);
    client.connect(ctx.asyncAssertSuccess(conn -> {
      conn.query("BEGIN", ctx.asyncAssertSuccess(begin -> {
        PgPreparedStatement ps = conn.prepare("SELECT * FROM Fortune WHERE id=$1 OR id=$2 OR id=$3 OR id=$4 OR id=$5 OR id=$6");
        PreparedQueryWithParams stream = (PreparedQueryWithParams) ps.query(1, 8, 4, 11, 2, 9);
        stream.fetch(4);
        LinkedList<JsonArray> results = new LinkedList<>();
        stream.handler(results::add);
        stream.exceptionHandler(ctx::fail);
        stream.resultSetClosedHandler(v -> {
          ctx.assertEquals(4, results.size());
          stream.close(ctx.asyncAssertSuccess(v1 -> {
            ps.close(ctx.asyncAssertSuccess(v2 -> {
              async.complete();
            }));
          }));
        });
        stream.endHandler(v -> ctx.fail());
        stream.moreResults();
      }));
    }));
  }
}
