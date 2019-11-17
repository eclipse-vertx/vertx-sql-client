package io.vertx.mssqlclient.impl;

import io.vertx.mssqlclient.impl.codec.MSSQLCodec;
import io.vertx.mssqlclient.impl.command.PreLoginCommand;
import io.netty.channel.ChannelPipeline;
import io.vertx.core.Context;
import io.vertx.core.Handler;
import io.vertx.core.net.impl.NetSocketInternal;
import io.vertx.sqlclient.impl.Connection;
import io.vertx.sqlclient.impl.SocketConnectionBase;
import io.vertx.sqlclient.impl.command.CommandResponse;
import io.vertx.sqlclient.impl.command.InitCommand;

import java.util.Map;

class MSSQLSocketConnection extends SocketConnectionBase {
  MSSQLSocketConnection(NetSocketInternal socket,
                        boolean cachePreparedStatements,
                        int preparedStatementCacheSize,
                        int preparedStatementCacheSqlLimit,
                        int pipeliningLimit,
                        Context context) {
    super(socket, cachePreparedStatements, preparedStatementCacheSize, preparedStatementCacheSqlLimit, pipeliningLimit, context);
  }

  // command response should show what capabilities server provides
  void sendPreLoginMessage(boolean ssl, Handler<? super CommandResponse<Void>> completionHandler) {
    PreLoginCommand cmd = new PreLoginCommand(ssl);
    cmd.handler = completionHandler;
    schedule(cmd);
  }

  void sendLoginMessage(String username, String password, String database, Map<String, String> properties, Handler<? super CommandResponse<Connection>> completionHandler) {
    InitCommand cmd = new InitCommand(this, username, password, database, properties);
    cmd.handler = completionHandler;
    schedule(cmd);
  }

  @Override
  public void init() {
    ChannelPipeline pipeline = socket.channelHandlerContext().pipeline();
    MSSQLCodec.initPipeLine(pipeline);
    super.init();
  }
}
