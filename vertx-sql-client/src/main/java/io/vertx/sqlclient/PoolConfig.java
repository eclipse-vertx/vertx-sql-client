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
import io.vertx.core.Handler;
import io.vertx.sqlclient.impl.PoolConfigImpl;

import java.util.Collections;
import java.util.List;

/**
 * The pool configuration that comprehends several mandatory configuration items, plus a few extra optional ones.
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
   * Set the connect options.
   *
   * @param options the connect options
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  default PoolConfig connectOptions(SqlConnectOptions options) {
    return connectOptions(Collections.singletonList(options));
  }

  /**
   * Set the connect options.
   *
   * @param options the connect options
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  PoolConfig connectOptions(List<SqlConnectOptions> options);

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

  @GenIgnore
  SqlConnectOptions determineConnectOptions();

  @GenIgnore
  PoolOptions options();

  @GenIgnore
  Handler<SqlConnection> connectHandler();

}
