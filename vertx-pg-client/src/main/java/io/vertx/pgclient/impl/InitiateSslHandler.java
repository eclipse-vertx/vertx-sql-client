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

package io.vertx.pgclient.impl;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.DecoderException;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.pgclient.impl.codec.PgProtocolConstants;
import io.vertx.sqlclient.impl.SocketConnectionBase;
import io.vertx.core.VertxException;

public class InitiateSslHandler extends ChannelInboundHandlerAdapter {

  private static final int code = 80877103;
  private final SocketConnectionBase conn;
  private final Promise<Void> upgradePromise;

  public InitiateSslHandler(SocketConnectionBase conn, Promise<Void> upgradePromise) {
    this.conn = conn;
    this.upgradePromise = upgradePromise;
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
      case PgProtocolConstants.MESSAGE_TYPE_SSL_YES: {
        Handler handler = o -> {
          if (o instanceof AsyncResult) {
            AsyncResult res = (AsyncResult) o;
            if (res.failed()) {
              // Connection close will fail the promise
              return;
            }
          }
          ctx.pipeline().remove(this);
          upgradePromise.complete();
        };
        conn.socket().upgradeToSsl(handler);
        break;
      }
      case PgProtocolConstants.MESSAGE_TYPE_SSL_NO: {
        upgradePromise.fail(new Exception("Postgres Server does not handle SSL connection"));
        break;
      }
      default:
        upgradePromise.fail(new Exception("Invalid SSL connection message"));
        break;
    }
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    if (cause instanceof DecoderException) {
      DecoderException err = (DecoderException) cause;
      cause = err.getCause();
    }
    upgradePromise.tryFail(cause);
  }

  @Override
  public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    super.channelInactive(ctx);
    // Work around for https://github.com/eclipse-vertx/vert.x/issues/2748
    upgradePromise.tryFail(new VertxException("SSL handshake failed", true));
  }
}
