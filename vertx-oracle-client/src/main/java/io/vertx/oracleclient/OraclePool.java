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
package io.vertx.oracleclient;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.impl.ContextInternal;
import io.vertx.core.impl.VertxInternal;
import io.vertx.oracleclient.impl.OraclePoolImpl;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.impl.tracing.QueryTracer;

/**
 * Represents a pool of connection to interact with an Oracle database.
 */
@VertxGen
public interface OraclePool extends Pool {

  static OraclePool pool(OracleConnectOptions connectOptions, PoolOptions poolOptions) {
    if (Vertx.currentContext() != null) {
      throw new IllegalStateException(
        "Running in a Vertx context => use OraclePool#pool(Vertx, MySQLConnectOptions, PoolOptions) instead");
    }
    VertxOptions vertxOptions = new VertxOptions();
    VertxInternal vertx = (VertxInternal) Vertx.vertx(vertxOptions);
    final ContextInternal context = vertx.getOrCreateContext();
    QueryTracer tracer = context.tracer() == null ? null : new QueryTracer(vertx.tracer(), connectOptions);
    return OraclePoolImpl.create(vertx, true, connectOptions, poolOptions, tracer);
  }

  /**
   * Like {@link #pool(OracleConnectOptions, PoolOptions)} with a specific {@link Vertx} instance.
   */
  static OraclePool pool(Vertx vertx, OracleConnectOptions connectOptions, PoolOptions poolOptions) {
    final ContextInternal context = (ContextInternal) vertx.getOrCreateContext();
    QueryTracer tracer = context.tracer() == null ? null : new QueryTracer(((VertxInternal) vertx).tracer(), connectOptions);
    return OraclePoolImpl.create((VertxInternal) vertx, false, connectOptions, poolOptions, tracer);
  }

  /**
   * Like {@link #pool(OracleConnectOptions, PoolOptions)} but connection options are created from the provided {@code connectionUri}.
   */
  static OraclePool pool(String connectionUri, PoolOptions poolOptions) {
    return pool(OracleConnectOptions.fromUri(connectionUri), poolOptions);
  }

  /**
   * Like {@link #pool(String, PoolOptions)} with a specific {@link Vertx} instance.
   */
  static OraclePool pool(Vertx vertx, String connectionUri, PoolOptions poolOptions) {
    return pool(vertx, OracleConnectOptions.fromUri(connectionUri), poolOptions);
  }
}
