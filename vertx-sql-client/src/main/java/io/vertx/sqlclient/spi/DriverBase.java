package io.vertx.sqlclient.spi;

import io.vertx.core.*;
import io.vertx.core.internal.ContextInternal;
import io.vertx.core.internal.VertxInternal;
import io.vertx.core.net.NetClientOptions;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.SqlConnectOptions;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.internal.PoolInternal;
import io.vertx.sqlclient.spi.connection.Connection;
import io.vertx.sqlclient.impl.pool.PoolImpl;
import io.vertx.sqlclient.internal.SqlConnectionBase;
import io.vertx.sqlclient.internal.SqlConnectionInternal;
import io.vertx.sqlclient.spi.connection.ConnectionFactory;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A generic driver.
 * @param <O>
 */
public abstract class DriverBase<O extends SqlConnectOptions> implements Driver<O> {

  private static final String SHARED_CLIENT_KEY_PREFIX = "__vertx.shared.";


  private final String discriminant;
  private final String sharedClientKey;
  private final Function<Connection, Future<Void>> afterAcquire;
  private final Function<Connection, Future<Void>> beforeRecycle;

  public DriverBase(String discriminant) {
    this(discriminant, null, null);
  }

  public DriverBase(String discriminant, Function<Connection, Future<Void>> afterAcquire, Function<Connection, Future<Void>> beforeRecycle) {
    this.afterAcquire = afterAcquire;
    this.beforeRecycle = beforeRecycle;
    this.discriminant = discriminant;
    this.sharedClientKey = SHARED_CLIENT_KEY_PREFIX + "." + discriminant;
  }

  @Override
  public String sharedClientKey() {
    return sharedClientKey;
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
  public PoolInternal newPool(Vertx vertx, Supplier<Future<O>> databases, PoolOptions options, NetClientOptions transportOptions, Handler<SqlConnection> connectHandler) {
    return newPool((VertxInternal) vertx, connectHandler, databases, options, transportOptions);
  }

  protected PoolInternal newPool(VertxInternal vertx, Handler<SqlConnection> connectHandler, Supplier<Future<O>> databases, PoolOptions poolOptions, NetClientOptions transportOptions) {
    ConnectionFactory<O> factory = createConnectionFactory(vertx, transportOptions);
    PoolImpl pool = new PoolImpl(vertx, this, false, poolOptions, afterAcquire, beforeRecycle,
      factory, databases, connectHandler, this::wrapConnection) {
      @Override
      protected Future<Void> closeImpl() {
        return super.closeImpl().eventually(factory::close);
      }
    };
    pool.init();
    return pool;
  }
}
