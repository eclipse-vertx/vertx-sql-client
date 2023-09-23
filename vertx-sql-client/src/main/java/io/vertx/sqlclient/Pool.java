/*
 * Copyright (C) 2017 Julien Viet
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.vertx.sqlclient;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.Nullable;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.impl.ContextInternal;
import io.vertx.core.net.NetClientOptions;
import io.vertx.sqlclient.impl.PoolImpl;
import io.vertx.sqlclient.impl.Utils;
import io.vertx.sqlclient.spi.Driver;

import java.util.function.Function;
import java.util.function.Supplier;

import static io.vertx.sqlclient.impl.PoolImpl.startPropagatableConnection;

/**
 * A connection pool which reuses a number of SQL connections.
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@VertxGen
public interface Pool extends SqlClient {

  /**
   * Like {@link #pool(SqlConnectOptions, PoolOptions)} with default options.
   */
  static Pool pool(SqlConnectOptions connectOptions) {
    return pool(connectOptions, new PoolOptions());
  }

  /**
   * Like {@link #pool(Vertx, SqlConnectOptions, PoolOptions)} with a Vert.x instance created automatically.
   */
  static Pool pool(SqlConnectOptions database, PoolOptions options) {
    return pool(null, database, options);
  }

  /**
   * Create a connection pool to the {@code database} with the given {@code options}.
   *
   * <p> A {@link Driver} will be selected among the drivers found on the classpath returning
   * {@code true} when {@link Driver#acceptsOptions(SqlConnectOptions)} applied to the first options
   * of the list.
   *
   * @param vertx the Vertx instance to be used with the connection pool
   * @param database the options used to create the connection pool, such as database hostname
   * @param options the options for creating the pool
   * @return the connection pool
   * @throws ServiceConfigurationError if no compatible drivers are found, or if multiple compatible drivers are found
   */
  static Pool pool(Vertx vertx, SqlConnectOptions database, PoolOptions options) {
    List<Driver<SqlConnectOptions>> candidates = new ArrayList<>(1);
    for (Driver<SqlConnectOptions> d : ServiceLoader.load(Driver.class)) {
      if (d.acceptsOptions(database)) {
        candidates.add(d);
      }
    }
    if (candidates.size() == 0) {
      throw new ServiceConfigurationError("No implementations of " + Driver.class + " found that accept connection options " + database);
    } else if (candidates.size() > 1) {
      throw new ServiceConfigurationError("Multiple implementations of " + Driver.class + " found: " + candidates);
    } else {
      Driver<SqlConnectOptions> driver = candidates.get(0);
      return driver.createPool(vertx, Utils.singletonSupplier(driver.downcast(database)), options, new NetClientOptions(), null);
    }
  }

  /**
   * Get a connection from the pool.
   *
   * @return a future notified with the {@link SqlConnection}
   */
  Future<SqlConnection> getConnection();

  /**
   * {@inheritDoc}
   *
   * A connection is borrowed from the connection pool when the query is executed and then immediately returned
   * to the pool after it completes.
   */
  @Override
  Query<RowSet<Row>> query(String sql);

  /**
   * {@inheritDoc}
   *
   * A connection is borrowed from the connection pool when the query is executed and then immediately returned
   * to the pool after it completes.
   */
  @Override
  PreparedQuery<RowSet<Row>> preparedQuery(String sql);

  /**
   * Execute the given {@code function} within a transaction.
   *
   * <p>The {@code function} is passed a client executing all operations within a transaction.
   * When the future returned by the function
   * <ul>
   *   <li>succeeds the transaction commits</li>
   *   <li>fails the transaction rollbacks</li>
   * </ul>
   *
   * <p>The {@code handler} is given a success result when the function returns a succeeded futures and the transaction commits.
   * Otherwise it is given a failure result.
   *
   * @param function the code to execute
   * @return a future notified with the result
   */
  default <T> Future<@Nullable T> withTransaction(Function<SqlConnection, Future<@Nullable T>> function) {
    return getConnection()
      .flatMap(conn -> conn
        .begin()
        .flatMap(tx -> function
          .apply(conn)
          .compose(
            res -> tx
              .commit()
              .flatMap(v -> Future.succeededFuture(res)),
            err -> {
              if (err instanceof TransactionRollbackException) {
                return Future.failedFuture(err);
              } else {
                return tx
                  .rollback()
                  .compose(v -> Future.failedFuture(err), failure -> Future.failedFuture(err));
              }
            }))
        .onComplete(ar -> conn.close()));
  }

  /**
   * Like {@link #withTransaction(Function)} but allows for setting the mode, defining how the acquired
   * connection is managed during the execution of the function.
   */
  default <T> Future<@Nullable T> withTransaction(TransactionPropagation txPropagation, Function<SqlConnection, Future<@Nullable T>> function) {
    if (txPropagation == TransactionPropagation.CONTEXT) {
      ContextInternal context = (ContextInternal) Vertx.currentContext();
      SqlConnection sqlConnection = context.getLocal(PoolImpl.PROPAGATABLE_CONNECTION);
      if (sqlConnection == null) {
        return startPropagatableConnection(this, function);
      }
      return context.succeededFuture(sqlConnection)
        .flatMap(conn -> function.apply(conn)
          .onFailure(err -> {
            if (!(err instanceof TransactionRollbackException)) {
              conn.transaction().rollback();
            }
          }));
    }
    return withTransaction(function);
  }

  /**
   * Get a connection from the pool and execute the given {@code function}.
   *
   * <p> When the future returned by the {@code function} completes, the connection is returned to the pool.
   *
   * <p>The {@code handler} is given a success result when the function returns a succeeded futures.
   * Otherwise it is given a failure result.
   *
   * @param function the code to execute
   * @return a future notified with the result
   */
  default <T> Future<@Nullable T> withConnection(Function<SqlConnection, Future<@Nullable T>> function) {
    return getConnection().flatMap(conn -> function.apply(conn).onComplete(ar -> conn.close()));
  }

  /**
   * Replace the default pool connection provider, the new {@code provider} returns a future connection for a
   * given {@link Context}.
   *
   * <p> A {@link io.vertx.sqlclient.spi.ConnectionFactory} can be used as connection provider.
   *
   * @param provider the new connection provider
   * @return a reference to this, so the API can be used fluently
   * @deprecated instead use {@link ClientBuilder#connectingTo(Supplier)}
   */
  @Deprecated
  @Fluent
  Pool connectionProvider(Function<Context, Future<SqlConnection>> provider);

  /**
   * @return the current pool size approximation
   */
  int size();
}
