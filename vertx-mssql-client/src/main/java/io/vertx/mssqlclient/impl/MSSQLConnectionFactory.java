package io.vertx.mssqlclient.impl;

import io.vertx.core.impl.ContextInternal;
import io.vertx.mssqlclient.MSSQLConnectOptions;
import io.vertx.core.*;
import io.vertx.core.net.impl.NetSocketInternal;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetClientOptions;
import io.vertx.core.net.NetSocket;
import io.vertx.sqlclient.impl.Connection;
import io.vertx.sqlclient.impl.ConnectionFactory;

import java.util.HashMap;
import java.util.Map;

class MSSQLConnectionFactory implements ConnectionFactory {

  private final NetClient netClient;
  private final ContextInternal context;
  private final String host;
  private final int port;
  private final String username;
  private final String password;
  private final String database;
  private final Map<String, String> properties;

  MSSQLConnectionFactory(Vertx vertx, ContextInternal context, MSSQLConnectOptions options) {
    NetClientOptions netClientOptions = new NetClientOptions(options);

    this.context = context;
    this.host = options.getHost();
    this.port = options.getPort();
    this.username = options.getUser();
    this.password = options.getPassword();
    this.database = options.getDatabase();
    this.properties = new HashMap<>(options.getProperties());
    this.netClient = vertx.createNetClient(netClientOptions);
  }

  @Override
  public Future<Connection> connect() {
    Promise<Connection> promise = context.promise();
    context.dispatch(null, v -> doConnect(promise));
    return promise.future();
  }

  public void doConnect(Promise<Connection> promise) {
    Future<NetSocket> fut = netClient.connect(port, host);
    fut.onComplete(ar -> {
      if (ar.succeeded()) {
        NetSocket so = ar.result();
        MSSQLSocketConnection conn = new MSSQLSocketConnection((NetSocketInternal) so, false, 0, 0, 1, context);
        conn.init();
        conn.sendPreLoginMessage(false, preLogin -> {
          if (preLogin.succeeded()) {
            conn.sendLoginMessage(username, password, database, properties, promise);
          } else {
            promise.fail(preLogin.cause());
          }
        });
      } else {
        promise.fail(ar.cause());
      }
    });
  }

  // Called by hook
  private void close(Handler<AsyncResult<Void>> completionHandler) {
    netClient.close();
    completionHandler.handle(Future.succeededFuture());
  }

  void close() {
    netClient.close();
  }
}
