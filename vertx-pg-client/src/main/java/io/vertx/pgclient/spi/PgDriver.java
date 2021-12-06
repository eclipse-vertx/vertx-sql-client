package io.vertx.pgclient.spi;

import io.vertx.core.Vertx;
import io.vertx.core.impl.CloseFuture;
import io.vertx.core.impl.VertxInternal;
import io.vertx.core.spi.metrics.ClientMetrics;
import io.vertx.core.spi.metrics.VertxMetrics;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgPool;
import io.vertx.pgclient.impl.PgConnectionFactory;
import io.vertx.pgclient.impl.PgPoolImpl;
import io.vertx.pgclient.impl.PgPoolOptions;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.SqlConnectOptions;
import io.vertx.sqlclient.impl.tracing.QueryTracer;
import io.vertx.sqlclient.spi.Driver;
import io.vertx.sqlclient.spi.ConnectionFactory;

import java.util.List;
import java.util.stream.Collectors;

public class PgDriver implements Driver {

  public static final PgDriver INSTANCE = new PgDriver();

  @Override
  public PgPool newPool(Vertx vertx, List<? extends SqlConnectOptions> databases, PoolOptions options, CloseFuture closeFuture) {
    VertxInternal vx = (VertxInternal) vertx;
    PgConnectOptions baseConnectOptions = PgConnectOptions.wrap(databases.get(0));
    QueryTracer tracer = vx.tracer() == null ? null : new QueryTracer(vx.tracer(), baseConnectOptions);
    VertxMetrics vertxMetrics = vx.metricsSPI();
    ClientMetrics metrics = vertxMetrics != null ? vertxMetrics.createClientMetrics(baseConnectOptions.getSocketAddress(), "sql", baseConnectOptions.getMetricsName()) : null;
    boolean pipelinedPool = options instanceof PgPoolOptions && ((PgPoolOptions) options).isPipelined();
    int pipeliningLimit = pipelinedPool ? baseConnectOptions.getPipeliningLimit() : 1;
    PgPoolImpl pool = new PgPoolImpl(vx, baseConnectOptions, tracer, metrics, pipeliningLimit, options, closeFuture);
    List<ConnectionFactory> lst = databases.stream().map(o -> createConnectionFactory(vx, o)).collect(Collectors.toList());
    ConnectionFactory factory = ConnectionFactory.roundRobinSelector(lst);
    pool.connectionProvider(factory::connect);
    pool.init();
    closeFuture.add(factory);
    return pool;
  }

  @Override
  public boolean acceptsOptions(SqlConnectOptions options) {
    return options instanceof PgConnectOptions || SqlConnectOptions.class.equals(options.getClass());
  }

  @Override
  public ConnectionFactory createConnectionFactory(Vertx vertx, SqlConnectOptions database) {
    return new PgConnectionFactory((VertxInternal) vertx, PgConnectOptions.wrap(database));
  }
}
