package io.vertx.mysqlclient.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.impl.NetSocketInternal;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetClientOptions;
import io.vertx.core.net.NetSocket;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.sqlclient.impl.Connection;

import java.nio.charset.Charset;

public class MySQLConnectionFactory {
  private final NetClient netClient;
  private final Context context;

  private final String host;
  private final int port;
  private final String username;
  private final String password;
  private final String database;
  private final Charset charset;
  private final boolean ssl = false;

  public MySQLConnectionFactory(Context context, PgConnectOptions options) {
    NetClientOptions netClientOptions = new NetClientOptions(options);

    this.context = context;
    this.host = options.getHost();
    this.port = options.getPort();
    this.username = options.getUser();
    this.password = options.getPassword();
    this.database = options.getDatabase();
    this.charset = CharacterSetMapping.getCharset("UTF-8"); // Make it an option later

    this.netClient = context.owner().createNetClient(netClientOptions);
  }

  public void connect(Handler<AsyncResult<Connection>> handler) {
    Future<NetSocket> future = Future.future();
    future.setHandler(ar1 -> {
      if (ar1.succeeded()) {
        NetSocketInternal socket = (NetSocketInternal) ar1.result();
        MySQLSocketConnection conn = new MySQLSocketConnection(socket, 1, context);
        conn.init();
        conn.sendStartupMessage(username, password, database, handler);
      } else {
        handler.handle(Future.failedFuture(ar1.cause()));
      }
    });
    netClient.connect(port, host, future);
  }
}
