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
package io.vertx.db2client.impl.codec;

import java.util.ArrayDeque;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.db2client.impl.DB2SocketConnection;
import io.vertx.db2client.impl.command.InitialHandshakeCommand;
import io.vertx.db2client.impl.command.PingCommand;
import io.vertx.sqlclient.impl.command.CloseConnectionCommand;
import io.vertx.sqlclient.impl.command.CloseCursorCommand;
import io.vertx.sqlclient.impl.command.CloseStatementCommand;
import io.vertx.sqlclient.impl.command.CommandBase;
import io.vertx.sqlclient.impl.command.CommandResponse;
import io.vertx.sqlclient.impl.command.ExtendedBatchQueryCommand;
import io.vertx.sqlclient.impl.command.ExtendedQueryCommand;
import io.vertx.sqlclient.impl.command.PrepareStatementCommand;
import io.vertx.sqlclient.impl.command.SimpleQueryCommand;

class DB2Encoder extends ChannelOutboundHandlerAdapter {
	
	private static final Logger LOG = LoggerFactory.getLogger(DB2Encoder.class);

    private final ArrayDeque<CommandCodec<?, ?>> inflight;
    ChannelHandlerContext chctx;

    DB2SocketConnection socketConnection;

    DB2Encoder(ArrayDeque<CommandCodec<?, ?>> inflight, DB2SocketConnection db2SocketConnection) {
        this.inflight = inflight;
        this.socketConnection = db2SocketConnection;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        chctx = ctx;
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof CommandBase<?>) {
            CommandBase<?> cmd = (CommandBase<?>) msg;
            write(cmd);
        } else {
            super.write(ctx, msg, promise);
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    void write(CommandBase<?> cmd) {
        CommandCodec<?, ?> codec = wrap(cmd);
        codec.completionHandler = resp -> {
            CommandCodec<?, ?> c = inflight.poll();
            resp.cmd = (CommandBase) c.cmd;
            chctx.fireChannelRead(resp);
        };
        inflight.add(codec);
        try {
            codec.encode(this);
        } catch (Throwable e) {
            LOG.error("FATAL: Unable to encode command: " + cmd, e);
            codec.completionHandler.handle(CommandResponse.failure(e));
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private CommandCodec<?, ?> wrap(CommandBase<?> cmd) {
    	CommandCodec<?,?> codec = null;
        if (cmd instanceof InitialHandshakeCommand) {
            codec = new InitialHandshakeCommandCodec((InitialHandshakeCommand) cmd);
        } else if (cmd instanceof SimpleQueryCommand) {
            codec = new SimpleQueryCommandCodec((SimpleQueryCommand) cmd);
        } else if (cmd instanceof ExtendedQueryCommand) {
            codec = new ExtendedQueryCommandCodec((ExtendedQueryCommand) cmd);
        } else if (cmd instanceof ExtendedBatchQueryCommand<?>) {
          codec = new ExtendedBatchQueryCommandCodec<>((ExtendedBatchQueryCommand<?>) cmd);
        } else if (cmd instanceof CloseConnectionCommand) {
            codec = new CloseConnectionCommandCodec((CloseConnectionCommand) cmd);
        } else if (cmd instanceof PrepareStatementCommand) {
            codec = new PrepareStatementCodec((PrepareStatementCommand) cmd);
        } else if (cmd instanceof CloseStatementCommand) {
            codec = new CloseStatementCommandCodec((CloseStatementCommand) cmd);
        } else if (cmd instanceof CloseCursorCommand) {
            codec = new CloseCursorCommandCodec((CloseCursorCommand) cmd);
        } else if (cmd instanceof PingCommand) {
            codec = new PingCommandCodec((PingCommand) cmd);
//        } else if (cmd instanceof InitDbCommand) {
//            codec = new InitDbCommandCodec((InitDbCommand) cmd);
            // } else if (cmd instanceof StatisticsCommand) {
            // codec = new StatisticsCommandCodec((StatisticsCommand) cmd);
            // } else if (cmd instanceof SetOptionCommand) {
            // codec = new SetOptionCommandCodec((SetOptionCommand) cmd);
            // } else if (cmd instanceof ResetConnectionCommand) {
            // codec = new ResetConnectionCommandCodec((ResetConnectionCommand) cmd);
            // } else if (cmd instanceof DebugCommand) {
            // codec = new DebugCommandCodec((DebugCommand) cmd);
            // } else if (cmd instanceof ChangeUserCommand) {
            // codec = new ChangeUserCommandCodec((ChangeUserCommand) cmd);
        } else {
            UnsupportedOperationException uoe = new UnsupportedOperationException("Unsupported command type: " + cmd);
            LOG.error(uoe);
            throw uoe;
        }
        if (LOG.isDebugEnabled())
    		LOG.debug(">>> ENCODE " + codec);
        return codec;
    }
    
}
