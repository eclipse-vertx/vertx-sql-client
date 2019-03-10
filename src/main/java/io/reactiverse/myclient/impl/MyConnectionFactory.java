package io.reactiverse.myclient.impl;

import io.netty.channel.ChannelPipeline;
import io.reactiverse.pgclient.PgConnectOptions;
import io.reactiverse.pgclient.PgConnection;
import io.reactiverse.pgclient.impl.Connection;
import io.reactiverse.pgclient.impl.PgConnectionImpl;
import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.impl.NetSocketInternal;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetClientOptions;
import io.vertx.core.net.NetSocket;

import java.nio.charset.Charset;

public class MyConnectionFactory {
  private final NetClient netClient;
  private final Context context;

  private final String host;
  private final int port;
  private final String username;
  private final String password;
  private final String database;
  private final Charset charset;
  private final boolean ssl = false;

  public MyConnectionFactory(Context context, PgConnectOptions options) {
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

  public void connect(Handler<AsyncResult<PgConnection>> handler) {
    Future<NetSocket> future = Future.future();
    future.setHandler(ar1 -> {
      if (ar1.succeeded()) {
        NetSocketInternal socket = (NetSocketInternal) ar1.result();
        MySocketConnection conn = new MySocketConnection(socket, 1, context);
        conn.init();
        conn.sendStartupMessage(username, password, database, ar2 -> {
          if (ar2.succeeded()) {
            Connection connection = ar2.result();
            PgConnectionImpl holder = new PgConnectionImpl(null, context, ar2.result());
            connection.init(holder);
            handler.handle(Future.succeededFuture(holder));
          } else {
            handler.handle(Future.failedFuture(ar2.cause()));
          }
        });
      } else {
        handler.handle(Future.failedFuture(ar1.cause()));
      }
    });
    netClient.connect(port, host, future);
  }

  public void initProtocol(NetSocketInternal socket, String username, String password, String database, boolean ssl, Handler<AsyncResult<PgConnection>> handler) {
    ChannelPipeline pipeline = socket.channelHandlerContext().pipeline();
    Future<Void> future = Future.future();
    future.setHandler(ar -> {
      if (ar.succeeded()) {
        /*
        handler.handle(Future.succeededFuture(this));
        switchToState(MySQLSocketConnection.State.COMMANDING);
        pipeline.remove("authenticationHandler");
        pipeline.addBefore("handler", "commandHandler", commandHandler);
        */
      } else {
        handler.handle(Future.failedFuture(ar.cause()));
      }
    });
    // InitialHandshakeHandler initialHandshakeHandler = new InitialHandshakeHandler(charset, this, username, password, database, ssl, future);
    // pipeline.addBefore("handler", "handshakeHandler", initialHandshakeHandler);
    // switchToState(MySQLSocketConnection.State.CONNECTING);
  }
}
