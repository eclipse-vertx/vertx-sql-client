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

package io.vertx.mssqlclient.impl;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.ssl.SslHandler;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.impl.EventLoopContext;
import io.vertx.core.impl.future.PromiseInternal;
import io.vertx.core.net.impl.NetSocketInternal;
import io.vertx.core.net.impl.SSLHelper;
import io.vertx.core.net.impl.SslHandshakeCompletionHandler;
import io.vertx.mssqlclient.impl.codec.TdsMessageCodec;
import io.vertx.mssqlclient.impl.codec.TdsPacketDecoder;
import io.vertx.mssqlclient.impl.command.PreLoginCommand;
import io.vertx.mssqlclient.impl.command.PreLoginResponse;
import io.vertx.sqlclient.impl.Connection;
import io.vertx.sqlclient.impl.QueryResultHandler;
import io.vertx.sqlclient.impl.SocketConnectionBase;
import io.vertx.sqlclient.impl.command.*;
import io.vertx.sqlclient.spi.DatabaseMetadata;

import java.util.Map;
import java.util.function.Predicate;

import static io.vertx.mssqlclient.MSSQLConnectOptions.MAX_PACKET_SIZE;
import static io.vertx.mssqlclient.impl.codec.MessageStatus.END_OF_MESSAGE;
import static io.vertx.mssqlclient.impl.codec.MessageStatus.NORMAL;
import static io.vertx.mssqlclient.impl.codec.MessageType.PRE_LOGIN;
import static io.vertx.mssqlclient.impl.codec.TdsPacket.PACKET_HEADER_SIZE;
import static io.vertx.sqlclient.impl.command.TxCommand.Kind.BEGIN;

class MSSQLSocketConnection extends SocketConnectionBase {

  private final int packetSize;

  private MSSQLDatabaseMetadata databaseMetadata;

  MSSQLSocketConnection(NetSocketInternal socket,
                        int packetSize,
                        boolean cachePreparedStatements,
                        int preparedStatementCacheSize,
                        Predicate<String> preparedStatementCacheSqlFilter,
                        int pipeliningLimit,
                        EventLoopContext context) {
    super(socket, cachePreparedStatements, preparedStatementCacheSize, preparedStatementCacheSqlFilter, pipeliningLimit, context);
    this.packetSize = packetSize;
  }

  Future<Byte> sendPreLoginMessage(boolean ssl) {
    PreLoginCommand cmd = new PreLoginCommand(ssl);
    return schedule(context, cmd).onSuccess(resp -> setDatabaseMetadata(resp.metadata())).map(PreLoginResponse::encryptionLevel);
  }

  public Future<Void> enableSsl(SSLHelper helper) {
    ChannelPipeline pipeline = socket.channelHandlerContext().pipeline();
    PromiseInternal<Void> promise = context.promise();

    ChannelPromise p = pipeline.newPromise();
    pipeline.addFirst("handshaker", new SslHandshakeCompletionHandler(p));
    p.addListener(future -> {
      if (future.isSuccess()) {
        pipeline.remove("prelogin-handshake-handler");
        promise.complete();
      } else {
        promise.fail(future.cause());
      }
    });

    SslHandler sslHandler = new SslHandler(helper.createEngine(context.owner(), socket.remoteAddress(), null, false));
    sslHandler.setHandshakeTimeout(helper.getSslHandshakeTimeout(), helper.getSslHandshakeTimeoutUnit());

    pipeline.addFirst("prelogin-handshake-handler", new MyHandler(sslHandler));
    pipeline.addAfter("prelogin-handshake-handler", "ssl", sslHandler);

    return promise.future();
  }

  public void disableSsl() {
    ChannelHandlerContext ctx = socket.channelHandlerContext();
    ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(future -> {
      ChannelPipeline pipeline = ctx.pipeline();
      pipeline.removeFirst();
    });
  }

  Future<Connection> sendLoginMessage(String username, String password, String database, Map<String, String> properties) {
    InitCommand cmd = new InitCommand(this, username, password, database, properties);
    return schedule(context, cmd);
  }

  @Override
  public void init() {
    ChannelPipeline pipeline = socket.channelHandlerContext().pipeline();
    pipeline.addBefore("handler", "messageCodec", new TdsMessageCodec(packetSize));
    pipeline.addBefore("messageCodec", "packetDecoder", new TdsPacketDecoder());
    super.init();
  }

  @Override
  protected <R> void doSchedule(CommandBase<R> cmd, Handler<AsyncResult<R>> handler) {
    if (cmd instanceof TxCommand) {
      TxCommand<R> tx = (TxCommand<R>) cmd;
      String sql = tx.kind == BEGIN ? "BEGIN TRANSACTION":tx.kind.sql;
      SimpleQueryCommand<Void> cmd2 = new SimpleQueryCommand<>(
        sql,
        false,
        false,
        QueryCommandBase.NULL_COLLECTOR,
        QueryResultHandler.NOOP_HANDLER);
      super.doSchedule(cmd2, ar -> handler.handle(ar.map(tx.result)));
    } else {
      super.doSchedule(cmd, handler);
    }
  }

  @Override
  public DatabaseMetadata getDatabaseMetaData() {
    return databaseMetadata;
  }

  private void setDatabaseMetadata(MSSQLDatabaseMetadata metadata) {
    this.databaseMetadata = metadata;
  }

  private static class MyHandler extends CombinedChannelDuplexHandler<LengthFieldBasedFrameDecoder, ChannelOutboundHandlerAdapter> {

    private final SslHandler sslHandler;

    public MyHandler(SslHandler sslHandler) {
      this.sslHandler = sslHandler;
      init(new LengthFieldBasedFrameDecoder(MAX_PACKET_SIZE, 2, 2, -4, 0) {
        @Override
        protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
          ByteBuf byteBuf = (ByteBuf) super.decode(ctx, in);
          if (byteBuf == null) {
            return null;
          }

          short type = in.getUnsignedByte(0);
          short status = in.getUnsignedByte(1);
          int length = in.getUnsignedShort(2);

          return byteBuf.slice(PACKET_HEADER_SIZE, length - PACKET_HEADER_SIZE);
        }
      }, new ChannelOutboundHandlerAdapter() {

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
        public void read(ChannelHandlerContext ctx) throws Exception {
          super.read(ctx);
        }

        @Override
        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
          if (msg instanceof ByteBuf) {
            ByteBuf byteBuf = (ByteBuf) msg;
            accumulate(byteBuf);
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
      });
    }
  }
}
