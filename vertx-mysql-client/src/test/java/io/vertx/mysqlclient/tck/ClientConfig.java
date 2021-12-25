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
package io.vertx.mysqlclient.tck;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.MySQLConnection;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.tck.Connector;
import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.SqlConnectOptions;
import io.vertx.sqlclient.SqlConnection;

public enum ClientConfig {

  CONNECT() {
    @Override
    Connector<SqlConnection> connect(Vertx vertx, SqlConnectOptions options) {
      return new Connector<SqlConnection>() {
        @Override
        public void connect(Handler<AsyncResult<SqlConnection>> handler) {
          MySQLConnection.connect(vertx, new MySQLConnectOptions(options), ar -> {
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
    Connector<SqlConnection> connect(Vertx vertx, SqlConnectOptions options) {
      MySQLPool pool = MySQLPool.pool(vertx, new MySQLConnectOptions(options), new PoolOptions().setMaxSize(1));
      return new Connector<SqlConnection>() {
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
