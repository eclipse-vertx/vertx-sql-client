package io.vertx.sqlclient.spi;

import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.internal.CloseFuture;
import io.vertx.core.internal.VertxInternal;
import io.vertx.core.net.NetClientOptions;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.SqlConnectOptions;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.internal.pool.CloseablePool;
import io.vertx.sqlclient.internal.pool.PoolImpl;

import java.util.function.Supplier;

public abstract class GenericDriver<O extends SqlConnectOptions> implements Driver<O> {

  private static final String SHARED_CLIENT_KEY_PREFIX = "__vertx.shared.";

  private final String sharedClientKey = SHARED_CLIENT_KEY_PREFIX + "." + discriminant();

  protected abstract String discriminant();

  @Override
  public Pool newPool(Vertx vertx, Supplier<Future<O>> databases, PoolOptions options, NetClientOptions transportOptions, Handler<SqlConnection> connectHandler, CloseFuture closeFuture) {
    VertxInternal vx = (VertxInternal) vertx;
    Pool pool;
    if (options.isShared()) {
      pool = vx.createSharedResource(sharedClientKey, options.getName(), closeFuture, cf -> newPool(vx, connectHandler, databases, options, transportOptions, cf));
    } else {
      pool = newPool(vx, connectHandler, databases, options, transportOptions, closeFuture);
    }
    return new CloseablePool(vx, closeFuture, pool);
  }

  protected Pool newPool(VertxInternal vertx, Handler<SqlConnection> connectHandler, Supplier<Future<O>> databases, PoolOptions poolOptions, NetClientOptions transportOptions, CloseFuture closeFuture) {
    ConnectionFactory<O> factory = createConnectionFactory(vertx, transportOptions);
    PoolImpl pool = new PoolImpl(vertx, this, false, poolOptions, null, null,
      factory, databases, connectHandler, closeFuture);
    pool.init();
    closeFuture.add(factory);
    return pool;
  }
}
