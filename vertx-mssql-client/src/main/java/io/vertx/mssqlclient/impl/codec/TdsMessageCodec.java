/*
 * Copyright (c) 2011-2024 Contributors to the Eclipse Foundation
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
import io.vertx.core.Completable;
import io.vertx.sqlclient.ClosedConnectionException;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class TdsMessageCodec extends CombinedChannelDuplexHandler<TdsMessageDecoder, TdsMessageEncoder> {

  private final ArrayDeque<MSSQLCommandCodec<?, ?>> inflight = new ArrayDeque<>();
  private final TdsMessageEncoder encoder;
  private final TdsMessageDecoder decoder;

  private ChannelHandlerContext chctx;
  private ByteBufAllocator alloc;
  private long transactionDescriptor;
  private Map<String, CursorData> cursorDataMap;
  private Throwable failure;

  public TdsMessageCodec(int desiredPacketSize) {
    decoder = new TdsMessageDecoder(this);
    encoder = new TdsMessageEncoder(this, desiredPacketSize);
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
    fail(cause);
    super.exceptionCaught(ctx, cause);
  }

  private void fail(Throwable cause) {
    if (failure == null) {
      failure = cause;
      for (Iterator<MSSQLCommandCodec<?, ?>> it = inflight.iterator(); it.hasNext(); ) {
        MSSQLCommandCodec<?, ?> codec = it.next();
        it.remove();
        fail(codec, cause);
      }
    }
  }

  private void fail(MSSQLCommandCodec<?, ?> codec, Throwable cause) {
    codec.cmd.fail(cause);
  }

  @Override
  public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    fail(ClosedConnectionException.INSTANCE);
    super.channelInactive(ctx);
  }

  TdsMessageEncoder encoder() {
    return encoder;
  }

  TdsMessageDecoder decoder() {
    return decoder;
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

  boolean add(MSSQLCommandCodec<?, ?> codec) {
    if (failure == null) {
      inflight.add(codec);
      return true;
    } else {
      fail(codec, failure);
      return false;
    }
  }

  CursorData getOrCreateCursorData(String cursorId) {
    if (cursorDataMap == null) {
      cursorDataMap = new HashMap<>();
    }
    CursorData cd = cursorDataMap.get(cursorId);
    if (cd == null) {
      cd = new CursorData();
      cursorDataMap.put(cursorId, cd);
    }
    return cd;
  }

  CursorData removeCursorData(String cursorId) {
    if (cursorDataMap == null) {
      return null;
    }
    CursorData cd = cursorDataMap.remove(cursorId);
    if (cursorDataMap.isEmpty()) {
      cursorDataMap = null;
    }
    return cd;
  }
}
