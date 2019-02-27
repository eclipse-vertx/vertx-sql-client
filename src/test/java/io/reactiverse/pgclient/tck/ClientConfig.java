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
package io.reactiverse.pgclient.tck;

import io.reactiverse.pgclient.PgClient;
import io.reactiverse.pgclient.PgConnectOptions;
import io.reactiverse.pgclient.PgConnection;
import io.reactiverse.pgclient.PgPool;
import io.reactiverse.pgclient.PgPoolOptions;
import io.reactiverse.sqlclient.Connector;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;

public enum ClientConfig {

  CONNECT() {
    @Override
    Connector<PgConnection> connect(Vertx vertx, PgConnectOptions options) {
      return new Connector<PgConnection>() {
        @Override
        public void connect(Handler<AsyncResult<PgConnection>> handler) {
          PgClient.connect(vertx, options, ar -> {
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
    Connector<PgClient> connect(Vertx vertx, PgConnectOptions options) {
      PgPool pool = PgClient.pool(vertx, new PgPoolOptions(options).setMaxSize(1));
      return new Connector<PgClient>() {
        @Override
        public void connect(Handler<AsyncResult<PgClient>> handler) {
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

  abstract <C extends PgClient> Connector<C> connect(Vertx vertx, PgConnectOptions options);

}
