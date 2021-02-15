package io.vertx.clickhouse.clikhousenative.impl;

import io.vertx.clickhouse.clikhousenative.ClickhouseNativeConnectOptions;
import io.vertx.clickhouse.clikhousenative.ClickhouseNativePool;
import io.vertx.core.impl.CloseFuture;
import io.vertx.core.impl.ContextInternal;
import io.vertx.core.impl.EventLoopContext;
import io.vertx.core.spi.metrics.ClientMetrics;
import io.vertx.core.spi.metrics.VertxMetrics;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.impl.Connection;
import io.vertx.sqlclient.impl.ConnectionFactory;
import io.vertx.sqlclient.impl.PoolBase;
import io.vertx.sqlclient.impl.SqlConnectionImpl;
import io.vertx.sqlclient.impl.tracing.QueryTracer;

public class ClickhouseNativePoolImpl extends PoolBase<ClickhouseNativePoolImpl> implements ClickhouseNativePool {
  public static ClickhouseNativePoolImpl create(ContextInternal context, boolean closeVertx,
                                                ClickhouseNativeConnectOptions connectOptions, PoolOptions poolOptions) {
    QueryTracer tracer = context.tracer() == null ? null : new QueryTracer(context.tracer(), connectOptions);
    VertxMetrics vertxMetrics = context.owner().metricsSPI();
    ClientMetrics metrics = vertxMetrics != null ? vertxMetrics.createClientMetrics(connectOptions.getSocketAddress(),
      "sql", connectOptions.getMetricsName()) : null;
    EventLoopContext eventLoopContext = ConnectionFactory.asEventLoopContext(context);
    ClickhouseNativePoolImpl pool = new ClickhouseNativePoolImpl(eventLoopContext,
      new ClickhouseNativeConnectionFactory(eventLoopContext, connectOptions), tracer, metrics, poolOptions);
    CloseFuture closeFuture = pool.closeFuture();
    if (closeVertx) {
      closeFuture.onComplete(ar -> context.owner().close());
    } else {
      context.addCloseHook(closeFuture);
    }
    return pool;
  }

  private final ClickhouseNativeConnectionFactory factory;

  private ClickhouseNativePoolImpl(EventLoopContext context, ClickhouseNativeConnectionFactory factory, QueryTracer tracer,
                                   ClientMetrics metrics, PoolOptions poolOptions) {
    super(context, factory, tracer, metrics, poolOptions);
    this.factory = factory;
  }

  @Override
  protected SqlConnectionImpl wrap(ContextInternal context, Connection conn) {
    return new ClickhouseNativeConnectionImpl(factory, context, conn, tracer, metrics);
  }
}
