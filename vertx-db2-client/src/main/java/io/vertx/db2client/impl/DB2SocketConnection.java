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

import java.util.Map;

import io.netty.channel.ChannelPipeline;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.impl.ContextInternal;
import io.vertx.core.net.impl.NetSocketInternal;
import io.vertx.db2client.impl.codec.DB2Codec;
import io.vertx.db2client.impl.command.InitialHandshakeCommand;
import io.vertx.db2client.impl.drda.ConnectionMetaData;
import io.vertx.sqlclient.impl.Connection;
import io.vertx.sqlclient.impl.QueryResultHandler;
import io.vertx.sqlclient.impl.SocketConnectionBase;
import io.vertx.sqlclient.impl.command.CommandBase;
import io.vertx.sqlclient.impl.command.CommandResponse;
import io.vertx.sqlclient.impl.command.QueryCommandBase;
import io.vertx.sqlclient.impl.command.SimpleQueryCommand;
import io.vertx.sqlclient.impl.command.TxCommand;

public class DB2SocketConnection extends SocketConnectionBase {

    private DB2Codec codec;
    private Handler<Void> closeHandler;

    public DB2SocketConnection(NetSocketInternal socket,
    		boolean cachePreparedStatements,
            int preparedStatementCacheSize,
            int preparedStatementCacheSqlLimit,
            int pipeliningLimit,
            ContextInternal context) {
        super(socket, cachePreparedStatements, preparedStatementCacheSize, preparedStatementCacheSqlLimit, pipeliningLimit, context);
    }

    void sendStartupMessage(String username,
            String password,
            String database,
            Map<String, String> properties,
            Promise<Connection> completionHandler) {
        InitialHandshakeCommand cmd = new InitialHandshakeCommand(this, username, password, database, properties);
        schedule(cmd, completionHandler);
    }

    @Override
    public void init() {
        codec = new DB2Codec(this);
        ChannelPipeline pipeline = socket.channelHandlerContext().pipeline();
        pipeline.addBefore("handler", "codec", codec);
        super.init();
    }

    @Override
    protected <R> void doSchedule(CommandBase<R> cmd, Handler<AsyncResult<R>> handler) {
      if (cmd instanceof TxCommand) {
        TxCommand txCmd = (TxCommand) cmd;
        if (txCmd.kind == TxCommand.Kind.BEGIN) {
          // DB2 always implicitly starts a transaction with each query, and does
          // not support the 'BEGIN' keyword. Instead we can no-op BEGIN commands
          cmd.handler = handler;
          cmd.complete(CommandResponse.success((R) null).toAsyncResult());
        } else {
          SimpleQueryCommand<Void> cmd2 = new SimpleQueryCommand<>(
              txCmd.kind.sql,
              false,
              false,
              QueryCommandBase.NULL_COLLECTOR,
              QueryResultHandler.NOOP_HANDLER);
            super.doSchedule(cmd2, ar -> handler.handle(ar.mapEmpty()));

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

    public DB2SocketConnection closeHandler(Handler<Void> handler) {
      closeHandler = handler;
      return this;
    }
}
