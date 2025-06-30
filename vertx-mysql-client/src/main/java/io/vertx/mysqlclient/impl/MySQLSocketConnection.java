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
import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.internal.ContextInternal;
import io.vertx.core.net.ClientSSLOptions;
import io.vertx.core.internal.net.NetSocketInternal;
import io.vertx.core.spi.metrics.ClientMetrics;
import io.vertx.mysqlclient.MySQLAuthenticationPlugin;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.SslMode;
import io.vertx.mysqlclient.impl.codec.ClearCachedStatementsEvent;
import io.vertx.mysqlclient.impl.codec.CommandCodec;
import io.vertx.mysqlclient.impl.codec.MySQLCodec;
import io.vertx.mysqlclient.impl.codec.MySQLPacketDecoder;
import io.vertx.mysqlclient.impl.command.InitialHandshakeCommand;
import io.vertx.sqlclient.SqlConnectOptions;
import io.vertx.sqlclient.impl.CommandMessage;
import io.vertx.sqlclient.internal.Connection;
import io.vertx.sqlclient.internal.QueryResultHandler;
import io.vertx.sqlclient.impl.SocketConnectionBase;
import io.vertx.sqlclient.internal.command.CommandBase;
import io.vertx.sqlclient.internal.command.QueryCommandBase;
import io.vertx.sqlclient.internal.command.SimpleQueryCommand;
import io.vertx.sqlclient.internal.command.TxCommand;
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
                               ContextInternal context) {
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
                          ClientSSLOptions sslOptions,
                          int initialCapabilitiesFlags,
                          Charset charsetEncoding,
                          MySQLAuthenticationPlugin authenticationPlugin,
                          Promise<Connection> completionHandler) {
    InitialHandshakeCommand cmd = new InitialHandshakeCommand(this, username, password, database, collation, serverRsaPublicKey, properties, sslMode, sslOptions, initialCapabilitiesFlags, charsetEncoding, authenticationPlugin);
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
  protected CommandMessage<?, ?> toMessage(CommandBase<?> command) {
    return CommandCodec.wrap(command);
  }

  @Override
  protected <R> void doSchedule(CommandBase<R> cmd, Completable<R> handler) {
    if (cmd instanceof TxCommand) {
      TxCommand<R> tx = (TxCommand<R>) cmd;
      SimpleQueryCommand<Void> cmd2 = new SimpleQueryCommand<>(
        tx.kind.sql,
        false,
        false,
        QueryCommandBase.NULL_COLLECTOR,
        QueryResultHandler.NOOP_HANDLER);
      super.doSchedule(cmd2, (res, err) -> handler.complete(tx.result, err));
    } else {
      super.doSchedule(cmd, handler);
    }
  }

  @Override
  protected void handleMessage(Object msg) {
    if (msg == ClearCachedStatementsEvent.INSTANCE) {
      clearCachedStatements();
    } else {
      super.handleMessage(msg);
    }
  }

  private void clearCachedStatements() {
    if (this.psCache != null) {
      this.psCache.clear();
    }
  }

  public Future<Void> upgradeToSsl(ClientSSLOptions sslOptions) {
    return socket.upgradeToSsl(sslOptions);
  }

  @Override
  public String system() {
    return metaData.system();
  }

  @Override
  public DatabaseMetadata getDatabaseMetaData() {
    return metaData;
  }
}
