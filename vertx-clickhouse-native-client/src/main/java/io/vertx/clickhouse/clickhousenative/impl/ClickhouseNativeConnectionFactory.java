package io.vertx.clickhouse.clikhousenative.impl;

import io.vertx.clickhouse.clikhousenative.ClickhouseNativeConnectOptions;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.impl.EventLoopContext;
import io.vertx.core.net.NetClientOptions;
import io.vertx.core.net.NetSocket;
import io.vertx.core.net.impl.NetSocketInternal;
import io.vertx.sqlclient.SqlConnectOptions;
import io.vertx.sqlclient.impl.Connection;
import io.vertx.sqlclient.impl.ConnectionFactory;
import io.vertx.sqlclient.impl.SqlConnectionFactoryBase;

public class ClickhouseNativeConnectionFactory extends SqlConnectionFactoryBase implements ConnectionFactory {
  private int pipeliningLimit;

  ClickhouseNativeConnectionFactory(EventLoopContext context, ClickhouseNativeConnectOptions options) {
    super(context, options);
  }

  @Override
  protected void initializeConfiguration(SqlConnectOptions connectOptions) {
    ClickhouseNativeConnectOptions options = (ClickhouseNativeConnectOptions) connectOptions;
    this.pipeliningLimit = options.getPipeliningLimit();
  }

  @Override
  protected void configureNetClientOptions(NetClientOptions netClientOptions) {
    netClientOptions.setSsl(false);
  }

  @Override
  protected void doConnectInternal(Promise<Connection> promise) {
    doConnect().flatMap(conn -> {
      ClickhouseNativeSocketConnection socket = (ClickhouseNativeSocketConnection) conn;
      socket.init();
      return Future.<Connection>future(p -> socket.sendStartupMessage(username, password, database, properties, p))
        .map(conn);
    }).onComplete(promise);
  }

  private Future<Connection> doConnect() {
    Future<NetSocket> soFut;
    try {
      soFut = netClient.connect(socketAddress, (String) null);
    } catch (Exception e) {
      // Client is closed
      return context.failedFuture(e);
    }
    Future<Connection> connFut = soFut.map(so -> newSocketConnection((NetSocketInternal) so));
    return connFut;
  }

  private ClickhouseNativeSocketConnection newSocketConnection(NetSocketInternal socket) {
    return new ClickhouseNativeSocketConnection(socket, cachePreparedStatements, preparedStatementCacheSize,
      preparedStatementCacheSqlFilter, pipeliningLimit, context);
  }
}
