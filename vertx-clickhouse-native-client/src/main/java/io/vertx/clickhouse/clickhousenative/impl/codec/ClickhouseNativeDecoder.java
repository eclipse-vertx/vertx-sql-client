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

package io.vertx.clickhouse.clickhousenative.impl.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.vertx.clickhouse.clickhousenative.impl.ClickhouseNativeSocketConnection;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;

import java.util.ArrayDeque;
import java.util.List;

public class ClickhouseNativeDecoder extends ByteToMessageDecoder {
  private static final Logger LOG = LoggerFactory.getLogger(ClickhouseNativeDecoder.class);

  private final ArrayDeque<ClickhouseNativeCommandCodec<?, ?>> inflight;
  private final ClickhouseNativeSocketConnection conn;
  public ClickhouseNativeDecoder(ArrayDeque<ClickhouseNativeCommandCodec<?, ?>> inflight, ClickhouseNativeSocketConnection conn) {
    this.inflight = inflight;
    this.conn = conn;
  }

  @Override
  protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
    ClickhouseNativeCommandCodec<?, ?> codec = inflight.peek();
    codec.decode(ctx, in);
  }
}
