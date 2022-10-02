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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.CompositeByteBuf;
import io.netty.channel.*;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

import static io.vertx.mssqlclient.MSSQLConnectOptions.MAX_PACKET_SIZE;
import static io.vertx.mssqlclient.impl.codec.MessageStatus.END_OF_MESSAGE;
import static io.vertx.mssqlclient.impl.codec.MessageStatus.NORMAL;
import static io.vertx.mssqlclient.impl.codec.MessageType.PRE_LOGIN;
import static io.vertx.mssqlclient.impl.codec.TdsPacket.PACKET_HEADER_SIZE;

/**
 * While handshaking, SSL payload is encapsulated in TDS PRELOGIN packets.
 * <p>
 * This handler must be installed before the {@link io.netty.handler.ssl.SslHandler} in the channel pipeline.
 */
public class TdsSslHandshakeCodec extends CombinedChannelDuplexHandler<ChannelInboundHandler, ChannelOutboundHandler> {

  public TdsSslHandshakeCodec() {
    LengthFieldBasedFrameDecoder decoder = new LengthFieldBasedFrameDecoder(MAX_PACKET_SIZE, 2, 2, -4, PACKET_HEADER_SIZE);
    init(decoder, new Encoder());
  }

  private static class Encoder extends ChannelOutboundHandlerAdapter {

    private CompositeByteBuf accumulator;

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
      if (accumulator != null) {
        accumulator.release();
      }
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
      if (msg instanceof ByteBuf) {
        accumulate(ctx.alloc(), (ByteBuf) msg);
        promise.setSuccess();
      } else {
        super.write(ctx, msg, promise);
      }
    }

    private void accumulate(ByteBufAllocator alloc, ByteBuf byteBuf) {
      if (accumulator == null) {
        accumulator = alloc.compositeBuffer();
      }
      accumulator.addComponent(true, byteBuf);
    }

    @Override
    public void flush(ChannelHandlerContext ctx) throws Exception {
      if (accumulator != null) {
        ByteBuf header = ctx.alloc().ioBuffer(PACKET_HEADER_SIZE);
        header.writeByte(PRE_LOGIN);
        header.writeByte(NORMAL | END_OF_MESSAGE);
        header.writeShort(PACKET_HEADER_SIZE + accumulator.writerIndex());
        header.writeZero(4);
        ctx.write(header, ctx.voidPromise());
        ctx.writeAndFlush(accumulator, ctx.voidPromise());
        accumulator = null;
      } else {
        ctx.flush();
      }
    }
  }
}
