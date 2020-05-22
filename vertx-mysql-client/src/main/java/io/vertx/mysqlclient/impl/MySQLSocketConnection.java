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
import io.vertx.core.impl.ContextInternal;
import io.vertx.core.net.impl.NetSocketInternal;
import io.vertx.mysqlclient.SslMode;
import io.vertx.mysqlclient.impl.codec.MySQLCodec;
import io.vertx.mysqlclient.impl.command.InitialHandshakeCommand;
import io.vertx.mysqlclient.impl.util.TransactionSqlBuilder;
import io.vertx.sqlclient.impl.Connection;
import io.vertx.sqlclient.impl.QueryResultHandler;
import io.vertx.sqlclient.impl.SocketConnectionBase;
import io.vertx.sqlclient.impl.command.*;

import java.nio.charset.Charset;
import java.util.Map;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class MySQLSocketConnection extends SocketConnectionBase {

  private MySQLCodec codec;

  public MySQLSocketConnection(NetSocketInternal socket,
                               boolean cachePreparedStatements,
                               int preparedStatementCacheSize,
                               int preparedStatementCacheSqlLimit,
                               ContextInternal context) {
    super(socket, cachePreparedStatements, preparedStatementCacheSize, preparedStatementCacheSqlLimit, 1, context);
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
                          Promise<Connection> completionHandler) {
    InitialHandshakeCommand cmd = new InitialHandshakeCommand(this, username, password, database, collation, serverRsaPublicKey, properties, sslMode, initialCapabilitiesFlags, charsetEncoding);
    schedule(cmd, completionHandler);
  }

  @Override
  public void init() {
    codec = new MySQLCodec(this);
    ChannelPipeline pipeline = socket.channelHandlerContext().pipeline();
    pipeline.addBefore("handler", "codec", codec);
    super.init();
  }

  @Override
  protected <R> void doSchedule(CommandBase<R> cmd, Handler<AsyncResult<R>> handler) {
    if (cmd instanceof TxCommand) {
      TxCommand<R> txCmd = (TxCommand<R>) cmd;
      String txSql;
      if (txCmd.kind == TxCommand.Kind.BEGIN) {
        StartTxCommand<R> startTxCommand = (StartTxCommand<R>) txCmd;

        if (startTxCommand.accessMode != null) {
          txSql = TransactionSqlBuilder.buildStartTxSql(startTxCommand.accessMode);
        } else {
          txSql = txCmd.kind.name();
        }

        if (startTxCommand.isolationLevel != null) {
          // MySQL could not set transaction level at the transaction start
          SimpleQueryCommand<Void> setTxIsolationLevelCmd = buildNoOpQueryCommand(TransactionSqlBuilder.buildSetTxIsolationLevelSql(startTxCommand.isolationLevel));
          super.doSchedule(new BiCommand<>(setTxIsolationLevelCmd, v -> Future.succeededFuture(buildNoOpQueryCommand(txSql))), ar -> handler.handle(ar.map(txCmd.result)));
          return;
        }
      } else {
        txSql = txCmd.kind.name();
      }
      super.doSchedule(buildNoOpQueryCommand(txSql), ar -> handler.handle(ar.map(txCmd.result)));
    } else {
      super.doSchedule(cmd, handler);
    }
  }

  public void upgradeToSsl(Handler<AsyncResult<Void>> completionHandler) {
    socket.upgradeToSsl(completionHandler);
  }

  private SimpleQueryCommand<Void> buildNoOpQueryCommand(String sql) {
    return new SimpleQueryCommand<>(
      sql,
      false,
      false,
      QueryCommandBase.NULL_COLLECTOR,
      QueryResultHandler.NOOP_HANDLER);
  }
}
