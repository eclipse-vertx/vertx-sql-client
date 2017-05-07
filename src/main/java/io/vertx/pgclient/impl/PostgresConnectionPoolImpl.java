package io.vertx.pgclient.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.pgclient.PostgresConnection;
import io.vertx.pgclient.PostgresConnectionPool;
import io.vertx.pgclient.Result;

import java.util.ArrayDeque;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
class PostgresConnectionPoolImpl implements PostgresConnectionPool {

  private final PostgresClientImpl client;
  private final Context context;
  private final ArrayDeque<PostgresConnection> available = new ArrayDeque<>();
  private final ArrayDeque<Waiter> waiters = new ArrayDeque<>();
  private final int maxSize;
  private int connCount;

  private static class Waiter {

    private final Handler<AsyncResult<PostgresConnection>> handler;
    private final Context context;

    Waiter(Handler<AsyncResult<PostgresConnection>> handler, Context context) {
      this.handler = handler;
      this.context = context;
    }

    void use(PostgresConnection conn) {
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
  public void getConnection(Handler<AsyncResult<PostgresConnection>> handler) {
    Context current = Vertx.currentContext();
    if (current == context) {
      PostgresConnection conn = available.poll();
      if (conn != null) {
        handler.handle(Future.succeededFuture(new Proxy(conn)));
      } else if (connCount < maxSize) {
        openConnection(handler);
      } else {
        waiters.add(new Waiter(handler, current));
      }
    } else {
      this.context.runOnContext(v -> getConnection(handler));
    }
  }

  private class Proxy implements PostgresConnection {

    final PostgresConnection conn;
    final AtomicBoolean closed = new AtomicBoolean();

    public Proxy(PostgresConnection conn) {
      this.conn = conn;
    }

    @Override
    public void execute(String sql, Handler<AsyncResult<Result>> handler) {
      conn.execute(sql, handler);
    }

    @Override
    public void exceptionHandler(Handler<Throwable> handler) {
    }

    @Override
    public void closeHandler(Handler<Void> handler) {
      throw new UnsupportedOperationException("todo");
    }

    @Override
    public void close() {
      if (closed.compareAndSet(false, true)) {
        returnToPool(conn);
      }
    }
  }

  private void openConnection(Handler<AsyncResult<PostgresConnection>> handler) {
    connCount++;
    client.connect(ar -> {
      if (ar.succeeded()) {
        handler.handle(Future.succeededFuture(new Proxy(ar.result())));
      } else {
        // number of retry should be bounded
        openConnection(handler);
      }
    });
  }

  private void returnToPool(PostgresConnection conn) {
    Waiter waiter = waiters.poll();
    if (waiter != null) {
      waiter.use(new Proxy(conn));
    } else {
      available.add(conn);
    }
  }

  @Override
  public void close() {
    throw new UnsupportedOperationException();
  }
}
