/*
 * Copyright (C) 2017 Julien Viet
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.vertx.mysqlclient.impl;

import io.netty.channel.ChannelPipeline;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.impl.EventLoopContext;
import io.vertx.core.net.impl.NetSocketInternal;
import io.vertx.core.spi.metrics.ClientMetrics;
import io.vertx.mysqlclient.MySQLAuthenticationPlugin;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.SslMode;
import io.vertx.mysqlclient.impl.codec.MySQLCodec;
import io.vertx.mysqlclient.impl.codec.MySQLPacketDecoder;
import io.vertx.mysqlclient.impl.command.InitialHandshakeCommand;
import io.vertx.sqlclient.SqlConnectOptions;
import io.vertx.sqlclient.impl.Connection;
import io.vertx.sqlclient.impl.QueryResultHandler;
import io.vertx.sqlclient.impl.SocketConnectionBase;
import io.vertx.sqlclient.impl.command.CommandBase;
import io.vertx.sqlclient.impl.command.QueryCommandBase;
import io.vertx.sqlclient.impl.command.SimpleQueryCommand;
import io.vertx.sqlclient.impl.command.TxCommand;
import io.vertx.sqlclient.spi.DatabaseMetadata;

import java.nio.charset.Charset;
import java.util.Map;
import java.util.function.Predicate;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class MySQLSocketConnection extends SocketConnectionBase {

  private final MySQLConnectOptions connectOptions;
  public MySQLDatabaseMetadata metaData;
  private MySQLCodec codec;

  public MySQLSocketConnection(NetSocketInternal socket,
                               ClientMetrics clientMetrics,
                               MySQLConnectOptions connectOptions,
                               boolean cachePreparedStatements,
                               int preparedStatementCacheSize,
                               Predicate<String> preparedStatementCacheSqlFilter,
                               int pipeliningLimit,
                               EventLoopContext context) {
    super(socket, clientMetrics, cachePreparedStatements, preparedStatementCacheSize, preparedStatementCacheSqlFilter, pipeliningLimit, context);
    this.connectOptions = connectOptions;
  }

  void sendStartupMessage(String username,
                          String password,
                          String database,
                          MySQLCollation collation,
                          Buffer serverRsaPublicKey,
                          Map<String, String> properties,
                          SslMode sslMode,
                          int initialCapabilitiesFlags,
                          Charset charsetEncoding,
                          MySQLAuthenticationPlugin authenticationPlugin,
                          Promise<Connection> completionHandler) {
    InitialHandshakeCommand cmd = new InitialHandshakeCommand(this, username, password, database, collation, serverRsaPublicKey, properties, sslMode, initialCapabilitiesFlags, charsetEncoding, authenticationPlugin);
    schedule(context, cmd).onComplete(completionHandler);
  }

  @Override
  protected SqlConnectOptions connectOptions() {
    return connectOptions;
  }

  @Override
  public void init() {
    codec = new MySQLCodec(this);
    ChannelPipeline pipeline = socket.channelHandlerContext().pipeline();
    pipeline.addBefore("handler", "codec", codec);
    pipeline.addBefore("codec", "packetDecoder", new MySQLPacketDecoder());
    super.init();
  }

  @Override
  protected <R> void doSchedule(CommandBase<R> cmd, Handler<AsyncResult<R>> handler) {
    if (cmd instanceof TxCommand) {
      TxCommand<R> tx = (TxCommand<R>) cmd;
      SimpleQueryCommand<Void> cmd2 = new SimpleQueryCommand<>(
        tx.kind.sql,
        false,
        false,
        QueryCommandBase.NULL_COLLECTOR,
        QueryResultHandler.NOOP_HANDLER);
      super.doSchedule(cmd2, ar -> handler.handle(ar.map(tx.result)));
    } else {
      super.doSchedule(cmd, handler);
    }
  }

  public void upgradeToSsl(Handler<AsyncResult<Void>> completionHandler) {
    socket.upgradeToSsl(completionHandler);
  }

  @Override
  public DatabaseMetadata getDatabaseMetaData() {
    return metaData;
  }
}
