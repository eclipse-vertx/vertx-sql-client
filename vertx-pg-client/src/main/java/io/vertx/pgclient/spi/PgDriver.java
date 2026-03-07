package io.vertx.pgclient.spi;

import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.NetClientOptions;
import io.vertx.core.internal.CloseFuture;
import io.vertx.core.internal.ContextInternal;
import io.vertx.core.internal.VertxInternal;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.TargetServerType;
import io.vertx.pgclient.impl.PgConnectionFactory;
import io.vertx.pgclient.impl.PgConnectionImpl;
import io.vertx.pgclient.impl.PgConnectionUriParser;
import io.vertx.pgclient.impl.PgPoolOptions;
import io.vertx.pgclient.impl.ServerTypeAwarePgConnectionFactory;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.SqlConnectOptions;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.spi.connection.Connection;
import io.vertx.sqlclient.impl.pool.PoolImpl;
import io.vertx.sqlclient.internal.SqlConnectionInternal;
import io.vertx.sqlclient.spi.connection.ConnectionFactory;
import io.vertx.sqlclient.spi.DriverBase;

import java.util.function.Supplier;

public class PgDriver extends DriverBase<PgConnectOptions> {

  private static final String DISCRIMINANT = "pgclient";

  public static final PgDriver INSTANCE = new PgDriver();

  public PgDriver() {
    super(DISCRIMINANT);
  }

  @Override
  protected Pool newPool(VertxInternal vertx, Handler<SqlConnection> connectHandler, Supplier<Future<PgConnectOptions>> databases, PoolOptions poolOptions, NetClientOptions transportOptions, CloseFuture closeFuture) {
    boolean pipelinedPool = poolOptions instanceof PgPoolOptions && ((PgPoolOptions) poolOptions).isPipelined();
    PgConnectionFactory baseFactory = (PgConnectionFactory) createConnectionFactory(vertx, transportOptions);
    ConnectionFactory<PgConnectOptions> factory = baseFactory;
    Supplier<Future<PgConnectOptions>> effectiveDatabases = databases;

    if (poolOptions instanceof PgPoolOptions) {
      PgPoolOptions pgOpts = (PgPoolOptions) poolOptions;
      if (pgOpts.getTargetServerType() != null
        && pgOpts.getTargetServerType() != TargetServerType.ANY
        && pgOpts.getServers() != null && !pgOpts.getServers().isEmpty()) {
        factory = new ServerTypeAwarePgConnectionFactory(baseFactory, pgOpts.getServers(), pgOpts.getTargetServerType());
        // The wrapper ignores the supplier; provide a dummy that returns the first server
        effectiveDatabases = () -> Future.succeededFuture(pgOpts.getServers().get(0));
      }
    }

    PoolImpl pool = new PoolImpl(vertx, this, pipelinedPool, poolOptions, null, null,
      factory, effectiveDatabases, connectHandler, this::wrapConnection, closeFuture);
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
  public PgConnectOptions downcast(SqlConnectOptions connectOptions) {
    return connectOptions instanceof PgConnectOptions ? (PgConnectOptions) connectOptions : new PgConnectOptions(connectOptions);
  }

  @Override
  public ConnectionFactory<PgConnectOptions> createConnectionFactory(Vertx vertx, NetClientOptions transportOptions) {
    return new PgConnectionFactory((VertxInternal) vertx);
  }

  @Override
  public int appendQueryPlaceholder(StringBuilder queryBuilder, int index, int current) {
    queryBuilder.append('$').append(1 + index);
    return index;
  }

  @Override
  public SqlConnectionInternal wrapConnection(ContextInternal context, ConnectionFactory<PgConnectOptions> factory, Connection connection) {
    PgConnectionFactory pgFactory;
    if (factory instanceof ServerTypeAwarePgConnectionFactory) {
      pgFactory = ((ServerTypeAwarePgConnectionFactory) factory).getDelegate();
    } else {
      pgFactory = (PgConnectionFactory) factory;
    }
    return new PgConnectionImpl(pgFactory, context, connection);
  }
}
