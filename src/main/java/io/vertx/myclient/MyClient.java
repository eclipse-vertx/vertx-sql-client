package io.vertx.myclient;

import io.vertx.myclient.impl.MyConnectionFactory;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgConnection;
import io.vertx.sqlclient.SqlResult;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Row;
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
