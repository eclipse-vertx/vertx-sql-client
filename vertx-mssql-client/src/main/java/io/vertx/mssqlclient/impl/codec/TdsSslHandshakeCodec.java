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
import io.vertx.mssqlclient.MSSQLConnectOptions;

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
    init(new Decoder(), new Encoder());
  }

  private static class Decoder extends LengthFieldBasedFrameDecoder {

    Decoder() {
      super(MSSQLConnectOptions.MAX_PACKET_SIZE, 2, 2, -4, 0);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
      ByteBuf byteBuf = (ByteBuf) super.decode(ctx, in);
      if (byteBuf == null) {
        return null;
      }

      int length = byteBuf.getUnsignedShort(2);

      return byteBuf.slice(PACKET_HEADER_SIZE, length - PACKET_HEADER_SIZE);
    }
  }

  private static class Encoder extends ChannelOutboundHandlerAdapter {

    private ByteBufAllocator alloc;
    private CompositeByteBuf accumulator;

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
      alloc = ctx.alloc();
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
      if (accumulator != null) {
        accumulator.release();
      }
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
      if (msg instanceof ByteBuf) {
        accumulate((ByteBuf) msg);
        promise.setSuccess();
      } else {
        super.write(ctx, msg, promise);
      }
    }

    private void accumulate(ByteBuf byteBuf) {
      if (accumulator == null) {
        accumulator = alloc.compositeBuffer();
      }
      accumulator.addComponent(true, byteBuf.retainedSlice());
    }

    @Override
    public void flush(ChannelHandlerContext ctx) throws Exception {
      if (accumulator != null) {
        ByteBuf header = alloc.ioBuffer(8);
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
