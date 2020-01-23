package io.vertx.mysqlclient.impl;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.DecoderException;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.VertxException;
import io.vertx.sqlclient.impl.SocketConnectionBase;

public class InitiateSslHandler extends ChannelInboundHandlerAdapter {
  private final SocketConnectionBase conn;
  private final Promise<Void> upgradePromise;

  public InitiateSslHandler(SocketConnectionBase conn, Promise<Void> upgradePromise) {
    this.conn = conn;
    this.upgradePromise = upgradePromise;
  }

  @Override
  public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
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
  }

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    ctx.fireExceptionCaught(new IllegalStateException("Read illegal data while performing TLS handshake"));
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
