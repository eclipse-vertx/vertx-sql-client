/*
 * Copyright (C) 2017 Julien Viet
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
 *
 */

package com.julienviet.pgclient.impl.codec.decoder;

import com.julienviet.pgclient.impl.SocketConnection;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.DecoderException;
import io.vertx.core.Future;

import static com.julienviet.pgclient.impl.codec.decoder.message.type.MessageType.SSL_NO;
import static com.julienviet.pgclient.impl.codec.decoder.message.type.MessageType.SSL_YES;

public class InitiateSslHandler extends ChannelInboundHandlerAdapter {

  private static final int code = 80877103;
  private final SocketConnection conn;
  private final Future<Void> upgradeFuture;

  public InitiateSslHandler(SocketConnection conn, Future<Void> upgradeFuture) {
    this.conn = conn;
    this.upgradeFuture = upgradeFuture;
  }

  @Override
  public void channelActive(ChannelHandlerContext ctx) throws Exception {
    ByteBuf byteBuf = Unpooled.buffer();
    byteBuf.writeInt(0);
    byteBuf.writeInt(code);
//    out.writeInt(0x12345679);
    byteBuf.setInt(0, byteBuf.writerIndex());
    ctx.writeAndFlush(byteBuf);
    super.channelActive(ctx);
  }

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    // This must be a single byte buffer - after that follow the SSL handshake
    ByteBuf byteBuf = (ByteBuf) msg;
    byte b = byteBuf.getByte(0);
    byteBuf.release();
    switch (b) {
      case SSL_YES: {
        conn.upgradeToSSL(v -> {
          ctx.pipeline().remove(this);
          upgradeFuture.complete();
        });
        break;
      }
      case SSL_NO: {
        // This case is not tested as our test db is configured for SSL
        ctx.fireExceptionCaught(new Exception("Postgres does not handle SSL"));
        break;
      }
      default:
        ctx.fireExceptionCaught(new Exception("Invalid connection data"));
        break;
    }
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    if (cause instanceof DecoderException) {
      DecoderException err = (DecoderException) cause;
      cause = err.getCause();
    }
    upgradeFuture.fail(cause);
  }
}
