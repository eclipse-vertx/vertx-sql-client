/*
 * Copyright (c) 2011-2019 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.mssqlclient.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Promise;
import io.vertx.core.impl.ContextInternal;
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
import io.vertx.sqlclient.spi.DatabaseMetadata;

import java.util.Map;

class MSSQLSocketConnection extends SocketConnectionBase {
  
  public MSSQLDatabaseMetadata dbMetaData;
  
  MSSQLSocketConnection(NetSocketInternal socket,
                        boolean cachePreparedStatements,
                        int preparedStatementCacheSize,
                        int preparedStatementCacheSqlLimit,
                        int pipeliningLimit,
                        ContextInternal context) {
    super(socket, cachePreparedStatements, preparedStatementCacheSize, preparedStatementCacheSqlLimit, pipeliningLimit, context);
  }

  // command response should show what capabilities server provides
  void sendPreLoginMessage(boolean ssl, Handler<AsyncResult<Void>> completionHandler) {
    PreLoginCommand cmd = new PreLoginCommand(ssl);
    Promise<Void> promise = Promise.promise();
    promise.future().onComplete(completionHandler);
    schedule(cmd, promise);
  }

  void sendLoginMessage(String username, String password, String database, Map<String, String> properties, Handler<AsyncResult<Connection>> completionHandler) {
    InitCommand cmd = new InitCommand(this, username, password, database, properties);
    Promise<Connection> promise = Promise.promise();
    promise.future().onComplete(completionHandler);
    schedule(cmd, promise);
  }

  @Override
  public void init() {
    ChannelPipeline pipeline = socket.channelHandlerContext().pipeline();
    MSSQLCodec.initPipeLine(pipeline);
    super.init();
  }
  
  @Override
  public DatabaseMetadata getDatabaseMetaData() {
    return dbMetaData;
  }
}
