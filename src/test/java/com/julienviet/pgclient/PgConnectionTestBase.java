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

import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */

@RunWith(VertxUnitRunner.class)
public abstract class PgConnectionTestBase extends PgTestBase {

  Vertx vertx;
  Consumer<Handler<AsyncResult<PgConnection>>> connector;
  PgConnectOptions options;

  @Before
  public void setup() {
    vertx = Vertx.vertx();
    options = new PgConnectOptions(PgTestBase.options);
  }

  @After
  public void teardown(TestContext ctx) {
    vertx.close(ctx.asyncAssertSuccess());
  }

  @Test
  public void testConnect(TestContext ctx) {
    Async async = ctx.async();
    connector.accept(ctx.asyncAssertSuccess(conn -> {
      async.complete();
    }));
  }

  @Test
  public void testConnectInvalidDatabase(TestContext ctx) {
    Async async = ctx.async();
    options.setDatabase("blah_db");
    connector.accept(ctx.asyncAssertFailure(conn -> {
      ctx.assertEquals("database \"blah_db\" does not exist", conn.getMessage());
      async.complete();
    }));
  }

  @Test
  public void testConnectInvalidPassword(TestContext ctx) {
    Async async = ctx.async();
    options.setPassword("incorrect");
    connector.accept(ctx.asyncAssertFailure(conn -> {
      ctx.assertEquals("password authentication failed for user \"postgres\"", conn.getMessage());
      async.complete();
    }));
  }

  @Test
  public void testConnectInvalidUsername(TestContext ctx) {
    Async async = ctx.async();
    options.setUsername("vertx");
    connector.accept(ctx.asyncAssertFailure(conn -> {
      ctx.assertEquals("password authentication failed for user \"vertx\"", conn.getMessage());
      async.complete();
    }));
  }

