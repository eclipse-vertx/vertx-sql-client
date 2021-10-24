/*
 *
 *  Copyright (c) 2021 Vladimir Vishnevskii
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Eclipse Public License 2.0 which is available at
 *  http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 *  which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 *  SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 *
 */

package io.vertx.clickhouseclient.binary.spi;

import io.vertx.clickhouseclient.binary.ClickhouseBinaryConnectOptions;
import io.vertx.clickhouseclient.binary.ClickhouseBinaryPool;
import io.vertx.clickhouseclient.binary.impl.ClickhouseBinaryConnectionFactory;
import io.vertx.clickhouseclient.binary.impl.ClickhouseBinaryPoolImpl;
import io.vertx.core.Vertx;
import io.vertx.core.impl.VertxInternal;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.SqlConnectOptions;
import io.vertx.sqlclient.spi.ConnectionFactory;
import io.vertx.sqlclient.spi.Driver;

import java.util.List;

public class ClickhouseBinaryDriver implements Driver {
  @Override
  public ClickhouseBinaryPool createPool(Vertx vertx, List<? extends SqlConnectOptions> databases, PoolOptions poolOptions) {
    return ClickhouseBinaryPoolImpl.create((VertxInternal) vertx, databases, poolOptions);
  }

  @Override
  public ConnectionFactory createConnectionFactory(Vertx vertx, SqlConnectOptions database) {
    return new ClickhouseBinaryConnectionFactory((VertxInternal) vertx, ClickhouseBinaryConnectOptions.wrap(database));
  }

  @Override
  public boolean acceptsOptions(SqlConnectOptions options) {
    return options instanceof ClickhouseBinaryConnectOptions || SqlConnectOptions.class.equals(options.getClass());
  }
}
