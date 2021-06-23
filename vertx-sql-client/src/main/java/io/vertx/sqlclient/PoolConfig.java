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

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.sqlclient.impl.PoolConfigImpl;

import java.util.List;
import java.util.function.Supplier;

/**
 * The pool configuration that comprehends several mandatory configuration items, plus a few extra optional ones.
 *
 * The pool config provides advanced features:
 *
 * <ul>
 *   <li>Connect handler for connection initialization</li>
 *   <li>Load balance across multiple servers</li>
 * </ul>
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@VertxGen
public interface PoolConfig {

  /**
   * Create a pool config.
   *
   * @param options the pool options
   */
  static PoolConfig create(PoolOptions options) {
    return new PoolConfigImpl(options);
  }

  /**
   * Create a pool config.
   */
  static PoolConfig create() {
    return create(new PoolOptions());
  }

  /**
   * Configures the pool to connect to given {@code server}.
   *
   * @param server the connect options
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  PoolConfig connectingTo(SqlConnectOptions server);

  /**
   * Configures the pool to pick the server among the given list of {@code servers} using round robin
   * load balancing.
   *
   * <ul>
   *   <li>The first server of the list defines the base options applied to all servers and defines the common connection configuration such as SSL certificates, etc...</li>
   *   <li>The remaining servers use the network address, user, password and databases</li>
   * </ul>
   * <p>
   *
   * @param servers the list of servers
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  PoolConfig connectingTo(List<SqlConnectOptions> servers);

  /**
   * Configures the pool to user a dynamic configuration provided by the {@code serverProvider}.
   *
   * <ul>
   *   <li>The {@code base} options is applied to all servers and defines the common connection configure such as SSL certificates, etc...</li>
   *   <li>When the pool creates a connection it uses the value returned by the supplier to define the network address, user, password and databases to use</li>
   * </ul>
   * <p>
   *
   * When the {@code serverProvider} fails to return a value, the pool falls back to the {@code base} options.
   *
   * @param base the base options for all servers
   * @param serverProvider the list of servers
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore
  PoolConfig connectingTo(SqlConnectOptions base, Supplier<Future<SqlConnectOptions>> serverProvider);

  /**
   * Set an handler called when the pool has established a connection to the database.
   *
   * <p> This handler allows interactions with the database before the connection is added to the pool.
   *
   * <p> When the handler has finished, it must call {@link SqlConnection#close()} to release the connection
   * to the pool.
   *
   * @param handler the handler
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  PoolConfig connectHandler(Handler<SqlConnection> handler);

  SqlConnectOptions baseConnectOptions();

  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  Supplier<Future<SqlConnectOptions>> connectOptionsProvider();

  @GenIgnore
  PoolOptions options();

  @GenIgnore
  Handler<SqlConnection> connectHandler();

}
