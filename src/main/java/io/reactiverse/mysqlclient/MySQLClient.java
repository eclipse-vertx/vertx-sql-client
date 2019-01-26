package io.reactiverse.mysqlclient;

import io.reactiverse.mysqlclient.impl.MySQLConnectionFactory;
import io.reactiverse.mysqlclient.impl.MySQLConnectionImpl;
import io.reactiverse.pgclient.PgResult;
import io.reactiverse.pgclient.PgRowSet;
import io.reactiverse.pgclient.Row;
import io.vertx.core.*;

import java.util.stream.Collector;

@OriginAPI
public interface MySQLClient {
  static void connect(Vertx vertx, MySQLConnectOptions options, Handler<AsyncResult<MySQLConnection>> handler) {
    Context ctx = Vertx.currentContext();
    if (ctx != null) {
      MySQLConnectionFactory connectionFactory = new MySQLConnectionFactory(ctx, options);
      connectionFactory.create(ar -> {
        if (ar.succeeded()) {
          MySQLConnection mySQLConnection = new MySQLConnectionImpl(ctx, ar.result());
          handler.handle(Future.succeededFuture(mySQLConnection));
        } else {
          handler.handle(Future.failedFuture(ar.cause()));
        }
      });
    } else {
      vertx.runOnContext(v -> {
        connect(vertx, options, handler);
      });
    }
  }

  MySQLClient query(String sql, Handler<AsyncResult<PgRowSet>> handler);

  <R> MySQLClient query(String sql, Collector<Row, ?, R> collector, Handler<AsyncResult<PgResult<R>>> handler);

  MySQLClient ping(Handler<AsyncResult<Void>> handler);
}