  @Test
  public void testQuery(TestContext ctx) {
    Async async = ctx.async();
    connector.accept(ctx.asyncAssertSuccess(conn -> {
      conn.query("SELECT id, randomnumber from WORLD", ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(10000, result.size());
        PgIterator<Row> it = result.iterator();
        for (int i = 0; i < 10000; i++) {
          Row row = it.next();
          ctx.assertEquals(2, row.size());
          ctx.assertTrue(row.getValue(0) instanceof Integer);
          ctx.assertEquals(row.getValue("id"), row.getValue(0));
          ctx.assertTrue(row.getValue(1) instanceof Integer);
          ctx.assertEquals(row.getValue("randomnumber"), row.getValue(1));
        }
        async.complete();
      }));
    }));
  }

  @Test
  public void testMultipleQuery(TestContext ctx) {
    Async async = ctx.async();
    connector.accept(ctx.asyncAssertSuccess(conn -> {
      PgQuery query = conn.createQuery("SELECT id, message from FORTUNE LIMIT 1;SELECT message, id from FORTUNE LIMIT 1");
      query.execute(ctx.asyncAssertSuccess(result1 -> {
        ctx.assertEquals(1, result1.size());
        ctx.assertEquals(Arrays.asList("id", "message"), result1.columnsNames());
        Tuple row1 = result1.iterator().next();
        ctx.assertTrue(row1.getValue(0) instanceof Integer);
        ctx.assertTrue(row1.getValue(1) instanceof String);
        ctx.assertTrue(query.hasMore());
        query.execute(ctx.asyncAssertSuccess(result2 -> {
          ctx.assertEquals(1, result2.size());
          ctx.assertEquals(Arrays.asList("message", "id"), result2.columnsNames());
          Tuple row2 = result2.iterator().next();
          ctx.assertTrue(row2.getValue(0) instanceof String);
          ctx.assertTrue(row2.getValue(1) instanceof Integer);
          ctx.assertFalse(query.hasMore());
          async.complete();
        }));
      }));
    }));
  }

  @Test
  public void testQueueQueries(TestContext ctx) {
    int num = 1000;
    Async async = ctx.async(num + 1);
    connector.accept(ctx.asyncAssertSuccess(conn -> {
      for (int i = 0;i < num;i++) {
        conn.createQuery("SELECT id, randomnumber from WORLD").execute(ar -> {
          if (ar.succeeded()) {
            PgResult result = ar.result();
            ctx.assertEquals(10000, result.size());
          } else {
            ctx.assertEquals("closed", ar.cause().getMessage());
          }
          async.countDown();
        });
      }
      conn.closeHandler(v -> {
        ctx.assertEquals(1, async.count());
        async.countDown();
      });
      conn.close();
    }));
  }

  @Test
  public void testQueryError(TestContext ctx) {
    Async async = ctx.async();
    connector.accept(ctx.asyncAssertSuccess(conn -> {
      conn.createQuery("SELECT whatever from DOES_NOT_EXIST").execute(ctx.asyncAssertFailure(err -> {
        async.complete();
      }));
    }));
  }

  @Test
  public void testUpdate(TestContext ctx) {
    Async async = ctx.async();
    connector.accept(ctx.asyncAssertSuccess(conn -> {
      conn.createQuery("UPDATE Fortune SET message = 'Whatever' WHERE id = 9").execute(ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(1, result.updatedCount());
        async.complete();
      }));
    }));
  }

  @Test
  public void testUpdateError(TestContext ctx) {
    Async async = ctx.async();
    connector.accept(ctx.asyncAssertSuccess(conn -> {
      conn.createQuery("INSERT INTO Fortune (id, message) VALUES (1, 'Duplicate')").execute(ctx.asyncAssertFailure(err -> {
        ctx.assertEquals("23505", ((PgException) err).getCode());
        conn.createQuery("SELECT 1000").execute(ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(1, result.size());
          ctx.assertEquals(1000, result.iterator().next().getInteger(0));
          async.complete();
        }));
      }));
    }));
  }

  @Test
  public void testInsert(TestContext ctx) {
    Async async = ctx.async();
    connector.accept(ctx.asyncAssertSuccess(conn -> {
      conn.createQuery("INSERT INTO Fortune (id, message) VALUES (13, 'Whatever')").execute(ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(1, result.updatedCount());
        async.complete();
      }));
    }));
  }

  @Test
  public void testDelete(TestContext ctx) {
    Async async = ctx.async();
    connector.accept(ctx.asyncAssertSuccess(conn -> {
      conn.createQuery("DELETE FROM Fortune where id = 6").execute(ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(1, result.updatedCount());
        async.complete();
      }));
    }));
  }

  @Test
  public void testBatchUpdate(TestContext ctx) {
    Async async = ctx.async();
    connector.accept(ctx.asyncAssertSuccess(conn -> {
      conn.prepare("UPDATE Fortune SET message=$1 WHERE id=$2", ctx.asyncAssertSuccess(ps -> {
        PgBatch batch = ps.createBatch();
        batch.add(Tuple.of("val0", 1));
        batch.add(Tuple.of("val1", 2));
        batch.execute(ctx.asyncAssertSuccess(results -> {
          ctx.assertEquals(2, results.size());
          for (int i = 0;i < 2;i++) {
            PgResult result = results.get(i);
            ctx.assertEquals(1, result.updatedCount());
          }
          ps.close(ctx.asyncAssertSuccess(result -> {
            async.complete();
          }));
        }));
      }));
    }));
  }

  private static int randomWorld() {
    return 1 + ThreadLocalRandom.current().nextInt(10000);
  }

  @Test
  public void testBatchUpdateError(TestContext ctx) throws Exception {
    Async async = ctx.async();
    connector.accept(ctx.asyncAssertSuccess(conn -> {
      int id = randomWorld();
      conn.prepare("INSERT INTO World (id, randomnumber) VALUES ($1, $2)", ctx.asyncAssertSuccess(worldUpdate -> {
        PgBatch batch = worldUpdate.createBatch();
        batch.add(Tuple.of(id, 3));
        batch.execute(ctx.asyncAssertFailure(err -> {
          ctx.assertEquals("23505", ((PgException) err).getCode());
          conn.createQuery("SELECT 1000").execute(ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.size());
            ctx.assertEquals(1000, result.iterator().next().getInteger(0));
            async.complete();
          }));
        }));
      }));
    }));
  }

  @Test
  public void testClose(TestContext ctx) {
    Async async = ctx.async();
    connector.accept(ctx.asyncAssertSuccess(conn -> {
      conn.closeHandler(v -> {
        async.complete();
      });
      conn.close();
    }));
  }

  @Test
  public void testDisconnectAbruptlyDuringStartup(TestContext ctx) {
    Async async = ctx.async();
    ProxyServer proxy = ProxyServer.create(vertx, options.getPort(), options.getHost());
    proxy.proxyHandler(conn -> {
      conn.clientSocket().handler(buff -> {
        conn.clientSocket().close();
      });
    });
    proxy.listen(8080, "localhost", ctx.asyncAssertSuccess(v1 -> {
      options.setPort(8080).setHost("localhost");
      connector.accept(ctx.asyncAssertFailure(err -> async.complete()));
    }));
  }

  @Test
  public void testDisconnectAbruptly(TestContext ctx) {
    Async async = ctx.async();
    ProxyServer proxy = ProxyServer.create(vertx, options.getPort(), options.getHost());
    proxy.proxyHandler(conn -> {
      vertx.setTimer(200, id -> {
        conn.close();
      });
      conn.connect();
    });
    proxy.listen(8080, "localhost", ctx.asyncAssertSuccess(v1 -> {
      options.setPort(8080).setHost("localhost");
      connector.accept(ctx.asyncAssertSuccess(conn -> {
        conn.closeHandler(v2 -> {
          async.complete();
        });
      }));
    }));
  }

  @Test
  public void testProtocolError(TestContext ctx) {
    Async async = ctx.async();
    ProxyServer proxy = ProxyServer.create(vertx, options.getPort(), options.getHost());
    CompletableFuture<Void> connected = new CompletableFuture<>();
    proxy.proxyHandler(conn -> {
      connected.thenAccept(v -> {
        System.out.println("send bogus");
        Buffer bogusMsg = Buffer.buffer();
        bogusMsg.appendByte((byte) 'R'); // Authentication
        bogusMsg.appendInt(0);
        bogusMsg.appendInt(1);
        bogusMsg.setInt(1, bogusMsg.length() - 1);
        conn.clientSocket().write(bogusMsg);
      });
      conn.connect();
    });
    proxy.listen(8080, "localhost", ctx.asyncAssertSuccess(v1 -> {
      options.setPort(8080).setHost("localhost");
      connector.accept(ctx.asyncAssertSuccess(conn -> {
        AtomicInteger count = new AtomicInteger();
        conn.exceptionHandler(err -> {
          ctx.assertEquals(err.getClass(), UnsupportedOperationException.class);
          count.incrementAndGet();
        });
        conn.closeHandler(v -> {
          ctx.assertEquals(1, count.get());
          async.complete();
        });
        connected.complete(null);
      }));
    }));
  }

  @Test
  public void testCloseWithQueryInProgress(TestContext ctx) {
    Async async = ctx.async(2);
    connector.accept(ctx.asyncAssertSuccess(conn -> {
      conn.createQuery("SELECT id, randomnumber from WORLD").execute(ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(2, async.count());
        ctx.assertEquals(10000, result.size());
        async.countDown();
      }));
      conn.closeHandler(v -> {
        ctx.assertEquals(1, async.count());
        async.countDown();
      });
      conn.close();
    }));
  }

  @Test
  public void testCloseWithErrorInProgress(TestContext ctx) {
    Async async = ctx.async(2);
    connector.accept(ctx.asyncAssertSuccess(conn -> {
      conn.createQuery("SELECT whatever from DOES_NOT_EXIST").execute(ctx.asyncAssertFailure(err -> {
        ctx.assertEquals(2, async.count());
        async.countDown();
      }));
      conn.closeHandler(v -> {
        ctx.assertEquals(1, async.count());
        async.countDown();
      });
      conn.close();
    }));
  }

  @Test
  public void testTx(TestContext ctx) {
    Async async = ctx.async();
    connector.accept(ctx.asyncAssertSuccess(conn -> {
      conn.createQuery("BEGIN").execute(ctx.asyncAssertSuccess(result1 -> {
        ctx.assertEquals(0, result1.size());
        ctx.assertNotNull(result1.iterator());
        conn.createQuery("COMMIT").execute(ctx.asyncAssertSuccess(result2 -> {
          ctx.assertEquals(0, result2.size());
          async.complete();
        }));
      }));
    }));
  }
