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
import io.vertx.sqlclient.codec.CommandResponse;

import java.nio.charset.Charset;

class MySQLEncoder extends ChannelOutboundHandlerAdapter {

  private final MySQLCodec codec;
  ChannelHandlerContext chctx;

  int clientCapabilitiesFlag;
  Charset encodingCharset;
  MySQLSocketConnection socketConnection;

  MySQLEncoder(MySQLCodec codec, MySQLSocketConnection mySQLSocketConnection) {
    this.codec = codec;
    this.socketConnection = mySQLSocketConnection;
  }

  @Override
  public void handlerAdded(ChannelHandlerContext ctx) {
    chctx = ctx;
  }

  @Override
  public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
    if (msg instanceof MySQLCommand<?, ?>) {
      MySQLCommand<?, ?> cmd = (MySQLCommand<?, ?>) msg;
      write(cmd);
      codec.checkFireAndForgetCommands();
    } else {
      super.write(ctx, msg, promise);
    }
  }

  void write(MySQLCommand<?, ?> cmd) {
    codec.add(cmd);
    cmd.encode(this);
  }

  final void fireCommandResponse(CommandResponse<?> commandResponse) {
    codec.poll();
    chctx.fireChannelRead(commandResponse);
  }
}
