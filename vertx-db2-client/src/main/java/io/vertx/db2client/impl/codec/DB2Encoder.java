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
import io.vertx.sqlclient.impl.command.CloseConnectionCommand;
import io.vertx.sqlclient.impl.command.CloseStatementCommand;
import io.vertx.sqlclient.impl.command.CommandBase;
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
    	if (LOG.isDebugEnabled())
    		LOG.debug("writing command: " + msg);
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private CommandCodec<?, ?> wrap(CommandBase<?> cmd) {
        if (cmd instanceof InitialHandshakeCommand) {
            return new InitialHandshakeCommandCodec((InitialHandshakeCommand) cmd);
        } else if (cmd instanceof SimpleQueryCommand) {
            return new SimpleQueryCommandCodec((SimpleQueryCommand) cmd);
        } else if (cmd instanceof ExtendedQueryCommand) {
            return new ExtendedQueryCommandCodec((ExtendedQueryCommand) cmd);
            // } else if (cmd instanceof ExtendedBatchQueryCommand<?>) {
            // return new ExtendedBatchQueryCommandCodec<>((ExtendedBatchQueryCommand<?>)
            // cmd);
        } else if (cmd instanceof CloseConnectionCommand) {
            return new CloseConnectionCommandCodec((CloseConnectionCommand) cmd);
        } else if (cmd instanceof PrepareStatementCommand) {
            return new PrepareStatementCodec((PrepareStatementCommand) cmd);
        } else if (cmd instanceof CloseStatementCommand) {
            return new CloseStatementCommandCodec((CloseStatementCommand) cmd);
            // } else if (cmd instanceof CloseCursorCommand) {
            // return new ResetStatementCommandCodec((CloseCursorCommand) cmd);
//        } else if (cmd instanceof PingCommand) {
//            return new PingCommandCodec((PingCommand) cmd);
//        } else if (cmd instanceof InitDbCommand) {
//            return new InitDbCommandCodec((InitDbCommand) cmd);
            // } else if (cmd instanceof StatisticsCommand) {
            // return new StatisticsCommandCodec((StatisticsCommand) cmd);
            // } else if (cmd instanceof SetOptionCommand) {
            // return new SetOptionCommandCodec((SetOptionCommand) cmd);
            // } else if (cmd instanceof ResetConnectionCommand) {
            // return new ResetConnectionCommandCodec((ResetConnectionCommand) cmd);
            // } else if (cmd instanceof DebugCommand) {
            // return new DebugCommandCodec((DebugCommand) cmd);
            // } else if (cmd instanceof ChangeUserCommand) {
            // return new ChangeUserCommandCodec((ChangeUserCommand) cmd);
        } else {
            throw new UnsupportedOperationException("Unsupported command type: " + cmd);
        }
    }
}
