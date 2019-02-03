package io.reactiverse.pgclient;

import io.reactiverse.pgclient.impl.my.MyConnectionFactory;
import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;

import java.util.stream.Collector;

public interface MyClient {
  static void connect(Vertx vertx, PgConnectOptions options, Handler<AsyncResult<PgConnection>> handler) {
    Context ctx = Vertx.currentContext();
    if (ctx != null) {
      MyConnectionFactory connectionFactory = new MyConnectionFactory(ctx, options);
      connectionFactory.connect(handler);
    } else {
      vertx.runOnContext(v -> {
        connect(vertx, options, handler);
      });
    }
  }

  MyClient query(String sql, Handler<AsyncResult<PgRowSet>> handler);

  <R> MyClient query(String sql, Collector<Row, ?, R> collector, Handler<AsyncResult<PgResult<R>>> handler);

  MyClient ping(Handler<AsyncResult<Void>> handler);
}
