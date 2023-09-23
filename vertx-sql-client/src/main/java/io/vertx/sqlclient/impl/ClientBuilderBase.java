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
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.net.NetClientOptions;
import io.vertx.sqlclient.*;
import io.vertx.sqlclient.spi.Driver;

import java.util.List;
import java.util.function.Supplier;

public abstract class ClientBuilderBase<C> implements ClientBuilder<C> {

  protected final Driver<SqlConnectOptions> driver;
  private PoolOptions poolOptions;
  private NetClientOptions transportOptions;
  private Supplier<Future<SqlConnectOptions>> database;
  private Handler<SqlConnection> connectHandler;
  private Vertx vertx;

  public ClientBuilderBase(Driver<?> driver) {
    this.driver = (Driver<SqlConnectOptions>) driver;
  }

  @Override
  public ClientBuilder<C> with(PoolOptions options) {
    this.poolOptions = options;
    return this;
  }

  @Override
  public ClientBuilder<C> with(NetClientOptions options) {
    this.transportOptions = options;
    return this;
  }

  @Override
  public ClientBuilder<C> connectingTo(SqlConnectOptions database) {
    return connectingTo(SingletonSupplier.wrap(database));
  }

  @Override
  public ClientBuilder<C> connectingTo(String database) {
    return connectingTo(driver.parseConnectionUri(database));
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
  public ClientBuilder<C> withConnectHandler(Handler<SqlConnection> handler) {
    this.connectHandler = handler;
    return this;
  }

  @Override
  public ClientBuilder<C> using(Vertx vertx) {
    this.vertx = vertx;
    return this;
  }

  @Override
  public final C build() {
    PoolOptions poolOptions = this.poolOptions;
    if (poolOptions == null) {
      poolOptions = new PoolOptions();
    }
    NetClientOptions transportOptions = this.transportOptions;
    if (transportOptions == null) {
      transportOptions = new NetClientOptions();
    }
    C c = create(vertx, database, poolOptions, transportOptions, connectHandler);
    return c;
  }

  protected abstract C create(Vertx vertx, Supplier<Future<SqlConnectOptions>> databases, PoolOptions poolOptions, NetClientOptions transportOptions, Handler<SqlConnection> connectHandler);

}
