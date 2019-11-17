package io.vertx.mssqlclient.impl;

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
  private final Context context;
  private final boolean registerCloseHook;

  private final String host;
  private final int port;
  private final String username;
  private final String password;
  private final String database;
  private final Map<String, String> properties;
  private final Closeable hook;

  public MSSQLConnectionFactory(Context context, boolean registerCloseHook, MSSQLConnectOptions options) {
    NetClientOptions netClientOptions = new NetClientOptions(options);

    this.context = context;
    this.registerCloseHook = registerCloseHook;
    this.hook = this::close;
    if (registerCloseHook) {
      context.addCloseHook(hook);
    }

    this.host = options.getHost();
    this.port = options.getPort();
    this.username = options.getUser();
    this.password = options.getPassword();
    this.database = options.getDatabase();
    this.properties = new HashMap<>(options.getProperties());

    this.netClient = context.owner().createNetClient(netClientOptions);
  }

  public void create(Handler<AsyncResult<Connection>> completionHandler) {
    Promise<NetSocket> promise = Promise.promise();
    promise.future().setHandler(connect -> {
      if (connect.succeeded()) {
        NetSocketInternal socket = (NetSocketInternal) connect.result();
        MSSQLSocketConnection conn = new MSSQLSocketConnection(socket, false, 0, 0, 1, context);
        conn.init();
        conn.sendPreLoginMessage(false, preLogin -> {
          if (preLogin.succeeded()) {
            conn.sendLoginMessage(username, password, database, properties, (Handler)completionHandler);
          } else {
            completionHandler.handle(Future.failedFuture(preLogin.cause()));
          }
        });
      } else {
        completionHandler.handle(Future.failedFuture(connect.cause()));
      }
    });
    netClient.connect(port, host, promise);
  }

  // Called by hook
  private void close(Handler<AsyncResult<Void>> completionHandler) {
    netClient.close();
    completionHandler.handle(Future.succeededFuture());
  }

  void close() {
    if (registerCloseHook) {
      context.removeCloseHook(hook);
    }
    netClient.close();
  }
}
