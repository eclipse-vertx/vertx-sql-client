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

package io.vertx.mssqlclient.impl.codec;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.vertx.mssqlclient.impl.command.PreLoginCommand;
import io.vertx.sqlclient.impl.command.*;

import java.util.ArrayDeque;

class TdsMessageEncoder extends ChannelOutboundHandlerAdapter {
  private final ArrayDeque<MSSQLCommandCodec<?, ?>> inflight;

  long transactionDescriptor;
  ChannelHandlerContext chctx;

  TdsMessageEncoder(ArrayDeque<MSSQLCommandCodec<?, ?>> inflight) {
    this.inflight = inflight;
  }

  @Override
  public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
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
    MSSQLCommandCodec<?, ?> codec = wrap(cmd);
    codec.completionHandler = resp -> {
      MSSQLCommandCodec<?, ?> c = inflight.poll();
      resp.cmd = (CommandBase) c.cmd;
      chctx.fireChannelRead(resp);
    };
    inflight.add(codec);
    codec.encode(this);
  }

  private MSSQLCommandCodec<?, ?> wrap(CommandBase<?> cmd) {
    if (cmd instanceof PreLoginCommand) {
      return new PreLoginCommandCodec((PreLoginCommand) cmd);
    } else if (cmd instanceof InitCommand) {
      return new InitCommandCodec((InitCommand) cmd);
    } else if (cmd instanceof SimpleQueryCommand) {
      return new SQLBatchCommandCodec((SimpleQueryCommand) cmd);
    } else if (cmd instanceof PrepareStatementCommand) {
      return new PrepareStatementCodec((PrepareStatementCommand) cmd);
    } else if (cmd instanceof ExtendedQueryCommand) {
      ExtendedQueryCommand<?> queryCmd = (ExtendedQueryCommand<?>) cmd;
      if (queryCmd.isBatch()) {
        throw new UnsupportedOperationException();
      } else {
        return new ExtendedQueryCommandCodec((ExtendedQueryCommand) cmd);
      }
    } else if (cmd instanceof CloseStatementCommand) {
      return new CloseStatementCommandCodec((CloseStatementCommand) cmd);
    } else if (cmd == CloseConnectionCommand.INSTANCE) {
      return new CloseConnectionCommandCodec((CloseConnectionCommand) cmd);
    } else {
      throw new UnsupportedOperationException();
    }
  }
}
