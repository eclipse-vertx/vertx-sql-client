/*
 * Copyright (c) 2011-2019 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.tests.mssqlclient.tck;

import io.vertx.mssqlclient.MSSQLBuilder;
import io.vertx.mssqlclient.MSSQLConnectOptions;
import io.vertx.mssqlclient.MSSQLConnection;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.sqlclient.*;
import io.vertx.tests.sqlclient.tck.Connector;

public enum ClientConfig {
  CONNECT() {
    @Override
    Connector<SqlConnection> connect(Vertx vertx, SqlConnectOptions options) {
      return new Connector<SqlConnection>() {
        @Override
        public void connect(Handler<AsyncResult<SqlConnection>> handler) {
          //TODO remove this when we have data object support for connect options
          MSSQLConnectOptions connectOptions = new MSSQLConnectOptions(options);
          MSSQLConnection.connect(vertx, connectOptions).onComplete(ar -> {
            if (ar.succeeded()) {
              handler.handle(Future.succeededFuture(ar.result()));
            } else {
              handler.handle(Future.failedFuture(ar.cause()));
            }
          });
        }

        @Override
        public void close() {
        }
      };
    }
  },

  POOLED() {
    @Override
    Connector<SqlClient> connect(Vertx vertx, SqlConnectOptions options) {
      Pool pool = MSSQLBuilder.pool(builder -> builder
        .with(new PoolOptions().setMaxSize(1))
        .connectingTo(new MSSQLConnectOptions(options))
        .using(vertx));
      return new Connector<SqlClient>() {
        @Override
        public void connect(Handler<AsyncResult<SqlClient>> handler) {
          pool.getConnection().onComplete(ar -> {
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
