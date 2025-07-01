package io.vertx.sqlclient.spi;

import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.internal.CloseFuture;
import io.vertx.core.internal.ContextInternal;
import io.vertx.core.internal.VertxInternal;
import io.vertx.core.net.NetClientOptions;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.SqlConnectOptions;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.spi.connection.Connection;
import io.vertx.sqlclient.impl.pool.CloseablePool;
import io.vertx.sqlclient.impl.pool.PoolImpl;
import io.vertx.sqlclient.internal.SqlConnectionBase;
import io.vertx.sqlclient.internal.SqlConnectionInternal;
import io.vertx.sqlclient.spi.connection.ConnectionFactory;

import java.util.function.Function;
import java.util.function.Supplier;

public abstract class GenericDriver<O extends SqlConnectOptions> implements Driver<O> {

  private static final String SHARED_CLIENT_KEY_PREFIX = "__vertx.shared.";

  private final String sharedClientKey = SHARED_CLIENT_KEY_PREFIX + "." + discriminant();

  protected abstract String discriminant();

  private final Function<Connection, Future<Void>> afterAcquire;
  private final Function<Connection, Future<Void>> beforeRecycle;

  public GenericDriver() {
    this.afterAcquire = null;
    this.beforeRecycle = null;
  }

  public GenericDriver(Function<Connection, Future<Void>> afterAcquire, Function<Connection, Future<Void>> beforeRecycle) {
    this.afterAcquire = afterAcquire;
    this.beforeRecycle = beforeRecycle;
  }

  /**
   * Create a connection factory to the given {@code database}.
   *
   * @param vertx            the Vertx instance
   * @param transportOptions the options to configure the TCP client
   * @return the connection factory
   */
  public abstract ConnectionFactory<O> createConnectionFactory(Vertx vertx, NetClientOptions transportOptions);

  /**
   * Wrap a given {@code connection} into a {@link SqlConnectionInternal}. The default implementation
   * wraps with a generic {@link SqlConnectionBase}.
   *
   * @param context the connection context
   * @param factory the connection factory
   * @param connection the connection to wrap
   * @return the wrapped connection
   */
  public SqlConnectionInternal wrapConnection(ContextInternal context, ConnectionFactory<O> factory, Connection connection) {
    return new SqlConnectionBase<>(context, factory, connection, this);
  }

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
    PoolImpl pool = new PoolImpl(vertx, this, false, poolOptions, afterAcquire, beforeRecycle,
      factory, databases, connectHandler, this::wrapConnection, closeFuture);
    pool.init();
    closeFuture.add(factory);
    return pool;
  }
}
