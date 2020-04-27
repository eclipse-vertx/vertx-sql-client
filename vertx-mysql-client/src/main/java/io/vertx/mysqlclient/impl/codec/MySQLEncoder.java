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
import io.vertx.mysqlclient.impl.MySQLSocketConnection;
import io.vertx.mysqlclient.impl.command.*;
import io.vertx.sqlclient.impl.command.*;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;

class MySQLEncoder extends ChannelOutboundHandlerAdapter {

  private final ArrayDeque<CommandCodec<?, ?>> inflight;
  ChannelHandlerContext chctx;

  int clientCapabilitiesFlag;
  Charset charset;
  Charset encodingCharset;
  MySQLSocketConnection socketConnection;

  MySQLEncoder(ArrayDeque<CommandCodec<?, ?>> inflight, MySQLSocketConnection mySQLSocketConnection) {
    this.inflight = inflight;
    this.socketConnection = mySQLSocketConnection;
    this.charset = StandardCharsets.UTF_8;
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

  void write(CommandBase<?> cmd) {
    CommandCodec<?, ?> codec = wrap(cmd);
    codec.completionHandler = resp -> {
      CommandCodec<?, ?> c = inflight.poll();
      resp.cmd = (CommandBase) c.cmd;
      chctx.fireChannelRead(resp);
    };
    inflight.add(codec);
    codec.encode(this);
  }

  private CommandCodec<?, ?> wrap(CommandBase<?> cmd) {
    if (cmd instanceof InitialHandshakeCommand) {
      return new InitialHandshakeCommandCodec((InitialHandshakeCommand) cmd);
    } else if (cmd instanceof SimpleQueryCommand) {
      return new SimpleQueryCommandCodec<>((SimpleQueryCommand<?>) cmd);
    } else if (cmd instanceof ExtendedQueryCommand) {
      return new ExtendedQueryCommandCodec<>((ExtendedQueryCommand<?>) cmd);
    } else if (cmd instanceof ExtendedBatchQueryCommand<?>) {
      return new ExtendedBatchQueryCommandCodec<>((ExtendedBatchQueryCommand<?>) cmd);
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
      System.out.println("Unsupported command " + cmd);
      throw new UnsupportedOperationException("Todo");
    }
  }

}
