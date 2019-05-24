package io.vertx.mysqlclient.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.mysqlclient.MySQLConnection;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.sqlclient.impl.Connection;
import io.vertx.sqlclient.impl.SqlConnectionImpl;

public class MySQLConnectionImpl extends SqlConnectionImpl<MySQLConnectionImpl> implements MySQLConnection {

  public static void connect(Vertx vertx, PgConnectOptions options, Handler<AsyncResult<MySQLConnection>> handler) {
    Context ctx = Vertx.currentContext();
    if (ctx != null) {
      //TODO close hook support
      MySQLConnectionFactory client = new MySQLConnectionFactory(ctx, options);
      client.connect(ar-> {
        if (ar.succeeded()) {
          Connection conn = ar.result();
          MySQLConnectionImpl p = new MySQLConnectionImpl(client, ctx, conn);
          conn.init(p);
          handler.handle(Future.succeededFuture(p));
        } else {
          handler.handle(Future.failedFuture(ar.cause()));
        }
      });
    } else {
      vertx.runOnContext(v -> {
        if (options.isUsingDomainSocket() && !vertx.isNativeTransportEnabled()) {
          handler.handle(Future.failedFuture("Native transport is not available"));
        } else {
          connect(vertx, options, handler);
        }
      });
    }
  }

  private final MySQLConnectionFactory factory;

  public MySQLConnectionImpl(MySQLConnectionFactory factory, Context context, Connection conn) {
    super(context, conn);

    this.factory = factory;
  }

  @Override
  public void handleNotification(int processId, String channel, String payload) {
    throw new UnsupportedOperationException();
  }

  @Override
  public MySQLConnection ping(Handler<AsyncResult<Void>> handler) {
    throw new UnsupportedOperationException();
  }
}
