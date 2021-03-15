package io.vertx.clickhousenativeclient.tck;

import io.vertx.clickhouse.clickhousenative.ClickhouseNativeConnectOptions;
import io.vertx.clickhouse.clickhousenative.ClickhouseNativeConnection;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.SqlConnectOptions;
import io.vertx.sqlclient.tck.Connector;

public enum ClientConfig {
  CONNECT() {
    @Override
    Connector<ClickhouseNativeConnection> connect(Vertx vertx, SqlConnectOptions options) {
      return new Connector<ClickhouseNativeConnection>() {
        @Override
        public void connect(Handler<AsyncResult<ClickhouseNativeConnection>> handler) {
          ClickhouseNativeConnection.connect(vertx, new ClickhouseNativeConnectOptions(options.toJson()), ar -> {
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
  };

  abstract <C extends SqlClient> Connector<C> connect(Vertx vertx, SqlConnectOptions options);
}
