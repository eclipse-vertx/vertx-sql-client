package io.vertx.pgclient.spi;

import io.vertx.core.Vertx;
import io.vertx.core.impl.CloseFuture;
import io.vertx.core.impl.ContextInternal;
import io.vertx.core.impl.VertxInternal;
import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.metrics.ClientMetrics;
import io.vertx.core.spi.metrics.VertxMetrics;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgPool;
import io.vertx.pgclient.impl.PgConnectionFactory;
import io.vertx.pgclient.impl.PgConnectionImpl;
import io.vertx.pgclient.impl.PgConnectionUriParser;
import io.vertx.pgclient.impl.PgPoolImpl;
import io.vertx.pgclient.impl.PgPoolOptions;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.SqlConnectOptions;
import io.vertx.sqlclient.impl.Connection;
import io.vertx.sqlclient.impl.PoolImpl;
import io.vertx.sqlclient.impl.SqlConnectionInternal;
import io.vertx.sqlclient.impl.tracing.QueryTracer;
import io.vertx.sqlclient.spi.Driver;
import io.vertx.sqlclient.spi.ConnectionFactory;

import java.util.List;
import java.util.stream.Collectors;

public class PgDriver implements Driver {

  private static final String SHARED_CLIENT_KEY = "__vertx.shared.pgclient";

  public static final PgDriver INSTANCE = new PgDriver();

  @Override
  public PgPool newPool(Vertx vertx, List<? extends SqlConnectOptions> databases, PoolOptions options, CloseFuture closeFuture) {
    VertxInternal vx = (VertxInternal) vertx;
    PoolImpl pool;
    if (options.isShared()) {
      pool = vx.createSharedClient(SHARED_CLIENT_KEY, options.getName(), closeFuture, cf -> newPoolImpl(vx, databases, options, cf));
    } else {
      pool = newPoolImpl(vx, databases, options, closeFuture);
    }
    return new PgPoolImpl(vx, closeFuture, pool);
  }

  private PoolImpl newPoolImpl(VertxInternal vertx, List<? extends SqlConnectOptions> databases, PoolOptions options, CloseFuture closeFuture) {
    PgConnectOptions baseConnectOptions = PgConnectOptions.wrap(databases.get(0));
    QueryTracer tracer = vertx.tracer() == null ? null : new QueryTracer(vertx.tracer(), baseConnectOptions);
    VertxMetrics vertxMetrics = vertx.metricsSPI();
    ClientMetrics metrics = vertxMetrics != null ? vertxMetrics.createClientMetrics(baseConnectOptions.getSocketAddress(), "sql", baseConnectOptions.getMetricsName()) : null;
    boolean pipelinedPool = options instanceof PgPoolOptions && ((PgPoolOptions) options).isPipelined();
    int pipeliningLimit = pipelinedPool ? baseConnectOptions.getPipeliningLimit() : 1;
    PoolImpl pool = new PoolImpl(vertx, this, baseConnectOptions, null, tracer, metrics, pipeliningLimit, options, closeFuture);
    List<ConnectionFactory> lst = databases.stream().map(o -> createConnectionFactory(vertx, o)).collect(Collectors.toList());
    ConnectionFactory factory = ConnectionFactory.roundRobinSelector(lst);
    pool.connectionProvider(factory::connect);
    pool.init();
    closeFuture.add(factory);
    return pool;
  }

  @Override
  public PgConnectOptions parseConnectionUri(String uri) {
    JsonObject conf = PgConnectionUriParser.parse(uri, false);
    return conf == null ? null : new PgConnectOptions(conf);
  }

  @Override
  public boolean acceptsOptions(SqlConnectOptions options) {
    return options instanceof PgConnectOptions || SqlConnectOptions.class.equals(options.getClass());
  }

  @Override
  public ConnectionFactory createConnectionFactory(Vertx vertx, SqlConnectOptions database) {
    return new PgConnectionFactory((VertxInternal) vertx, PgConnectOptions.wrap(database));
  }

  @Override
  public int appendQueryPlaceholder(StringBuilder queryBuilder, int index, int current) {
    queryBuilder.append('$').append(1 + index);
    return index;
  }

  @Override
  public SqlConnectionInternal wrapConnection(ContextInternal context, ConnectionFactory factory, Connection conn, QueryTracer tracer, ClientMetrics metrics) {
    return new PgConnectionImpl((PgConnectionFactory) factory, context, conn, tracer, metrics);
  }
}
