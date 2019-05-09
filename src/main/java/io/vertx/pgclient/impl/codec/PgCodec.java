/*
 * Copyright (C) 2018 Julien Viet
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
package io.vertx.pgclient.impl.codec;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.CombinedChannelDuplexHandler;
import io.vertx.sqlclient.impl.command.CommandBase;
import io.vertx.sqlclient.impl.command.CommandResponse;
import io.vertx.core.VertxException;

import java.util.ArrayDeque;
import java.util.Iterator;

public class PgCodec extends CombinedChannelDuplexHandler<PgDecoder, PgEncoder> {

  private final ArrayDeque<PgCommandCodec<?, ?>> inflight = new ArrayDeque<>();

  public PgCodec() {
    PgDecoder decoder = new PgDecoder(inflight);
    PgEncoder encoder = new PgEncoder(decoder, inflight);
    init(decoder, encoder);
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    fail(ctx, cause);
    super.exceptionCaught(ctx, cause);
  }

  private void fail(ChannelHandlerContext ctx, Throwable cause) {
    for  (Iterator<PgCommandCodec<?, ?>> it = inflight.iterator(); it.hasNext();) {
      PgCommandCodec<?, ?> codec = it.next();
      it.remove();
      CommandResponse<Object> failure = CommandResponse.failure(cause);
      failure.cmd = (CommandBase) codec.cmd;
      ctx.fireChannelRead(failure);
    }
  }

  @Override
  public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    fail(ctx, new VertxException("closed"));
    super.channelInactive(ctx);
  }
}
