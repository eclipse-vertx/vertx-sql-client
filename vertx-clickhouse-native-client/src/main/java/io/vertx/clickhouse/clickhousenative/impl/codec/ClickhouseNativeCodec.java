/*
 *
 *  * Copyright (c) 2021 Vladimir Vishnevsky
 *  *
 *  * This program and the accompanying materials are made available under the
 *  * terms of the Eclipse Public License 2.0 which is available at
 *  * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 *  * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *  *
 *  * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 *
 */

package io.vertx.clickhouse.clickhousenative.impl.codec;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.CombinedChannelDuplexHandler;
import io.vertx.clickhouse.clickhousenative.impl.ClickhouseNativeSocketConnection;
import io.vertx.core.impl.NoStackTraceThrowable;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.sqlclient.impl.command.CommandBase;
import io.vertx.sqlclient.impl.command.CommandResponse;

import java.util.ArrayDeque;
import java.util.Iterator;

public class ClickhouseNativeCodec extends CombinedChannelDuplexHandler<ClickhouseNativeDecoder, ClickhouseNativeEncoder> {
  private static final Logger LOG = LoggerFactory.getLogger(ClickhouseNativeCodec.class);

  private ArrayDeque<ClickhouseNativeCommandCodec<?, ?>> inflight;

  public ClickhouseNativeCodec(ClickhouseNativeSocketConnection conn) {
    inflight = new ArrayDeque<>();
    ClickhouseNativeEncoder encoder = new ClickhouseNativeEncoder(inflight, conn);
    ClickhouseNativeDecoder decoder = new ClickhouseNativeDecoder(inflight, conn);
    init(decoder, encoder);
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    fail(ctx, cause);
    super.exceptionCaught(ctx, cause);
  }

  private void fail(ChannelHandlerContext ctx, Throwable cause) {
    for  (Iterator<ClickhouseNativeCommandCodec<?, ?>> it = inflight.iterator(); it.hasNext();) {
      ClickhouseNativeCommandCodec<?, ?> codec = it.next();
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
}
