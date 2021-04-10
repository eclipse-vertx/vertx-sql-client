/*
 *
 *  * Copyright (c) 2021 Vladimir Vishnevsky
 *  *
 *  * This program and the accompanying materials are made available under the
 *  * terms of the Eclipse Public License 2.0 which is available at
 *  * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 *  * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *  *
 *  * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 *
 */

package io.vertx.clickhouse.clickhousenative.spi;

import io.vertx.clickhouse.clickhousenative.ClickhouseNativeConnectOptions;
import io.vertx.clickhouse.clickhousenative.ClickhouseNativePool;
import io.vertx.core.Vertx;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.SqlConnectOptions;
import io.vertx.sqlclient.spi.Driver;

public class ClickhouseNativeDriver implements Driver {
  @Override
  public Pool createPool(SqlConnectOptions options, PoolOptions poolOptions) {
    return ClickhouseNativePool.pool(wrap(options), poolOptions);
  }

  @Override
  public Pool createPool(Vertx vertx, SqlConnectOptions options, PoolOptions poolOptions) {
    return ClickhouseNativePool.pool(vertx, wrap(options), poolOptions);
  }

  @Override
  public boolean acceptsOptions(SqlConnectOptions options) {
    return options instanceof ClickhouseNativeConnectOptions || SqlConnectOptions.class.equals(options.getClass());
  }

  private static ClickhouseNativeConnectOptions wrap(SqlConnectOptions options) {
    if (options instanceof ClickhouseNativeConnectOptions) {
      return (ClickhouseNativeConnectOptions) options;
    } else {
      return new ClickhouseNativeConnectOptions(options);
    }
  }
}
