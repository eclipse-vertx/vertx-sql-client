/*
 * Copyright (C) 2020 IBM Corporation
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
package io.vertx.sqlclient.spi;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.impl.CloseFuture;
import io.vertx.core.impl.ContextInternal;
import io.vertx.core.impl.VertxInternal;
import io.vertx.core.spi.metrics.ClientMetrics;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.SqlConnectOptions;
import io.vertx.sqlclient.impl.Connection;
import io.vertx.sqlclient.impl.SqlConnectionBase;
import io.vertx.sqlclient.impl.SqlConnectionInternal;
import io.vertx.sqlclient.impl.tracing.QueryTracer;

import java.util.List;
import java.util.function.Supplier;

/**
 * An entry point to the Vertx Reactive SQL Client
 * Every driver must implement this interface.
 */
public interface Driver {

  /**
   * Create a connection pool to the database configured with the given {@code connectOptions} and {@code poolOptions}.
   *
   * <p> The returned pool will automatically closed when {@code vertx} is not {@code null} and is closed or when the creating
   * context is closed (e.g verticle undeployment).
   *
   * @param vertx the Vertx instance to be used with the connection pool or {@code null} to create an auto closed Vertx instance
   * @param databases the list of databases
   * @param options the options for creating the pool
   * @return the connection pool
   */
  default Pool createPool(Vertx vertx, Supplier<? extends SqlConnectOptions> databases, PoolOptions options) {
    VertxInternal vx;
    if (vertx == null) {
      if (Vertx.currentContext() != null) {
        throw new IllegalStateException("Running in a Vertx context => use Pool#pool(Vertx, SqlConnectOptions, PoolOptions) instead");
      }
      VertxOptions vertxOptions = new VertxOptions();
      SqlConnectOptions database = databases.get();
      if (database.isUsingDomainSocket()) {
        vertxOptions.setPreferNativeTransport(true);
      }
      vx = (VertxInternal) Vertx.vertx(vertxOptions);
    } else {
      vx = (VertxInternal) vertx;
    }
    CloseFuture closeFuture = new CloseFuture();
    Pool pool;
    try {
      pool = newPool(vx, databases, options, closeFuture);
    } catch (Exception e) {
      if (vertx == null) {
        vx.close();
      }
      throw e;
    }
    if (vertx == null) {
      closeFuture.future().onComplete(ar -> vx.close());
    } else {
      ContextInternal ctx = vx.getContext();
      if (ctx != null) {
        ctx.addCloseHook(closeFuture);
      } else {
        vx.addCloseHook(closeFuture);
      }
    }
    return pool;
  }

  /**
   * Create a connection pool to the database configured with the given {@code connectOptions} and {@code poolOptions}.
   *
   * <p> The returned pool will automatically closed when {@code vertx} is not {@code null} and is closed or when the creating
   * context is closed (e.g verticle undeployment).
   *
   * @param vertx the Vertx instance to be used with the connection pool or {@code null} to create an auto closed Vertx instance
   * @param databases the list of databases
   * @param options the options for creating the pool
   * @return the connection pool
   */
  default Pool createPool(Vertx vertx, List<? extends SqlConnectOptions> databases, PoolOptions options) {
    VertxInternal vx;
    if (vertx == null) {
      if (Vertx.currentContext() != null) {
        throw new IllegalStateException("Running in a Vertx context => use Pool#pool(Vertx, SqlConnectOptions, PoolOptions) instead");
      }
      VertxOptions vertxOptions = new VertxOptions();
      SqlConnectOptions database = databases.get(0);
      if (database.isUsingDomainSocket()) {
        vertxOptions.setPreferNativeTransport(true);
      }
      vx = (VertxInternal) Vertx.vertx(vertxOptions);
    } else {
      vx = (VertxInternal) vertx;
    }
    CloseFuture closeFuture = new CloseFuture();
    Pool pool;
    try {
      pool = newPool(vx, databases, options, closeFuture);
    } catch (Exception e) {
      if (vertx == null) {
        vx.close();
      }
      throw e;
    }
    if (vertx == null) {
      closeFuture.future().onComplete(ar -> vx.close());
    } else {
      ContextInternal ctx = vx.getContext();
      if (ctx != null) {
        ctx.addCloseHook(closeFuture);
      } else {
        vx.addCloseHook(closeFuture);
      }
    }
    return pool;
  }

  /**
   * Create a connection pool to the database configured with the given {@code connectOptions} and {@code poolOptions}.
   *
   * This method is not meant to be used directly by users, instead they should use {@link #createPool(Vertx, List, PoolOptions)}.
   *
   * @param vertx the Vertx instance to be used with the connection pool
   * @param databases the list of databases
   * @param options the options for creating the pool
   * @param closeFuture the close future
   * @return the connection pool
   */
  Pool newPool(Vertx vertx, List<? extends SqlConnectOptions> databases, PoolOptions options, CloseFuture closeFuture);

  /**
   * Create a connection pool to the database configured with the given {@code connectOptions} and {@code poolOptions}.
   *
   * This method is not meant to be used directly by users, instead they should use {@link #createPool(Vertx, List, PoolOptions)}.
   *
   * @param vertx the Vertx instance to be used with the connection pool
   * @param databases the list of databases
   * @param options the options for creating the pool
   * @param closeFuture the close future
   * @return the connection pool
   */
  Pool newPool(Vertx vertx, Supplier<? extends SqlConnectOptions> databases, PoolOptions options, CloseFuture closeFuture);

  /**
   * Create a connection factory to the given {@code database}.
   *
   * @param vertx the Vertx instance t
   * @param database the database to connect to
   * @return the connection factory
   */
  ConnectionFactory createConnectionFactory(Vertx vertx, SqlConnectOptions database);

  /**
   * Create a connection factory to the given {@code database}.
   *
   * @param vertx the Vertx instance t
   * @param database the database to connect to
   * @return the connection factory
   */
  ConnectionFactory createConnectionFactory(Vertx vertx, Supplier<? extends SqlConnectOptions> database);

  /**
   * @return {@code true} if the driver accepts the {@code connectOptions}, {@code false} otherwise
   */
  SqlConnectOptions parseConnectionUri(String uri);

  /**
   * @return true if the driver accepts the {@code connectOptions}, false otherwise
   */
  boolean acceptsOptions(SqlConnectOptions connectOptions);

  /**
   * Append a parameter placeholder in the {@code query}.
   *
   * <p>The index starts at {@code 0}.
   *
   * <ul>
   *   <li>When {@code index == current} indicates it is a new parameter and therefore the same
   *    * value should be returned.</li>
   *   <li>When {@code index < current} indicates the builder wants to reuse a parameter.
   *   The implementation can either return the same value to indicate the parameter can be reused or
   *   return the next index to use (which is shall be the {@code current} value</li>
   * </ul>
   *
   * @param queryBuilder the builder to append to
   * @param index the parameter placeholder index
   * @return the index at which the parameter placeholder could be added
   */
  default int appendQueryPlaceholder(StringBuilder queryBuilder, int index, int current) {
    queryBuilder.append("?");
    return current;
  }

  default SqlConnectionInternal wrapConnection(ContextInternal context, ConnectionFactory factory, Connection conn, QueryTracer tracer, ClientMetrics metrics) {
    return new SqlConnectionBase<>(context, factory, conn, this, tracer, metrics);
  }
}
