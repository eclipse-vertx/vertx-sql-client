/*
 * Copyright (c) 2011-2020 Contributors to the Eclipse Foundation
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
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.impl.ContextInternal;
import io.vertx.core.impl.EventLoopContext;
import io.vertx.core.net.impl.NetSocketInternal;
import io.vertx.mssqlclient.impl.codec.MSSQLCodec;
import io.vertx.mssqlclient.impl.command.PreLoginCommand;
import io.vertx.sqlclient.impl.Connection;
import io.vertx.sqlclient.impl.QueryResultHandler;
import io.vertx.sqlclient.impl.SocketConnectionBase;
import io.vertx.sqlclient.impl.command.*;
import io.vertx.sqlclient.spi.DatabaseMetadata;

import java.util.Map;
import java.util.function.Predicate;

import static io.vertx.sqlclient.impl.command.TxCommand.Kind.BEGIN;

class MSSQLSocketConnection extends SocketConnectionBase {

  public MSSQLDatabaseMetadata dbMetaData;

  MSSQLSocketConnection(NetSocketInternal socket,
                        boolean cachePreparedStatements,
                        int preparedStatementCacheSize,
                        Predicate<String> preparedStatementCacheSqlFilter,
                        int pipeliningLimit,
                        EventLoopContext context) {
    super(socket, cachePreparedStatements, preparedStatementCacheSize, preparedStatementCacheSqlFilter, pipeliningLimit, context);
  }

  // TODO RETURN FUTURE ???
  // command response should show what capabilities server provides
  void sendPreLoginMessage(boolean ssl, Handler<AsyncResult<Void>> completionHandler) {
    PreLoginCommand cmd = new PreLoginCommand(ssl);
    schedule(context, cmd).onComplete(completionHandler);
  }

  // TODO RETURN FUTURE ???
  void sendLoginMessage(String username, String password, String database, Map<String, String> properties, Handler<AsyncResult<Connection>> completionHandler) {
    InitCommand cmd = new InitCommand(this, username, password, database, properties);
    schedule(context, cmd).onComplete(completionHandler);
  }

  @Override
  public void init() {
    ChannelPipeline pipeline = socket.channelHandlerContext().pipeline();
    MSSQLCodec.initPipeLine(pipeline);
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
    return dbMetaData;
  }
}
