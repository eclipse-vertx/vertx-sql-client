/*
 * Copyright (c) 2011-2022 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */
package io.vertx.oracleclient.impl;

import io.vertx.core.impl.CloseFuture;
import io.vertx.core.impl.VertxInternal;
import io.vertx.oracleclient.OraclePool;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.impl.PoolBase;

public class OraclePoolImpl extends PoolBase<OraclePoolImpl> implements OraclePool {

  public OraclePoolImpl(VertxInternal vertx, CloseFuture closeFuture, Pool delegate) {
    super(vertx, closeFuture, delegate);
  }
}
