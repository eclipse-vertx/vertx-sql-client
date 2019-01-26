package io.reactiverse.mysqlclient.impl;

import io.reactiverse.mysqlclient.MySQLConnectOptions;
import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.impl.NetSocketInternal;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetClientOptions;
import io.vertx.core.net.NetSocket;

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

  public MySQLConnectionFactory(Context context, MySQLConnectOptions options) {
    NetClientOptions netClientOptions = new NetClientOptions(options);

    this.context = context;
    this.host = options.getHost();
    this.port = options.getPort();
    this.username = options.getUser();
    this.password = options.getPassword();
    this.database = options.getDatabase();
    this.charset = CharacterSetMapping.getCharset(options.getCharset());

    this.netClient = context.owner().createNetClient(netClientOptions);
  }

  public void create(Handler<AsyncResult<MySQLSocketConnection>> handler) {
    Future<NetSocket> future = Future.future();
    future.setHandler(ar -> {
      if (ar.succeeded()) {
        NetSocketInternal socket = (NetSocketInternal) ar.result();

        MySQLSocketConnection mySQLSocketConnection = new MySQLSocketConnection(socket, charset, context);
        mySQLSocketConnection.initProtocol(username, password, database, ssl, handler);
      } else {
        handler.handle(Future.failedFuture(ar.cause()));
      }
    });
    netClient.connect(port, host, future);
  }
}
