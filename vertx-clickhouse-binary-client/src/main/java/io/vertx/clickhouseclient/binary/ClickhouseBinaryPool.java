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

package io.vertx.clickhouseclient.binary;

import io.vertx.clickhouseclient.binary.impl.ClickhouseBinaryPoolImpl;
import io.vertx.clickhouseclient.binary.spi.ClickhouseBinaryDriver;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Vertx;
import io.vertx.core.impl.VertxInternal;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;

import java.util.Collections;
import java.util.List;

@VertxGen
public interface ClickhouseBinaryPool extends Pool {
  static ClickhouseBinaryPool pool(ClickhouseBinaryConnectOptions database, PoolOptions options) {
    return pool(null, database, options);
  }

  static ClickhouseBinaryPool pool(Vertx vertx, ClickhouseBinaryConnectOptions connectOptions, PoolOptions poolOptions) {
    return ClickhouseBinaryPoolImpl.create((VertxInternal)vertx, Collections.singletonList(connectOptions), poolOptions);
  }

  static ClickhouseBinaryPool pool(List<ClickhouseBinaryConnectOptions> databases, PoolOptions options) {
    return pool(null, databases, options);
  }

  static ClickhouseBinaryPool pool(Vertx vertx, List<ClickhouseBinaryConnectOptions> databases, PoolOptions options) {
    return new ClickhouseBinaryDriver().createPool(vertx, databases, options);
  }
}