/*
  @Test
  public void testSQLConnection(TestContext ctx) {
    Async async = ctx.async();
    PgClientImpl client = (PgClientImpl) PgClient.create(vertx, options);
    client.connect(ctx.asyncAssertSuccess(conn -> {
      conn.query("SELECT 1", ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(1, result.rowCount());
        async.complete();
      }));
    }));
  }

  @Test
  public void testSelectForQueryWithParams(TestContext ctx) {
    Async async = ctx.async();
    PgClientImpl client = (PgClientImpl) PgClient.create(vertx, options);
    client.connect(c -> {
      SQLConnection conn = c.result();
      conn.queryWithParams("SELECT * FROM Fortune WHERE id=$1", new JsonArray().add(1) ,
        ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(1, result.rowCount());
        async.complete();
      }));
    });
  }

  @Test
  public void testInsertForUpdateWithParams(TestContext ctx) {
    Async async = ctx.async();
    PgClientImpl client = (PgClientImpl) PgClient.create(vertx, options);
    client.connect(c -> {
      SQLConnection conn = c.result();
      conn.updateWithParams("INSERT INTO Fortune (id, message) VALUES ($1, $2)", new JsonArray().add(1234).add("Yes!"),
        ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(1, result.getUpdated());
          async.complete();
        }));
    });
  }

  @Test
  public void testUpdateForUpdateWithParams(TestContext ctx) {
    Async async = ctx.async();
    PgClientImpl client = (PgClientImpl) PgClient.create(vertx, options);
    client.connect(c -> {
      SQLConnection conn = c.result();
      conn.updateWithParams("UPDATE Fortune SET message = $1 WHERE id = $2", new JsonArray().add("Hello").add(1),
        ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(1, result.getUpdated());
          async.complete();
        }));
    });
  }

  @Test
  public void testDeleteForUpdateWithParams(TestContext ctx) {
    Async async = ctx.async();
    PgClientImpl client = (PgClientImpl) PgClient.create(vertx, options);
    client.connect(c -> {
      SQLConnection conn = c.result();
      conn.updateWithParams("DELETE FROM Fortune WHERE id = $1", new JsonArray().add(3),
        ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(1, result.getUpdated());
          async.complete();
        }));
    });
  }

  @Test
  public void testGetDefaultTx(TestContext ctx) {
    Async async = ctx.async();
    PgClientImpl client = (PgClientImpl) PgClient.create(vertx, options);
    client.connect(c -> {
      SQLConnection conn = c.result();
      conn.getTransactionIsolation(ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(TransactionIsolation.READ_COMMITTED, result);
        async.complete();
      }));
    });
  }

  @Test
  public void testSetUnsupportedTx(TestContext ctx) {
    Async async = ctx.async();
    PgClientImpl client = (PgClientImpl) PgClient.create(vertx, options);
    client.connect(c -> {
      SQLConnection conn = c.result();
      conn.setTransactionIsolation(TransactionIsolation.NONE, ctx.asyncAssertFailure(result -> {
        ctx.assertEquals("None transaction isolation is not supported", result.getMessage());
        async.complete();
      }));
    });
  }

  @Test
  public void testSetAndGetReadUncommittedTx(TestContext ctx) {
    Async async = ctx.async();
    PgClientImpl client = (PgClientImpl) PgClient.create(vertx, options);
    client.connect(c -> {
      SQLConnection conn = c.result();
      conn.setTransactionIsolation(TransactionIsolation.READ_UNCOMMITTED, ctx.asyncAssertSuccess(result -> {
        conn.getTransactionIsolation(ctx.asyncAssertSuccess(res -> {
          ctx.assertEquals(TransactionIsolation.READ_UNCOMMITTED, res);
          async.complete();
        }));
      }));
    });
  }

  @Test
  public void testSetAndGetReadCommittedTx(TestContext ctx) {
    Async async = ctx.async();
    PgClientImpl client = (PgClientImpl) PgClient.create(vertx, options);
    client.connect(c -> {
      SQLConnection conn = c.result();
      conn.setTransactionIsolation(TransactionIsolation.READ_COMMITTED, ctx.asyncAssertSuccess(result -> {
        conn.getTransactionIsolation(ctx.asyncAssertSuccess(res -> {
          ctx.assertEquals(TransactionIsolation.READ_COMMITTED, res);
          async.complete();
        }));
      }));
    });
  }

  @Test
  public void testSetAndGetRepeatableReadTx(TestContext ctx) {
    Async async = ctx.async();
    PgClientImpl client = (PgClientImpl) PgClient.create(vertx, options);
    client.connect(c -> {
      SQLConnection conn = c.result();
      conn.setTransactionIsolation(TransactionIsolation.REPEATABLE_READ, ctx.asyncAssertSuccess(result -> {
        conn.getTransactionIsolation(ctx.asyncAssertSuccess(res -> {
          ctx.assertEquals(TransactionIsolation.REPEATABLE_READ, res);
          async.complete();
        }));
      }));
    });
  }

  @Test
  public void testSetAndGetSerializableTx(TestContext ctx) {
    Async async = ctx.async();
    PgClientImpl client = (PgClientImpl) PgClient.create(vertx, options);
    client.connect(c -> {
      SQLConnection conn = c.result();
      conn.setTransactionIsolation(TransactionIsolation.SERIALIZABLE, ctx.asyncAssertSuccess(result -> {
        conn.getTransactionIsolation(ctx.asyncAssertSuccess(res -> {
          ctx.assertEquals(TransactionIsolation.SERIALIZABLE, res);
          async.complete();
        }));
      }));
    });
  }
*/
  @Test
  public void testPreparedUpdate(TestContext ctx) {
    Async async = ctx.async();
    connector.accept(ctx.asyncAssertSuccess(conn -> {
      conn.prepare("UPDATE Fortune SET message = 'PgClient Rocks!' WHERE id = 2", ctx.asyncAssertSuccess(ps -> {
        PgQuery update = ps.createQuery();
        update.execute(ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(1, result.updatedCount());
          conn.prepare("SELECT message FROM Fortune WHERE id = 2", ctx.asyncAssertSuccess(ps2 -> {
            ps2.createQuery()
              .execute(ctx.asyncAssertSuccess(r -> {
                ctx.assertEquals("PgClient Rocks!", r.iterator().next().getValue(0));
                async.complete();
              }));
          }));
        }));
      }));
    }));
  }

  @Test
  public void testPreparedUpdateWithParams(TestContext ctx) {
    Async async = ctx.async();
    connector.accept(ctx.asyncAssertSuccess(conn -> {
      conn.prepare("UPDATE Fortune SET message = $1 WHERE id = $2", ctx.asyncAssertSuccess(ps -> {
        PgQuery update = ps.createQuery(Tuple.of("PgClient Rocks Again!!", 2));
        update.execute(ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(1, result.updatedCount());
          conn.prepare("SELECT message FROM Fortune WHERE id = $1", ctx.asyncAssertSuccess(ps2 -> {
            ps2.createQuery(Tuple.of(2))
              .execute(ctx.asyncAssertSuccess(r -> {
                ctx.assertEquals("PgClient Rocks Again!!", r.iterator().next().getValue(0));
                async.complete();
              }));
          }));
        }));
      }));
    }));
  }

  @Test
  public void testCloseOnUndeploy(TestContext ctx) {
    Async done = ctx.async();
    vertx.deployVerticle(new AbstractVerticle() {
      @Override
      public void start(Future<Void> startFuture) throws Exception {
        connector.accept(ctx.asyncAssertSuccess(conn -> {
          conn.closeHandler(v -> {
            done.complete();
          });
          startFuture.complete();
        }));
      }
    }, ctx.asyncAssertSuccess(id -> {
      vertx.undeploy(id);
    }));
  }

  @Test
  public void testTransactionCommit(TestContext ctx) {
    Async done = ctx.async();
    connector.accept(ctx.asyncAssertSuccess(conn -> {
      conn.begin();
      conn.query("INSERT INTO TxTest (id) VALUES (1)", ar1 -> {
        System.out.println("got res 1");
      });
      conn.query("INSERT INTO TxTest (id) VALUES (2)", ar2 -> {
        System.out.println("got res 2");
      });
      conn.commit(ctx.asyncAssertSuccess(v -> {
        conn.query("SELECT id FROM TxTest WHERE id=1 OR id=2", ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(2, result.size());
          done.complete();
        }));
      }));
    }));
  }

  @Test
  public void testTransactionRollback(TestContext ctx) {
    Async done = ctx.async();
    connector.accept(ctx.asyncAssertSuccess(conn -> {
      conn.begin();
      conn.query("INSERT INTO TxTest (id) VALUES (3)", ar1 -> {
        System.out.println("got res 1 " + ar1.succeeded());
      });
      conn.query("INSERT INTO TxTest (id) VALUES (4)", ar2 -> {
        System.out.println("got res 2");
      });
      conn.rollback(ctx.asyncAssertSuccess(v -> {
        conn.query("SELECT id FROM TxTest WHERE id=3 OR id=4", ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(0, result.size());
          done.complete();
        }));
      }));
    }));
  }

  @Test
  public void testTransactionFailure(TestContext ctx) {
    Async done = ctx.async();
    connector.accept(ctx.asyncAssertSuccess(conn -> {
      conn.begin();
      conn.query("INSERT INTO TxTest (id) VALUES (5)", ar1 -> {
        System.out.println("got res 1");
      });
      conn.query("invalid-sql", ar2 -> {
        conn.query("SELECT id FROM TxTest WHERE id=5", ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(0, result.size());
          done.complete();
        }));
      });
    }));
  }

  /*
  @Test
  public void testServerUpdate(TestContext ctx) {

    ctx.async();

    PostgresClient client = PostgresClient.create(vertx, options);
    PostgresConnectionPool pool = client.createPool(1);
    HttpServer server = vertx.createHttpServer();
    server.requestHandler(req -> {
      new Update(pool).handle(req);
    });
    server.listen(8080, ctx.asyncAssertSuccess());


  }

  class Update {

    boolean failed;
    JsonArray worlds = new JsonArray();
    PostgresConnectionPool pool;

    public Update(PostgresConnectionPool pool) {
      this.pool = pool;
    }

    public void handle(HttpServerRequest req) {
      HttpServerResponse resp = req.response();
      final int queries = getQueries(req);

      pool.connect(ar1 -> {
        if (ar1.succeeded()) {
          PostgresConnection conn = ar1.result();

          int[] ids = new int[queries];
          Row[] rows = new Row[queries];
          for (int i = 0; i < queries; i++) {
            int index = i;
            int id = randomWorld();
            ids[i] = id;
            conn.execute("SELECT id, randomnumber from WORLD where id = " + id, ar2 -> {
              if (!failed) {
                if (ar2.failed()) {
                  failed = true;
                  resp.setStatusCode(500).end(ar2.cause().getMessage());
                  conn.close();
                  return;
                }
                rows[index] = ar2.result().get(0);
              }
            });
          }

          conn.execute("BEGIN", ar2 -> {
            if (!failed) {
              if (ar2.failed()) {
                failed = true;
                resp.setStatusCode(500).end(ar2.cause().getMessage());
                conn.close();
              }

              for (int i = 0;i < queries;i++) {
                int index = i;
                int randomNumber = randomWorld();

                conn.execute("UPDATE world SET randomnumber = " + randomNumber + " WHERE id = " + ids[i], ar4 -> {
                  if (!failed) {
                    if (ar4.failed()) {
                      failed = true;
                      resp.setStatusCode(500).end(ar4.cause().getMessage());
                      conn.close();
                      return;
                    }
                    Row row = rows[index];
                    worlds.add(new JsonObject().put("id", "" + row.get(0)).put("randomNumber", "" + randomNumber));
                  }
                });
              }

              conn.execute("COMMIT", ar5 -> {
                if (!failed) {
                  if (ar5.failed()) {
                    failed = true;
                    resp.setStatusCode(500).end(ar5.cause().getMessage());
                    conn.close();
                    return;
                  }
                  conn.close();
                  resp
                    .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                    .end(Json.encode(worlds.encode()));
                }
              });
            }
          });
        } else {
          resp.setStatusCode(500).end(ar1.cause().getMessage());
        }
      });
    }

    int getQueries(HttpServerRequest request) {
      String param = request.getParam("queries");

      if (param == null) {
        return 1;
      }
      try {
        int parsedValue = Integer.parseInt(param);
        return Math.min(500, Math.max(1, parsedValue));
      } catch (NumberFormatException e) {
        return 1;
      }
    }

    private int randomWorld() {
      return 1 + ThreadLocalRandom.current().nextInt(10000);
    }
  }
*/

}
