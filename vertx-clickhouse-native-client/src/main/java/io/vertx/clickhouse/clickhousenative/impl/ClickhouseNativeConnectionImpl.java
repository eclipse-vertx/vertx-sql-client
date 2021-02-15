package io.vertx.clickhouse.clikhousenative.impl;

import io.vertx.clickhouse.clikhousenative.ClickhouseNativeConnectOptions;
import io.vertx.clickhouse.clikhousenative.ClickhouseNativeConnection;
import io.vertx.core.Future;
import io.vertx.core.impl.ContextInternal;
import io.vertx.core.impl.future.PromiseInternal;
import io.vertx.core.spi.metrics.ClientMetrics;
import io.vertx.sqlclient.impl.Connection;
import io.vertx.sqlclient.impl.ConnectionFactory;
import io.vertx.sqlclient.impl.SqlConnectionImpl;
import io.vertx.sqlclient.impl.tracing.QueryTracer;

public class ClickhouseNativeConnectionImpl extends SqlConnectionImpl<ClickhouseNativeConnectionImpl> implements ClickhouseNativeConnection {
  private final ClickhouseNativeConnectionFactory factory;

  public static Future<ClickhouseNativeConnection> connect(ContextInternal ctx, ClickhouseNativeConnectOptions options) {
    ClickhouseNativeConnectionFactory client;
    try {
      client = new ClickhouseNativeConnectionFactory(ConnectionFactory.asEventLoopContext(ctx), options);
    } catch (Exception e) {
      return ctx.failedFuture(e);
    }
    ctx.addCloseHook(client);
    QueryTracer tracer = ctx.tracer() == null ? null : new QueryTracer(ctx.tracer(), options);
    PromiseInternal<Connection> promise = ctx.promise();
    client.connect(promise);
    return promise.future().map(conn -> {
      ClickhouseNativeConnectionImpl mySQLConnection = new ClickhouseNativeConnectionImpl(client, ctx, conn, tracer, null);
      conn.init(mySQLConnection);
      return mySQLConnection;
    });
  }

  ClickhouseNativeConnectionImpl(ClickhouseNativeConnectionFactory factory, ContextInternal context, Connection conn, QueryTracer tracer, ClientMetrics metrics) {
    super(context, conn, tracer, metrics);

    this.factory = factory;
  }
}
