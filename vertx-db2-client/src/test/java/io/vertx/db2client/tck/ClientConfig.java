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
package io.vertx.db2client.tck;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.db2client.DB2Builder;
import io.vertx.db2client.DB2ConnectOptions;
import io.vertx.db2client.DB2Connection;
import io.vertx.sqlclient.*;
import io.vertx.sqlclient.tck.Connector;

@SuppressWarnings("unchecked")
public enum ClientConfig {

  CONNECT() {
    @Override
    public Connector<SqlConnection> connect(Vertx vertx, SqlConnectOptions options) {
      return new Connector<SqlConnection>() {
        @Override
        public void connect(Handler<AsyncResult<SqlConnection>> handler) {
          DB2Connection.connect(vertx, new DB2ConnectOptions(options)).onComplete(ar -> {
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
    public Connector<SqlConnection> connect(Vertx vertx, SqlConnectOptions options) {
      Pool pool = DB2Builder.pool()
        .with(new PoolOptions().setMaxSize(1))
        .connectingTo(new DB2ConnectOptions(options))
        .using(vertx)
        .build();
      return new Connector<SqlConnection>() {
        @Override
        public void connect(Handler<AsyncResult<SqlConnection>> handler) {
          pool
            .getConnection()
            .onComplete(ar -> {
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

  public abstract <C extends SqlClient> Connector<C> connect(Vertx vertx, SqlConnectOptions options);

}
