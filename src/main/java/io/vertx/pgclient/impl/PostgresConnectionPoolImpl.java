package io.vertx.pgclient.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.pgclient.PostgresConnection;
import io.vertx.pgclient.PostgresConnectionPool;
import io.vertx.pgclient.Result;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class PostgresConnectionPoolImpl implements PostgresConnectionPool {

  private final PostgresClientImpl client;
  private final Context context;
  private PostgresConnection[] connections;
  private long index;

  public PostgresConnectionPoolImpl(PostgresClientImpl client) {
    this.context = client.vertx.getOrCreateContext();
    this.client = client;
  }

  void connect(int size, Handler<AsyncResult<PostgresConnectionPool>> completionHandler) {
    if (context == Vertx.currentContext()) {
      if (connections == null) {
        List<Future> list = new ArrayList<>(size);
        for (int i = 0;i < size;i++) {
          Future<PostgresConnection> future = Future.future();
          list.add(future);
          client.connect(future);
        }
        connections = new PostgresConnection[size];
        CompositeFuture fut = CompositeFuture.all(list);
        fut.setHandler(ar -> {
          if (ar.succeeded()) {
            for (int i = 0;i < size;i++) {
              connections[i] = ar.result().resultAt(i);
            }
            completionHandler.handle(ar.map(this));
          } else {
            completionHandler.handle(Future.failedFuture(ar.cause()));
          }
        });
      } else {
        completionHandler.handle(Future.failedFuture("Already connecting"));
      }
    } else {
      context.runOnContext(v -> connect(size, completionHandler));
    }
  }

  @Override
  public void execute(String sql, Handler<AsyncResult<Result>> resultHandler) {
    Context current = Vertx.currentContext();
    if (current == context) {
      int i = (int) index++ % connections.length;
      connections[i].execute(sql, resultHandler);
    } else {
      this.context.runOnContext(v1 -> execute(sql, ar -> {
        current.runOnContext(v2 -> {
          resultHandler.handle(ar);
        });
      }));
    }
  }

  @Override
  public void close() {
    if (Vertx.currentContext() == context) {
      if (connections != null) {
        for (int i = 0;i < connections.length;i++) {
          connections[i].close();
        }
      }
    } else {
      context.runOnContext(v -> close());
    }
  }
}
