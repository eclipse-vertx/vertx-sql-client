package io.reactiverse.myclient;

import io.reactiverse.myclient.impl.MyConnectionFactory;
import io.reactiverse.pgclient.PgConnectOptions;
import io.reactiverse.pgclient.PgConnection;
import io.reactiverse.sqlclient.SqlResult;
import io.reactiverse.sqlclient.RowSet;
import io.reactiverse.sqlclient.Row;
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

  MyClient query(String sql, Handler<AsyncResult<RowSet>> handler);

  <R> MyClient query(String sql, Collector<Row, ?, R> collector, Handler<AsyncResult<SqlResult<R>>> handler);

  MyClient ping(Handler<AsyncResult<Void>> handler);
}
