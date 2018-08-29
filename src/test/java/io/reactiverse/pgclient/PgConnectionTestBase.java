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

import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */

@RunWith(VertxUnitRunner.class)
public abstract class PgConnectionTestBase extends PgClientTestBase<PgConnection> {

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
  public void testTx(TestContext ctx) {
    Async async = ctx.async();
    connector.accept(ctx.asyncAssertSuccess(conn -> {
      conn.query("BEGIN", ctx.asyncAssertSuccess(result1 -> {
        ctx.assertEquals(0, result1.size());
        ctx.assertNotNull(result1.iterator());
        conn.query("COMMIT", ctx.asyncAssertSuccess(result2 -> {
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
  public void testUpdateError(TestContext ctx) {
    Async async = ctx.async();
    connector.accept(ctx.asyncAssertSuccess(conn -> {
      conn.query("INSERT INTO Fortune (id, message) VALUES (1, 'Duplicate')", ctx.asyncAssertFailure(err -> {
        ctx.assertEquals("23505", ((PgException) err).getCode());
        conn.query("SELECT 1000", ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(1, result.size());
          ctx.assertEquals(1000, result.iterator().next().getInteger(0));
          async.complete();
        }));
      }));
    }));
  }

  @Test
  public void testBatchInsertError(TestContext ctx) throws Exception {
    Async async = ctx.async();
    connector.accept(ctx.asyncAssertSuccess(conn -> {
      int id = randomWorld();
      List<Tuple> batch = new ArrayList<>();
      batch.add(Tuple.of(id, 3));
      conn.preparedBatch("INSERT INTO World (id, randomnumber) VALUES ($1, $2)", batch, ctx.asyncAssertFailure(err -> {
        ctx.assertEquals("23505", ((PgException) err).getCode());
        conn.query("SELECT 1000", ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(1, result.size());
          ctx.assertEquals(1000, result.iterator().next().getInteger(0));
          async.complete();
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
    testTransactionCommit(ctx, Runnable::run);
  }

  @Test
  public void testTransactionCommitFromAnotherThread(TestContext ctx) {
    testTransactionCommit(ctx, t -> new Thread(t).start());
  }

  private void testTransactionCommit(TestContext ctx, Executor exec) {
    Async done = ctx.async();
    connector.accept(ctx.asyncAssertSuccess(conn -> {
      deleteFromTestTable(ctx, conn, () -> {
        exec.execute(() -> {
          PgTransaction tx = conn.begin();
          AtomicInteger u1 = new AtomicInteger();
          AtomicInteger u2 = new AtomicInteger();
          conn.query("INSERT INTO Test (id, val) VALUES (1, 'val-1')", ctx.asyncAssertSuccess(res1 -> {
            u1.addAndGet(res1.rowCount());
            exec.execute(() -> {
              conn.query("INSERT INTO Test (id, val) VALUES (2, 'val-2')", ctx.asyncAssertSuccess(res2 -> {
                u2.addAndGet(res2.rowCount());
                exec.execute(() -> {
                  tx.commit(ctx.asyncAssertSuccess(v -> {
                    ctx.assertEquals(1, u1.get());
                    ctx.assertEquals(1, u2.get());
                    conn.query("SELECT id FROM Test WHERE id=1 OR id=2", ctx.asyncAssertSuccess(result -> {
                      ctx.assertEquals(2, result.size());
                      done.complete();
                    }));
                  }));
                });
              }));
            });
          }));
        });
      });
    }));
  }

  @Test
  public void testTransactionRollback(TestContext ctx) {
    testTransactionRollback(ctx, Runnable::run);
  }

  @Test
  public void testTransactionRollbackFromAnotherThread(TestContext ctx) {
    testTransactionRollback(ctx, t -> new Thread(t).start());
  }

  private void testTransactionRollback(TestContext ctx, Executor exec) {
    Async done = ctx.async();
    connector.accept(ctx.asyncAssertSuccess(conn -> {
      deleteFromTestTable(ctx, conn, () -> {
        exec.execute(() -> {
          PgTransaction tx = conn.begin();
          AtomicInteger u1 = new AtomicInteger();
          AtomicInteger u2 = new AtomicInteger();
          conn.query("INSERT INTO Test (id, val) VALUES (1, 'val-1')", ctx.asyncAssertSuccess(res1 -> {
            u1.addAndGet(res1.rowCount());
            exec.execute(() -> {

            });
            conn.query("INSERT INTO Test (id, val) VALUES (2, 'val-2')", ctx.asyncAssertSuccess(res2 -> {
              u2.addAndGet(res2.rowCount());
              exec.execute(() -> {
                tx.rollback(ctx.asyncAssertSuccess(v -> {
                  ctx.assertEquals(1, u1.get());
                  ctx.assertEquals(1, u2.get());
                  conn.query("SELECT id FROM Test WHERE id=1 OR id=2", ctx.asyncAssertSuccess(result -> {
                    ctx.assertEquals(0, result.size());
                    done.complete();
                  }));
                }));
              });
            }));
          }));
        });
      });
    }));
  }

  @Test
  public void testTransactionAbort(TestContext ctx) {
    Async done = ctx.async();
    connector.accept(ctx.asyncAssertSuccess(conn -> {
      deleteFromTestTable(ctx, conn, () -> {
        PgTransaction tx = conn.begin();
        AtomicInteger failures = new AtomicInteger();
        tx.abortHandler(v -> ctx.assertEquals(0, failures.getAndIncrement()));
        AtomicReference<AsyncResult<PgRowSet>> queryAfterFailed = new AtomicReference<>();
        AtomicReference<AsyncResult<Void>> commit = new AtomicReference<>();
        conn.query("INSERT INTO Test (id, val) VALUES (1, 'val-1')", ar1 -> { });
        conn.query("INSERT INTO Test (id, val) VALUES (1, 'val-2')", ar2 -> {
          ctx.assertNotNull(queryAfterFailed.get());
          ctx.assertTrue(queryAfterFailed.get().failed());
          ctx.assertNotNull(commit.get());
          ctx.assertTrue(commit.get().failed());
          ctx.assertTrue(ar2.failed());
          ctx.assertEquals(1, failures.get());
          // This query won't be made in the same TX
          conn.query("SELECT id FROM Test WHERE id=1", ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(0, result.size());
            done.complete();
          }));
        });
        conn.query("SELECT id FROM Test", queryAfterFailed::set);
        tx.commit(commit::set);
      });
    }));
  }
}
