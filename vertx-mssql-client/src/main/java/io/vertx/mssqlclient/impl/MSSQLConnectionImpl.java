package io.vertx.mssqlclient.impl;

import io.vertx.core.impl.ContextInternal;
import io.vertx.core.impl.PromiseInternal;
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

  public static Future<MSSQLConnection> connect(Vertx vertx, MSSQLConnectOptions options) {
    ContextInternal ctx = (ContextInternal) vertx.getOrCreateContext();
    PromiseInternal<MSSQLConnection> promise = ctx.promise();
    MSSQLConnectionFactory client = new MSSQLConnectionFactory(vertx, options);
    ctx.dispatch(null, v -> {
      client.create(ctx)
        .<MSSQLConnection>map(conn -> {
          MSSQLConnectionImpl msConn = new MSSQLConnectionImpl(client, ctx, conn);
          conn.init(msConn);
          return msConn;
        }).setHandler(promise);
    });
    return promise.future();
  }

  @Override
  public void handleNotification(int processId, String channel, String payload) {
    throw new UnsupportedOperationException();
  }
}
