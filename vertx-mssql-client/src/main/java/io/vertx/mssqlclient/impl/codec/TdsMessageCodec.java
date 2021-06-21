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
import io.netty.channel.CombinedChannelDuplexHandler;
import io.vertx.core.impl.NoStackTraceThrowable;
import io.vertx.sqlclient.impl.command.CommandBase;
import io.vertx.sqlclient.impl.command.CommandResponse;

import java.util.ArrayDeque;
import java.util.Iterator;

public class TdsMessageCodec extends CombinedChannelDuplexHandler<TdsMessageDecoder, TdsMessageEncoder> {

  private final ArrayDeque<MSSQLCommandCodec<?, ?>> inflight = new ArrayDeque<>();
  private final TdsMessageEncoder encoder;

  private ChannelHandlerContext chctx;
  private ByteBufAllocator alloc;
  private long transactionDescriptor;

  public TdsMessageCodec(int packetSize) {
    TdsMessageDecoder decoder = new TdsMessageDecoder(this);
    encoder = new TdsMessageEncoder(this, packetSize);
    init(decoder, encoder);
  }

  @Override
  public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
    super.handlerAdded(ctx);
    chctx = ctx;
    alloc = chctx.alloc();
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    fail(ctx, cause);
    super.exceptionCaught(ctx, cause);
  }

  private void fail(ChannelHandlerContext ctx, Throwable cause) {
    for (Iterator<MSSQLCommandCodec<?, ?>> it = inflight.iterator(); it.hasNext(); ) {
      MSSQLCommandCodec<?, ?> codec = it.next();
      it.remove();
      CommandResponse<Object> failure = CommandResponse.failure(cause);
      failure.cmd = (CommandBase) codec.cmd;
      ctx.fireChannelRead(failure);
    }
  }

  @Override
  public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    fail(ctx, new NoStackTraceThrowable("Fail to read any response from the server, the underlying connection might get lost unexpectedly."));
    super.channelInactive(ctx);
  }

  TdsMessageEncoder encoder() {
    return encoder;
  }

  ChannelHandlerContext chctx() {
    return chctx;
  }

  ByteBufAllocator alloc() {
    return alloc;
  }

  long transactionDescriptor() {
    return transactionDescriptor;
  }

  void setTransactionDescriptor(long transactionDescriptor) {
    this.transactionDescriptor = transactionDescriptor;
  }

  MSSQLCommandCodec<?, ?> peek() {
    return inflight.peek();
  }

  MSSQLCommandCodec<?, ?> poll() {
    return inflight.poll();
  }

  void add(MSSQLCommandCodec<?, ?> codec) {
    inflight.add(codec);
  }
}
