/*
 *
 *  Copyright (c) 2021 Vladimir Vishnevskii
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Eclipse Public License 2.0 which is available at
 *  http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 *  which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 *  SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 *
 */

package io.vertx.clickhouseclient.binary.impl.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.ArrayDeque;
import java.util.List;

public class ClickhouseBinaryDecoder extends ByteToMessageDecoder {
  private final ArrayDeque<ClickhouseBinaryCommandCodec<?, ?>> inflight;

  public ClickhouseBinaryDecoder(ArrayDeque<ClickhouseBinaryCommandCodec<?, ?>> inflight) {
    this.inflight = inflight;
  }

  @Override
  protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
    ClickhouseBinaryCommandCodec<?, ?> codec = inflight.peek();
    codec.decode(ctx, in);
  }
}