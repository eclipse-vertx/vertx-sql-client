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
import io.vertx.pgclient.impl.PgConnectionFactory;
import io.vertx.pgclient.impl.PgConnectionImpl;
import io.vertx.pgclient.impl.PgConnectionUriParser;
import io.vertx.pgclient.impl.PgPoolOptions;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.SqlConnectOptions;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.internal.Connection;
import io.vertx.sqlclient.internal.pool.CloseablePool;
import io.vertx.sqlclient.internal.pool.PoolImpl;
import io.vertx.sqlclient.internal.SqlConnectionInternal;
import io.vertx.sqlclient.spi.ConnectionFactory;
import io.vertx.sqlclient.spi.Driver;

import java.util.function.Supplier;

public class PgDriver implements Driver<PgConnectOptions> {

  private static final String SHARED_CLIENT_KEY = "__vertx.shared.pgclient";

  public static final PgDriver INSTANCE = new PgDriver();

  @Override
  public Pool newPool(Vertx vertx, Supplier<Future<PgConnectOptions>> databases, PoolOptions poolOptions, NetClientOptions transportOptions, Handler<SqlConnection> connectHandler, CloseFuture closeFuture) {
    VertxInternal vx = (VertxInternal) vertx;
    PoolImpl pool;
    if (poolOptions.isShared()) {
      pool = vx.createSharedResource(SHARED_CLIENT_KEY, poolOptions.getName(), closeFuture, cf -> newPoolImpl(vx, connectHandler, databases, poolOptions, transportOptions, cf));
    } else {
      pool = newPoolImpl(vx, connectHandler, databases, poolOptions, transportOptions, closeFuture);
    }
    return new CloseablePool(vx, closeFuture, pool);
  }

  private PoolImpl newPoolImpl(VertxInternal vertx, Handler<SqlConnection> connectHandler, Supplier<Future<PgConnectOptions>> databases, PoolOptions poolOptions, NetClientOptions transportOptions, CloseFuture closeFuture) {
    boolean pipelinedPool = poolOptions instanceof PgPoolOptions && ((PgPoolOptions) poolOptions).isPipelined();
    ConnectionFactory<PgConnectOptions> factory = createConnectionFactory(vertx, transportOptions);
    PoolImpl pool = new PoolImpl(vertx, this, pipelinedPool, poolOptions, null, null, context -> factory.connect(context, databases.get()), connectHandler, closeFuture);
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
  public SqlConnectionInternal wrapConnection(ContextInternal context, ConnectionFactory<PgConnectOptions> factory, Connection conn) {
    return new PgConnectionImpl((PgConnectionFactory) factory, context, conn);
  }
}
