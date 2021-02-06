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

package io.vertx.mysqlclient.impl.codec;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.mysqlclient.impl.MySQLSocketConnection;
import io.vertx.mysqlclient.impl.command.*;
import io.vertx.sqlclient.impl.command.*;

import java.nio.charset.Charset;
import java.util.ArrayDeque;

class MySQLEncoder extends ChannelOutboundHandlerAdapter {

  private static final Logger LOGGER = LoggerFactory.getLogger(MySQLEncoder.class);

  private final ArrayDeque<CommandCodec<?, ?>> inflight;
  ChannelHandlerContext chctx;

  int clientCapabilitiesFlag;
  Charset encodingCharset;
  MySQLSocketConnection socketConnection;

  MySQLEncoder(ArrayDeque<CommandCodec<?, ?>> inflight, MySQLSocketConnection mySQLSocketConnection) {
    this.inflight = inflight;
    this.socketConnection = mySQLSocketConnection;
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
      checkFireAndForgetCommands();
    } else {
      super.write(ctx, msg, promise);
    }
  }

  void write(CommandBase<?> cmd) {
    CommandCodec<?, ?> codec = wrap(cmd);
    inflight.add(codec);
    codec.encode(this);
  }

  private void checkFireAndForgetCommands() {
    // check if there is any completed command
    CommandCodec<?, ?> commandCodec;
    while ((commandCodec = inflight.peek()) != null && commandCodec.receiveNoResponsePacket()) {
      commandCodec.decodePayload(null, 0);
    }
  }

  final void onCommandResponse(CommandResponse<?> commandResponse) {
    CommandCodec<?, ?> c = inflight.poll();
    commandResponse.cmd = (CommandBase) c.cmd;
    chctx.fireChannelRead(commandResponse);
  }

  private CommandCodec<?, ?> wrap(CommandBase<?> cmd) {
    if (cmd instanceof InitialHandshakeCommand) {
      return new InitialHandshakeCommandCodec((InitialHandshakeCommand) cmd);
    } else if (cmd instanceof SimpleQueryCommand) {
      return new SimpleQueryCommandCodec<>((SimpleQueryCommand<?>) cmd);
    } else if (cmd instanceof ExtendedQueryCommand) {
      ExtendedQueryCommand<?> queryCmd = (ExtendedQueryCommand<?>) cmd;
      if (queryCmd.isBatch()) {
        return new ExtendedBatchQueryCommandCodec<>(queryCmd);
      } else {
        return new ExtendedQueryCommandCodec<>(queryCmd);
      }
    } else if (cmd instanceof CloseConnectionCommand) {
      return new CloseConnectionCommandCodec((CloseConnectionCommand) cmd);
    } else if (cmd instanceof PrepareStatementCommand) {
      return new PrepareStatementCodec((PrepareStatementCommand) cmd);
    } else if (cmd instanceof CloseStatementCommand) {
      return new CloseStatementCommandCodec((CloseStatementCommand) cmd);
    } else if (cmd instanceof CloseCursorCommand) {
      return new ResetStatementCommandCodec((CloseCursorCommand) cmd);
    } else if (cmd instanceof PingCommand) {
      return new PingCommandCodec((PingCommand) cmd);
    } else if (cmd instanceof InitDbCommand) {
      return new InitDbCommandCodec((InitDbCommand) cmd);
    } else if (cmd instanceof StatisticsCommand) {
      return new StatisticsCommandCodec((StatisticsCommand) cmd);
    } else if (cmd instanceof SetOptionCommand) {
      return new SetOptionCommandCodec((SetOptionCommand) cmd);
    } else if (cmd instanceof ResetConnectionCommand) {
      return new ResetConnectionCommandCodec((ResetConnectionCommand) cmd);
    } else if (cmd instanceof DebugCommand) {
      return new DebugCommandCodec((DebugCommand) cmd);
    } else if (cmd instanceof ChangeUserCommand) {
      return new ChangeUserCommandCodec((ChangeUserCommand) cmd);
    } else {
      LOGGER.error("Unsupported command " + cmd);
      throw new UnsupportedOperationException("Todo");
    }
  }

}
