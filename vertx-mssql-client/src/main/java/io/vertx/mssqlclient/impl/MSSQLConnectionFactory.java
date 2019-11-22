package io.vertx.mssqlclient.impl;

import io.vertx.core.impl.ContextInternal;
import io.vertx.mssqlclient.MSSQLConnectOptions;
import io.vertx.core.*;
import io.vertx.core.net.impl.NetSocketInternal;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetClientOptions;
import io.vertx.core.net.NetSocket;
import io.vertx.sqlclient.impl.Connection;

import java.util.HashMap;
import java.util.Map;

class MSSQLConnectionFactory {

  private final NetClient netClient;
  private final String host;
  private final int port;
  private final String username;
  private final String password;
  private final String database;
  private final Map<String, String> properties;

  MSSQLConnectionFactory(Vertx vertx, MSSQLConnectOptions options) {
    NetClientOptions netClientOptions = new NetClientOptions(options);

    this.host = options.getHost();
    this.port = options.getPort();
    this.username = options.getUser();
    this.password = options.getPassword();
    this.database = options.getDatabase();
    this.properties = new HashMap<>(options.getProperties());
    this.netClient = vertx.createNetClient(netClientOptions);
  }

  Future<Connection> create(ContextInternal context) {
    Future<NetSocket> fut = netClient.connect(port, host);
    return fut
      .map(so -> new MSSQLSocketConnection((NetSocketInternal) so, false, 0, 0, 1, context))
      .flatMap(conn -> {
        conn.init();
        return Future.future(p -> {
          conn.sendPreLoginMessage(false, preLogin -> {
            if (preLogin.succeeded()) {
              conn.sendLoginMessage(username, password, database, properties, p);
            } else {
              p.fail(preLogin.cause());
            }
          });
        });
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
