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
package io.vertx.oracle.test.tck;

import io.vertx.core.*;
import io.vertx.core.impl.VertxInternal;
import io.vertx.oracle.OracleConnectOptions;
import io.vertx.oracle.OraclePool;
import io.vertx.oracle.impl.OracleConnectionFactory;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.SqlConnectOptions;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.tck.Connector;

public enum ClientConfig {

  CONNECT() {
    @Override
    Connector<SqlConnection> connect(Vertx vertx, SqlConnectOptions options) {
      return new Connector<>() {

        private OracleConnectionFactory factory;

        @Override
        public void connect(Handler<AsyncResult<SqlConnection>> handler) {
          factory = new OracleConnectionFactory((VertxInternal) vertx, new OracleConnectOptions(options.toJson()), new PoolOptions().setMaxSize(1), null, null);
          Context context = vertx.getOrCreateContext();
          factory.connect(context).onComplete(handler);
        }

        @Override
        public void close() {
          if (factory != null) {
            factory.close(Promise.promise());
          }
        }
      };
    }
  },

  POOLED() {
    @Override
    Connector<SqlConnection> connect(Vertx vertx, SqlConnectOptions options) {
      OraclePool pool = OraclePool
        .pool(vertx, new OracleConnectOptions(options.toJson()), new PoolOptions().setMaxSize(5));
      return new Connector<>() {
        @Override
        public void connect(Handler<AsyncResult<SqlConnection>> handler) {
          pool.getConnection(ar -> {
            if (ar.succeeded()) {
              handler.handle(Future.succeededFuture(ar.result()));
            } else {
              handler.handle(Future.failedFuture(ar.cause()));
            }
          });
        }

        @Override
        public void close() {
          pool.close();
        }
      };
    }
  };

  abstract <C extends SqlClient> Connector<C> connect(Vertx vertx, SqlConnectOptions options);

}
