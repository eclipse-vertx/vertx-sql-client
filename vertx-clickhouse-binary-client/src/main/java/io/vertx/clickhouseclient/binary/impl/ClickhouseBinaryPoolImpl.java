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

package io.vertx.clickhouseclient.binary.impl;

import io.vertx.clickhouseclient.binary.ClickhouseBinaryPool;
import io.vertx.core.impl.CloseFuture;
import io.vertx.core.impl.VertxInternal;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.impl.PoolBase;

public class ClickhouseBinaryPoolImpl extends PoolBase<ClickhouseBinaryPoolImpl> implements ClickhouseBinaryPool {
  public ClickhouseBinaryPoolImpl(VertxInternal vertx, CloseFuture closeFuture, Pool delegate) {
    super(vertx, closeFuture, delegate);
  }
}
