/*
 * Copyright (c) 2011-2021 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */
package io.vertx.oracle.impl.spi;

import io.vertx.core.Vertx;
import io.vertx.oracle.OracleConnectOptions;
import io.vertx.oracle.OraclePool;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.SqlConnectOptions;
import io.vertx.sqlclient.spi.Driver;

public class OracleDriver implements Driver {

  @Override
  public Pool createPool(SqlConnectOptions options, PoolOptions poolOptions) {
    return OraclePool.pool(wrap(options), poolOptions);
  }

  @Override
  public Pool createPool(Vertx vertx, SqlConnectOptions options, PoolOptions poolOptions) {
    return OraclePool.pool(vertx, wrap(options), poolOptions);
  }

  @Override
  public boolean acceptsOptions(SqlConnectOptions options) {
    return options instanceof OracleConnectOptions || SqlConnectOptions.class.equals(options.getClass());
  }

  private static OracleConnectOptions wrap(SqlConnectOptions options) {
    if (options instanceof OracleConnectOptions) {
      return (OracleConnectOptions) options;
    } else {
      return new OracleConnectOptions(options);
    }
  }

}
