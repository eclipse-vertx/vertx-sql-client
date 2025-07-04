/*
 * Copyright (C) 2019,2020 IBM Corporation
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
 */
package io.vertx.db2client.impl;

import io.netty.channel.ChannelPipeline;
import io.vertx.core.Completable;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.internal.ContextInternal;
import io.vertx.core.internal.net.NetSocketInternal;
import io.vertx.core.spi.metrics.ClientMetrics;
import io.vertx.db2client.DB2ConnectOptions;
import io.vertx.db2client.DB2Exception;
import io.vertx.db2client.impl.codec.ConnectionState;
import io.vertx.db2client.impl.codec.DB2CommandMessage;
import io.vertx.db2client.impl.codec.DB2Codec;
import io.vertx.db2client.impl.codec.DB2PreparedStatement;
import io.vertx.db2client.impl.codec.ExtendedBatchQueryDB2CommandMessage;
import io.vertx.db2client.impl.codec.ExtendedQueryDB2CommandMessage;
import io.vertx.db2client.impl.command.InitialHandshakeCommand;
import io.vertx.db2client.impl.drda.ConnectionMetaData;
import io.vertx.db2client.impl.drda.SQLState;
import io.vertx.db2client.impl.drda.SqlCode;
import io.vertx.sqlclient.SqlConnectOptions;
import io.vertx.sqlclient.codec.CommandMessage;
import io.vertx.sqlclient.spi.connection.Connection;
import io.vertx.sqlclient.internal.PreparedStatement;
import io.vertx.sqlclient.internal.QueryResultHandler;
import io.vertx.sqlclient.codec.SocketConnectionBase;
import io.vertx.sqlclient.spi.DatabaseMetadata;
import io.vertx.sqlclient.spi.protocol.CommandBase;
import io.vertx.sqlclient.spi.protocol.ExtendedQueryCommand;
import io.vertx.sqlclient.spi.protocol.SimpleQueryCommand;
import io.vertx.sqlclient.spi.protocol.TxCommand;

import java.util.Map;
import java.util.function.Predicate;

public class DB2SocketConnection extends SocketConnectionBase {

  private final DB2ConnectOptions connectOptions;
  private DB2Codec codec;
  private Handler<Void> closeHandler;
  public final ConnectionMetaData connMetadata = new ConnectionMetaData();
  public ConnectionState status = ConnectionState.CONNECTING;

  public DB2SocketConnection(NetSocketInternal socket,
      ClientMetrics clientMetrics,
      DB2ConnectOptions connectOptions,
      boolean cachePreparedStatements,
      int preparedStatementCacheSize,
      Predicate<String> preparedStatementCacheSqlFilter,
      int pipeliningLimit,
                             ContextInternal context) {
    super(socket, clientMetrics, cachePreparedStatements, preparedStatementCacheSize, preparedStatementCacheSqlFilter, pipeliningLimit, context);
    this.connectOptions = connectOptions;
  }

  @Override
  protected <R> void fail(CommandBase<R> command, Completable<R> handler, Throwable err) {
    if (status == ConnectionState.CONNECTING && command instanceof InitialHandshakeCommand) {
      // Sometimes DB2 closes the connection when sending an invalid Database name.
      // -4499 = A fatal error occurred that resulted in a disconnect from the data
      // source.
      // 08001 = "The connection was unable to be established"
      err = new DB2Exception("The connection was closed by the database server.", SqlCode.CONNECTION_REFUSED,
        SQLState.AUTH_DATABASE_CONNECTION_REFUSED);
    }
    super.fail(command, handler, err);
  }

  // TODO RETURN FUTURE ???
  void sendStartupMessage(String username,
      String password,
      String database,
      Map<String, String> properties,
      Promise<Connection> completionHandler) {
    InitialHandshakeCommand cmd = new InitialHandshakeCommand(this, username, password, database, properties);
    schedule(context, cmd).onComplete(completionHandler);
  }

  @Override
  protected SqlConnectOptions connectOptions() {
    return connectOptions;
  }

  @Override
  public void init() {
    codec = new DB2Codec(this);
    ChannelPipeline pipeline = socket.channelHandlerContext().pipeline();
    pipeline.addBefore("handler", "codec", codec);
    super.init();
  }

  @Override
  protected CommandMessage<?, ?> toMessage(ExtendedQueryCommand<?> command, PreparedStatement preparedStatement) {
    if (command.isBatch()) {
      return new ExtendedBatchQueryDB2CommandMessage<>(command, (DB2PreparedStatement) preparedStatement);
    } else {
      return new ExtendedQueryDB2CommandMessage(command, (DB2PreparedStatement) preparedStatement);
    }
  }

  @Override
  protected CommandMessage<?, ?> toMessage(CommandBase<?> command) {
    return DB2CommandMessage.wrap(command);
  }

  @Override
  protected <R> void doSchedule(CommandBase<R> cmd, Completable<R> handler) {
    if (cmd instanceof TxCommand) {
      TxCommand<R> txCmd = (TxCommand<R>) cmd;
      if (txCmd.kind() == TxCommand.Kind.BEGIN) {
        // DB2 always implicitly starts a transaction with each query, and does
        // not support the 'BEGIN' keyword. Instead we can no-op BEGIN commands
        handler.succeed(txCmd.result());
      } else {
        SimpleQueryCommand<Void> cmd2 = new SimpleQueryCommand<>(txCmd.kind().sql(), false, false,
            SocketConnectionBase.NULL_COLLECTOR, QueryResultHandler.NOOP_HANDLER);
        super.doSchedule(cmd2, (res, err) -> handler.complete(txCmd.result(), err));

      }
    } else {
      super.doSchedule(cmd, handler);
    }
  }

  @Override
  public void handleClose(Throwable t) {
    super.handleClose(t);
    context().runOnContext(closeHandler);
  }

  @Override
  public String system() {
    return "db2";
  }

  @Override
  public DatabaseMetadata databaseMetadata() {
    return connMetadata.getDbMetadata();
  }

  public DB2SocketConnection closeHandler(Handler<Void> handler) {
    closeHandler = handler;
    return this;
  }
}
