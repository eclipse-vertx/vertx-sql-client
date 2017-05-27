package com.julienviet.pgclient.impl;

import com.julienviet.pgclient.PgConnection;
import com.julienviet.pgclient.PgConnectionPool;
import com.julienviet.pgclient.PgPreparedStatement;
import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.UpdateResult;

import java.util.ArrayDeque;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */
class PostgresConnectionPoolImpl implements PgConnectionPool {

  private final PostgresClientImpl client;
  private final Context context;
  private final ArrayDeque<Holder> available = new ArrayDeque<>();
  private final ArrayDeque<Waiter> waiters = new ArrayDeque<>();
  private final int maxSize;
  private int connCount;

  private static class Waiter {

    private final Handler<AsyncResult<PgConnection>> handler;
    private final Context context;

    Waiter(Handler<AsyncResult<PgConnection>> handler, Context context) {
      this.handler = handler;
      this.context = context;
    }

    void use(PgConnection conn) {
      Context current = Vertx.currentContext();
      if (current == context) {
        handler.handle(Future.succeededFuture(conn));
      } else {
        context.runOnContext(v -> {
          use(conn);
        });
      }
    }
  }

  PostgresConnectionPoolImpl(PostgresClientImpl client, int maxSize) {
    this.context = client.vertx.getOrCreateContext();
    this.client = client;
    this.maxSize = maxSize;
  }

  @Override
  public void getConnection(Handler<AsyncResult<PgConnection>> handler) {
    Context current = Vertx.currentContext();
    if (current == context) {
      Holder holder = available.poll();
      if (holder != null) {
        handler.handle(Future.succeededFuture(holder.createProxy()));
      } else if (connCount < maxSize) {
        connCount++;
        openConnection(handler, current);
      } else {
        waiters.add(new Waiter(handler, current));
      }
    } else {
      this.context.runOnContext(v -> getConnection(handler));
    }
  }

  private class Holder {

    private final PgConnection conn;
    private boolean failed;
    private volatile Handler<Throwable> exceptionHandler;
    private volatile Handler<Void> closeHandler;

    private Holder(PgConnection conn) {
      this.conn = conn;
      conn.exceptionHandler(err -> {
        failed = true;
        Handler<Throwable> handler = exceptionHandler;
        if (handler != null) {
          handler.handle(err);
        }
      });
      conn.closeHandler(v -> {
        Handler<Void> handler = closeHandler;
        removeFromPool();
        if (handler != null) {
          handler.handle(null);
        }
      });
    }

    private void proxyClosed() {
      if (Vertx.currentContext() == context) {
        if (!failed) {
          returnToPool(this);
        }
      } else {
        context.runOnContext(v -> proxyClosed());
      }
    }

