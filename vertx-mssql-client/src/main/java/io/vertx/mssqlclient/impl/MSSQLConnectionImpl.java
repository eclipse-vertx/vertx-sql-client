package io.vertx.mssqlclient.impl;

import io.vertx.core.impl.ContextInternal;
import io.vertx.mssqlclient.MSSQLConnectOptions;
import io.vertx.mssqlclient.MSSQLConnection;
import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.sqlclient.impl.Connection;
import io.vertx.sqlclient.impl.SqlConnectionImpl;

public class MSSQLConnectionImpl extends SqlConnectionImpl<MSSQLConnectionImpl> implements MSSQLConnection {
  private final MSSQLConnectionFactory factory;

  public MSSQLConnectionImpl(MSSQLConnectionFactory factory, ContextInternal context, Connection conn) {
    super(context, conn);
    this.factory = factory;
  }

  public static void connect(Vertx vertx, MSSQLConnectOptions options, Handler<AsyncResult<MSSQLConnection>> handler) {
    ContextInternal ctx = (ContextInternal) Vertx.currentContext();
    if (ctx != null) {
      MSSQLConnectionFactory client = new MSSQLConnectionFactory(vertx, options);
      client.create(ctx)
        .<MSSQLConnection>map(conn -> {
          MSSQLConnectionImpl c = new MSSQLConnectionImpl(client, ctx, conn);
          conn.init(c);
          return c;
        }).setHandler(handler);
    } else {
      vertx.runOnContext(v -> {
        connect(vertx, options, handler);
      });
    }
  }

  @Override
  public void handleNotification(int processId, String channel, String payload) {
    throw new UnsupportedOperationException();
  }
}
