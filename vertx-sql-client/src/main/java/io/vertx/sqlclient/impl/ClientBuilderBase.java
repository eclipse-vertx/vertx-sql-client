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
package io.vertx.sqlclient.impl;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.sqlclient.ClientBuilder;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.SqlConnectOptions;
import io.vertx.sqlclient.spi.Driver;

import java.util.List;
import java.util.function.Supplier;

public abstract class ClientBuilderBase<C> implements ClientBuilder<C> {

  protected final Driver driver;
  protected PoolOptions poolOptions;
  protected Supplier<Future<SqlConnectOptions>> database;
  protected Vertx vertx;

  public ClientBuilderBase(Driver driver) {
    this.driver = (Driver) driver;
  }

  @Override
  public ClientBuilder<C> config(PoolOptions options) {
    this.poolOptions = options;
    return this;
  }

  @Override
  public ClientBuilder<C> connectingTo(SqlConnectOptions database) {
    return connectingTo(SingletonSupplier.wrap(database));
  }

  @Override
  public ClientBuilder<C> connectingTo(Supplier<Future<SqlConnectOptions>> supplier) {
    this.database = supplier;
    return this;
  }

  @Override
  public ClientBuilder<C> connectingTo(List<SqlConnectOptions> databases) {
    return connectingTo(Utils.roundRobinSupplier(databases));
  }

  @Override
  public ClientBuilder<C> using(Vertx vertx) {
    this.vertx = vertx;
    return this;
  }
}
