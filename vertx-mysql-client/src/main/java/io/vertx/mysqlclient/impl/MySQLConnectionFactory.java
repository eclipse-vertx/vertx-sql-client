package io.vertx.mysqlclient.impl;

import io.vertx.core.*;
import io.vertx.core.impl.NetSocketInternal;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetClientOptions;
import io.vertx.core.net.NetSocket;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.sqlclient.impl.Connection;

import java.nio.charset.Charset;

public class MySQLConnectionFactory {
  private final NetClient netClient;
  private final Context context;
  private final boolean registerCloseHook;
  private final String host;
  private final int port;
  private final String username;
  private final String password;
  private final String database;
  private final Charset charset;
  private final boolean ssl = false;
  private final boolean cachePreparedStatements;
  private final int preparedStatementCacheSize;
  private final int preparedStatementCacheSqlLimit;
  private final Closeable hook;

  public MySQLConnectionFactory(Context context, boolean registerCloseHook, MySQLConnectOptions options) {
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
    this.charset = CharacterSetMapping.getCharset("UTF-8"); // Make it an option later
    this.cachePreparedStatements = options.getCachePreparedStatements();
    this.preparedStatementCacheSize = options.getPreparedStatementCacheMaxSize();
    this.preparedStatementCacheSqlLimit = options.getPreparedStatementCacheSqlLimit();

    this.netClient = context.owner().createNetClient(netClientOptions);
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

  public void connect(Handler<AsyncResult<Connection>> handler) {
    Future<NetSocket> future = Future.future();
    future.setHandler(ar1 -> {
      if (ar1.succeeded()) {
        NetSocketInternal socket = (NetSocketInternal) ar1.result();
        MySQLSocketConnection conn = new MySQLSocketConnection(socket, cachePreparedStatements, preparedStatementCacheSize, preparedStatementCacheSqlLimit, context);
        conn.init();
        conn.sendStartupMessage(username, password, database, handler);
      } else {
        handler.handle(Future.failedFuture(ar1.cause()));
      }
    });
    netClient.connect(port, host, future);
  }
}
