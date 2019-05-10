package io.vertx.mysqlclient;

import io.vertx.mysqlclient.impl.MySQLConnectionFactory;
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

public interface MySQLClient {
  static void connect(Vertx vertx, PgConnectOptions options, Handler<AsyncResult<PgConnection>> handler) {
    Context ctx = Vertx.currentContext();
    if (ctx != null) {
      MySQLConnectionFactory connectionFactory = new MySQLConnectionFactory(ctx, options);
      connectionFactory.connect(handler);
    } else {
      vertx.runOnContext(v -> {
        connect(vertx, options, handler);
      });
    }
  }

  MySQLClient query(String sql, Handler<AsyncResult<RowSet>> handler);

  <R> MySQLClient query(String sql, Collector<Row, ?, R> collector, Handler<AsyncResult<SqlResult<R>>> handler);

  MySQLClient ping(Handler<AsyncResult<Void>> handler);
}
