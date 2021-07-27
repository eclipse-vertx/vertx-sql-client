/*
 * Copyright (c) 2011-2021 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.mssqlclient.impl;

import io.netty.channel.ChannelPipeline;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.impl.EventLoopContext;
import io.vertx.core.net.impl.NetSocketInternal;
import io.vertx.mssqlclient.impl.codec.TdsMessageCodec;
import io.vertx.mssqlclient.impl.codec.TdsPacketDecoder;
import io.vertx.mssqlclient.impl.command.PreLoginCommand;
import io.vertx.mssqlclient.impl.command.PreLoginResponse;
import io.vertx.sqlclient.impl.Connection;
import io.vertx.sqlclient.impl.QueryResultHandler;
import io.vertx.sqlclient.impl.SocketConnectionBase;
import io.vertx.sqlclient.impl.command.*;
import io.vertx.sqlclient.spi.DatabaseMetadata;

import java.util.Map;
import java.util.function.Predicate;

import static io.vertx.sqlclient.impl.command.TxCommand.Kind.BEGIN;

class MSSQLSocketConnection extends SocketConnectionBase {

  private final int packetSize;

  private MSSQLDatabaseMetadata databaseMetadata;

  MSSQLSocketConnection(NetSocketInternal socket,
                        int packetSize,
                        boolean cachePreparedStatements,
                        int preparedStatementCacheSize,
                        Predicate<String> preparedStatementCacheSqlFilter,
                        int pipeliningLimit,
                        EventLoopContext context) {
    super(socket, cachePreparedStatements, preparedStatementCacheSize, preparedStatementCacheSqlFilter, pipeliningLimit, context);
    this.packetSize = packetSize;
  }

  Future<Byte> sendPreLoginMessage(boolean ssl) {
    PreLoginCommand cmd = new PreLoginCommand(ssl);
    return schedule(context, cmd).onSuccess(resp -> setDatabaseMetadata(resp.metadata())).map(PreLoginResponse::encryptionLevel);
  }

  Future<Connection> sendLoginMessage(String username, String password, String database, Map<String, String> properties) {
    InitCommand cmd = new InitCommand(this, username, password, database, properties);
    return schedule(context, cmd);
  }

  @Override
  public void init() {
    ChannelPipeline pipeline = socket.channelHandlerContext().pipeline();
    pipeline.addBefore("handler", "messageCodec", new TdsMessageCodec(packetSize));
    pipeline.addBefore("messageCodec", "packetDecoder", new TdsPacketDecoder());
    super.init();
  }

  @Override
  protected <R> void doSchedule(CommandBase<R> cmd, Handler<AsyncResult<R>> handler) {
    if (cmd instanceof TxCommand) {
      TxCommand<R> tx = (TxCommand<R>) cmd;
      String sql = tx.kind == BEGIN ? "BEGIN TRANSACTION":tx.kind.sql;
      SimpleQueryCommand<Void> cmd2 = new SimpleQueryCommand<>(
        sql,
        false,
        false,
        QueryCommandBase.NULL_COLLECTOR,
        QueryResultHandler.NOOP_HANDLER);
      super.doSchedule(cmd2, ar -> handler.handle(ar.map(tx.result)));
    } else {
      super.doSchedule(cmd, handler);
    }
  }

  @Override
  public DatabaseMetadata getDatabaseMetaData() {
    return databaseMetadata;
  }

  private void setDatabaseMetadata(MSSQLDatabaseMetadata metadata) {
    this.databaseMetadata = metadata;
  }
}
