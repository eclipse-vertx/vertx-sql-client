package io.vertx.mssqlclient.tck;

import io.vertx.mssqlclient.MSSQLConnectOptions;
import io.vertx.mssqlclient.MSSQLConnection;
import io.vertx.mssqlclient.MSSQLPool;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.SqlConnectOptions;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.tck.Connector;

public enum ClientConfig {
  CONNECT() {
    @Override
    Connector<SqlConnection> connect(Vertx vertx, SqlConnectOptions options) {
      return new Connector<SqlConnection>() {
        @Override
        public void connect(Handler<AsyncResult<SqlConnection>> handler) {
          //TODO remove this when we have data object support for connect options
          MSSQLConnectOptions connectOptions = new MSSQLConnectOptions(options.toJson());
          MSSQLConnection.connect(vertx, connectOptions, ar -> {
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
      MSSQLPool pool = MSSQLPool.pool(vertx, new MSSQLConnectOptions(options.toJson()), new PoolOptions().setMaxSize(1));
      return new Connector<SqlClient>() {
        @Override
        public void connect(Handler<AsyncResult<SqlClient>> handler) {
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
