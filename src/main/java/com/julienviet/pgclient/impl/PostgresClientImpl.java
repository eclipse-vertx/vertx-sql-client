package com.julienviet.pgclient.impl;

import com.julienviet.pgclient.PgClient;
import com.julienviet.pgclient.PgClientOptions;
import com.julienviet.pgclient.PgConnectionOptions;
import com.julienviet.pgclient.PgConnectionPool;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.impl.NetSocketInternal;
import io.vertx.core.impl.VertxInternal;
import io.vertx.core.net.NetClient;
import com.julienviet.pgclient.PgConnection;
import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.sql.SQLConnection;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class PostgresClientImpl implements PgClient {

  private static final PgConnectionOptions DEFAULT_OPTIONS = new PgConnectionOptions();
  final NetClient client;
  final VertxInternal vertx;
  final String host;
  final int port;
  final String database;
  final String username;
  final String password;

  public PostgresClientImpl(Vertx vertx, PgClientOptions options) {
    this.host = options.getHost();
    this.port = options.getPort();
    this.database = options.getDatabase();
    this.username = options.getUsername();
    this.password = options.getPassword();
    this.vertx = (VertxInternal) vertx;
    this.client = vertx.createNetClient();
  }

  @Override
  public void close() {
    client.close();
  }

  @Override
  public void connect(Handler<AsyncResult<PgConnection>> completionHandler) {
    connect(DEFAULT_OPTIONS, completionHandler);
  }

  @Override
  public void connect(PgConnectionOptions options, Handler<AsyncResult<PgConnection>> completionHandler) {
    client.connect(port, host, null, ar1 -> {
      if (ar1.succeeded()) {
        NetSocketInternal socket = (NetSocketInternal) ar1.result();
        DbConnection conn = new DbConnection(this, socket, vertx.getOrCreateContext(), options);
        conn.init(username, password, database, ar2 -> {
          if (ar2.succeeded()) {
            completionHandler.handle(Future.succeededFuture(new PostgresConnectionImpl(ar2.result())));
          } else {
            completionHandler.handle(Future.failedFuture(ar2.cause()));
          }
        });
      } else {
        completionHandler.handle(Future.failedFuture(ar1.cause()));
      }
    });
  }

  @Override
  public SQLClient getConnection(Handler<AsyncResult<SQLConnection>> handler) {
    client.connect(port, host, null, ar1 -> {
      if (ar1.succeeded()) {
        NetSocketInternal socket = (NetSocketInternal) ar1.result();
        DbConnection conn = new DbConnection(this, socket, vertx.getOrCreateContext(), DEFAULT_OPTIONS);
        conn.init(username, password, database, ar2 -> {
          if (ar2.succeeded()) {
            handler.handle(Future.succeededFuture(new PostgresSQLConnection(ar2.result())));
          } else {
            handler.handle(Future.failedFuture(ar2.cause()));
          }
        });
      } else {
        handler.handle(Future.failedFuture(ar1.cause()));
      }
    });
    return this;
  }

  @Override
  public void close(Handler<AsyncResult<Void>> handler) {
    throw new UnsupportedOperationException("Implement me");
  }

  @Override
  public PgConnectionPool createPool(int size) {
    return new PostgresConnectionPoolImpl(this, size);
  }

}
