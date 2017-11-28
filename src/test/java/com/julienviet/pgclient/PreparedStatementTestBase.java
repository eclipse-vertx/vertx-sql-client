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

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@RunWith(VertxUnitRunner.class)
public abstract class PreparedStatementTestBase extends PgTestBase {

  Vertx vertx;
  PgClient client;

  protected abstract PgClientOptions options();

  @Before
  public void setup() {
    vertx = Vertx.vertx();
    client = PgClient.create(vertx, options());
  }

  @After
  public void teardown(TestContext ctx) {
    vertx.close(ctx.asyncAssertSuccess());
  }

  @Test
  public void testQuery1Param(TestContext ctx) {
    Async async = ctx.async();
    client.connect(ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT * FROM Fortune WHERE id=$1", ctx.asyncAssertSuccess(ps -> {
        PgQuery query = ps.query(1);
        query.execute(ctx.asyncAssertSuccess(results -> {
          ctx.assertEquals(1, results.getNumRows());
          JsonArray row = results.getResults().get(0);
          ctx.assertEquals(1, row.getInteger(0));
          ctx.assertEquals("fortune: No such file or directory", row.getString(1));
          ps.close(ctx.asyncAssertSuccess(ar -> {
            async.complete();
          }));
        }));
      }));
    }));
  }

  @Test
  public void testQuery(TestContext ctx) {
    Async async = ctx.async();
    client.connect(ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT * FROM Fortune WHERE id=$1 OR id=$2 OR id=$3 OR id=$4 OR id=$5 OR id=$6", ctx.asyncAssertSuccess(ps -> {
        PgQuery query = ps.query(1, 8, 4, 11, 2, 9);
        query.execute(ctx.asyncAssertSuccess(results -> {
          ctx.assertEquals(6, results.getNumRows());
          ps.close(ctx.asyncAssertSuccess(result -> {
            async.complete();
          }));
        }));
      }));
    }));
  }

  @Test
  public void testQueryStream(TestContext ctx) {
    Async async = ctx.async();
    client.connect(ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT * FROM Fortune WHERE id=$1 OR id=$2 OR id=$3 OR id=$4 OR id=$5 OR id=$6", ctx.asyncAssertSuccess(ps -> {
        PgQuery stream = ps.query(1, 8, 4, 11, 2, 9);
        LinkedList<JsonArray> results = new LinkedList<>();
        stream.exceptionHandler(ctx::fail);
        stream.endHandler(v -> {
          ctx.assertEquals(6, results.size());
          ps.close(ctx.asyncAssertSuccess(result -> {
            async.complete();
          }));
        });
        stream.handler(rs -> results.addAll(rs.getResults()));
      }));
    }));
  }

  @Test
  public void testQueryParseError(TestContext ctx) {
    Async async = ctx.async();
    client.connect(ctx.asyncAssertSuccess(conn -> {
      conn.prepare("invalid", ctx.asyncAssertFailure(err -> {
        PgException pgErr = (PgException) err;
        ctx.assertEquals(ErrorCodes.syntax_error, pgErr.getCode());
        async.complete();
      }));
    }));
  }

  @Test
  public void testQueryBindError(TestContext ctx) {
    Async async = ctx.async();
    client.connect(ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT * FROM Fortune WHERE id=$1", ctx.asyncAssertSuccess(ps -> {
        PgQuery query = ps.query("invalid-id");
        query.execute(ctx.asyncAssertFailure(err -> {
          PgException pgErr = (PgException) err;
          ctx.assertEquals(ErrorCodes.invalid_text_representation, pgErr.getCode());
          async.complete();
        }));
      }));
    }));
  }

  // Need to test partial query close or abortion ?
  @Test
  public void testQueryCursor(TestContext ctx) {
    Async async = ctx.async();
    client.connect(ctx.asyncAssertSuccess(conn -> {
      conn.query("BEGIN").execute(ctx.asyncAssertSuccess(begin -> {
        conn.prepare("SELECT * FROM Fortune WHERE id=$1 OR id=$2 OR id=$3 OR id=$4 OR id=$5 OR id=$6", ctx.asyncAssertSuccess(ps -> {
          PgQuery query = ps.query(1, 8, 4, 11, 2, 9);
          query.fetch(4);
          List<ResultSet> results = new ArrayList<>();
          query.endHandler(v -> {
            ctx.assertEquals(2, results.size());
            ctx.assertEquals(4, results.get(0).getNumRows());
            ctx.assertEquals(2, results.get(1).getNumRows());
            async.complete();
          });
          query.handler(result -> {
            ctx.assertNotNull(result.getColumnNames());
            results.add(result);
          });
        }));
      }));
    }));
  }

  @Test
  public void testQueryStreamCursor(TestContext ctx) {
    Async async = ctx.async();
    client.connect(ctx.asyncAssertSuccess(conn -> {
      conn.query("BEGIN").execute(ctx.asyncAssertSuccess(begin -> {
        conn.prepare("SELECT * FROM Fortune WHERE id=$1 OR id=$2 OR id=$3 OR id=$4 OR id=$5 OR id=$6", ctx.asyncAssertSuccess(ps -> {
          PgQuery stream = ps.query(1, 8, 4, 11, 2, 9);
          stream.fetch(4);
          LinkedList<ResultSet> results = new LinkedList<>();
          stream.exceptionHandler(ctx::fail);
          stream.endHandler(v -> {
            ctx.assertEquals(2, results.size());
            ctx.assertEquals(4, results.get(0).getNumRows());
            ctx.assertEquals(2, results.get(1).getNumRows());
            ps.close(ctx.asyncAssertSuccess(result -> {
              async.complete();
            }));
          });
          stream.handler(results::add);
        }));
      }));
    }));
  }

  @Test
  public void testQueryCloseCursor(TestContext ctx) {
    Async async = ctx.async();
    client.connect(ctx.asyncAssertSuccess(conn -> {
      conn.query("BEGIN").execute(ctx.asyncAssertSuccess(begin -> {
        conn.prepare("SELECT * FROM Fortune WHERE id=$1 OR id=$2 OR id=$3 OR id=$4 OR id=$5 OR id=$6", ctx.asyncAssertSuccess(ps -> {
          PgQuery query = ps.query(1, 8, 4, 11, 2, 9);
          query.fetch(4);
          query.execute(ctx.asyncAssertSuccess(results -> {
            ctx.assertEquals(4, results.getNumRows());
            query.close(ctx.asyncAssertSuccess(v1 -> {
              ps.close(ctx.asyncAssertSuccess(v2 -> {
                async.complete();
              }));
            }));
          }));
        }));
      }));
    }));
  }

  @Test
  public void testQueryStreamCloseCursor(TestContext ctx) {
    Async async = ctx.async();
    client.connect(ctx.asyncAssertSuccess(conn -> {
      conn.query("BEGIN").execute(ctx.asyncAssertSuccess(begin -> {
        conn.prepare("SELECT * FROM Fortune WHERE id=$1 OR id=$2 OR id=$3 OR id=$4 OR id=$5 OR id=$6", ctx.asyncAssertSuccess(ps -> {
          PgQuery stream = ps.query(1, 8, 4, 11, 2, 9);
          stream.fetch(4);
          AtomicInteger results = new AtomicInteger();
          AtomicInteger completions = new AtomicInteger();
          stream.exceptionHandler(ctx::fail);
          stream.endHandler(v -> completions.incrementAndGet());
          stream.handler(result -> {
            ctx.assertEquals(4, results.addAndGet(result.getNumRows()));
            stream.close(ctx.asyncAssertSuccess(v1 -> {
              ctx.assertEquals(1, completions.get());
              ps.close(ctx.asyncAssertSuccess(v2 -> {
                async.complete();
              }));
            }));
          });
        }));
      }));
    }));
  }
}
