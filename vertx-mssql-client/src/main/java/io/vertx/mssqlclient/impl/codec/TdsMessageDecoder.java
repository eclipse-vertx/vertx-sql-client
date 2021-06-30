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

import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class TdsMessageDecoder extends ChannelInboundHandlerAdapter {

  private final TdsMessageCodec tdsMessageCodec;

  private ByteBufAllocator alloc;
  private TdsMessage message;

  public TdsMessageDecoder(TdsMessageCodec tdsMessageCodec) {
    this.tdsMessageCodec = tdsMessageCodec;
  }

  @Override
  public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
    alloc = ctx.alloc();
  }

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    TdsPacket tdsPacket = (TdsPacket) msg;
    if (message == null) {
      message = TdsMessage.createForDecoding(alloc, tdsPacket);
    } else {
      message.aggregate(tdsPacket);
    }
    if (tdsPacket.status() == MessageStatus.END_OF_MESSAGE) {
      decodeMessage();
    }
  }

  @Override
  public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
    releaseMessage();
  }

  private void releaseMessage() {
    if (message != null) {
      message.release();
      message = null;
    }
  }

  private void decodeMessage() {
    try {
      MSSQLCommandCodec<?, ?> commandCodec = tdsMessageCodec.peek();
      if (commandCodec == null) {
        throw new IllegalStateException("No command codec for message of type [" + message.type() + "]");
      }
      commandCodec.decode(message.content());
    } finally {
      releaseMessage();
    }
  }
}