    PgConnection createProxy() {
      return new PgConnection() {
        final AtomicBoolean closed = new AtomicBoolean();

        @Override
        public void execute(String sql, Handler<AsyncResult<ResultSet>> handler) {
          if (!closed.get()) {
            conn.execute(sql, handler);
          }
        }

        @Override
        public void update(String sql, Handler<AsyncResult<UpdateResult>> handler) {
          if (!closed.get()) {
            conn.update(sql, handler);
          }
        }

        @Override
        public void query(String sql, Handler<AsyncResult<ResultSet>> handler) {
          if (!closed.get()) {
            conn.query(sql, handler);
          }
        }

        @Override
        public void prepareAndQuery(String sql, Object param, Handler<AsyncResult<ResultSet>> handler) {
          if (!closed.get()) {
            conn.prepareAndQuery(sql, param, handler);
          }
        }

        @Override
        public void prepareAndQuery(String sql, Object param1, Object param2, Handler<AsyncResult<ResultSet>> handler) {
          if (!closed.get()) {
            conn.prepareAndQuery(sql, param1, param2, handler);
          }
        }

        @Override
        public void prepareAndQuery(String sql, Object param1, Object param2, Object param3,
                                    Handler<AsyncResult<ResultSet>> handler) {
          if (!closed.get()) {
            conn.prepareAndQuery(sql, param1, param2, param3, handler);
          }
        }

        @Override
        public void prepareAndQuery(String sql, Object param1, Object param2, Object param3, Object param4,
                                    Handler<AsyncResult<ResultSet>> handler) {
          if (!closed.get()) {
            conn.prepareAndQuery(sql, param1, param2, param3, param4, handler);
          }
        }

        @Override
        public void prepareAndQuery(String sql, Object param1, Object param2, Object param3, Object param4,
                                    Object param5, Handler<AsyncResult<ResultSet>> handler) {
          if (!closed.get()) {
            conn.prepareAndQuery(sql, param1, param2, param3, param4, param5, handler);
          }
        }

        @Override
        public void prepareAndQuery(String sql, Object param1, Object param2, Object param3, Object param4,
                                    Object param5, Object param6, Handler<AsyncResult<ResultSet>> handler) {
          if (!closed.get()) {
            conn.prepareAndQuery(sql, param1, param2, param3, param4, param5, param6, handler);
          }
        }

        @Override
        public void prepareAndQuery(String sql, List<Object> params, Handler<AsyncResult<ResultSet>> handler) {
          if (!closed.get()) {
            conn.prepareAndQuery(sql, params, handler);
          }
        }

        @Override
        public void prepareAndExecute(String sql, Object param, Handler<AsyncResult<UpdateResult>> handler) {
          if (!closed.get()) {
            conn.prepareAndExecute(sql, param, handler);
          }
        }

        @Override
        public void prepareAndExecute(String sql, Object param1, Object param2, Handler<AsyncResult<UpdateResult>> handler) {
          if (!closed.get()) {
            conn.prepareAndExecute(sql, param1, param2, handler);
          }
        }

        @Override
        public void prepareAndExecute(String sql, Object param1, Object param2, Object param3, Handler<AsyncResult<UpdateResult>> handler) {
          if (!closed.get()) {
            conn.prepareAndExecute(sql, param1, param2, param3, handler);
          }
        }

        @Override
        public void prepareAndExecute(String sql, Object param1, Object param2, Object param3, Object param4, Handler<AsyncResult<UpdateResult>> handler) {
          if (!closed.get()) {
            conn.prepareAndExecute(sql, param1, param2, param3, param4, handler);
          }
        }

        @Override
        public void prepareAndExecute(String sql, Object param1, Object param2, Object param3, Object param4, Object param5, Handler<AsyncResult<UpdateResult>> handler) {
          if (!closed.get()) {
            conn.prepareAndExecute(sql, param1, param2, param3, param4, param5, handler);
          }
        }

        @Override
        public void prepareAndExecute(String sql, Object param1, Object param2, Object param3, Object param4, Object param5, Object param6, Handler<AsyncResult<UpdateResult>> handler) {
          if (!closed.get()) {
            conn.prepareAndExecute(sql, param1, param2, param3, param4, param5, param6, handler);
          }
        }

        @Override
        public void prepareAndExecute(String sql, List<Object> params, Handler<AsyncResult<UpdateResult>> handler) {
          if (!closed.get()) {
            conn.prepareAndExecute(sql, params, handler);
          }
        }

        @Override
        public void exceptionHandler(Handler<Throwable> handler) {
          if (!closed.get()) {
            exceptionHandler = handler;
          }
        }

        @Override
        public void closeHandler(Handler<Void> handler) {
          if (!closed.get()) {
            closeHandler = handler;
          }
        }

        @Override
        public PgPreparedStatement prepare(String sql) {
          throw new UnsupportedOperationException("Implement me");
        }

        @Override
        public void close() {
          if (closed.compareAndSet(false, true)) {
            closeHandler = null;
            exceptionHandler = null;
            proxyClosed();
          }
        }
      };
    }
  }

  private void openConnection(Handler<AsyncResult<PgConnection>> handler, Context handlerContext) {
    client.connect(ar -> {
      Future<PgConnection> result;
      if (ar.succeeded()) {
        PgConnection conn = ar.result();
        result = Future.succeededFuture(new Holder(conn).createProxy());
      } else {
        result = Future.failedFuture(ar.cause());
        connCount--;
      }
      if (Vertx.currentContext() == handlerContext) {
        handler.handle(result);
      } else {
        handlerContext.runOnContext(v -> handler.handle(result));
      }
    });
  }

  private void removeFromPool() {
    Waiter waiter = waiters.poll();
    if (waiter != null) {
      openConnection(waiter.handler, waiter.context);
    } else {
      connCount--;
    }
  }

  private void returnToPool(Holder holder) {
    Waiter waiter = waiters.poll();
    if (waiter != null) {
      waiter.use(holder.createProxy());
    } else {
      available.add(holder);
    }
  }

  @Override
  public void close() {
    throw new UnsupportedOperationException();
  }
}
