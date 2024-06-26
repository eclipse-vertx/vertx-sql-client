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
import io.vertx.sqlclient.ClosedConnectionException;
import io.vertx.sqlclient.internal.command.CommandBase;
import io.vertx.sqlclient.internal.command.CommandResponse;

import java.util.ArrayDeque;
import java.util.Iterator;

public class PgCodec extends CombinedChannelDuplexHandler<PgDecoder, PgEncoder> {

  private final ArrayDeque<PgCommandCodec<?, ?>> inflight = new ArrayDeque<>();
  private final PgDecoder decoder;
  private final PgEncoder encoder;
  private ChannelHandlerContext chctx;
  private Throwable failure;

  public PgCodec(boolean useLayer7Proxy) {
    decoder = new PgDecoder(this);
    encoder = new PgEncoder(useLayer7Proxy, this);
    init(decoder, encoder);
  }

  boolean add(PgCommandCodec<?, ?> codec) {
    if (failure == null) {
      codec.decoder = decoder;
      inflight.add(codec);
      return true;
    } else {
      fail(codec, failure);
      return false;
    }
  }

  PgCommandCodec<?, ?> peek() {
    return inflight.peek();
  }

  PgCommandCodec<?, ?> poll() {
    return inflight.poll();
  }

  @Override
  public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
    chctx = ctx;
    super.handlerAdded(ctx);
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    fail(cause);
    super.exceptionCaught(ctx, cause);
  }

  private void fail(Throwable cause) {
    if (failure == null) {
      failure = cause;
      for  (Iterator<PgCommandCodec<?, ?>> it = inflight.iterator(); it.hasNext();) {
        PgCommandCodec<?, ?> cmdCodec = it.next();
        it.remove();
        fail(cmdCodec, cause);
      }
    }
  }

  private void fail(PgCommandCodec<?, ?> codec, Throwable cause) {
    CommandResponse<Object> failure = CommandResponse.failure(cause);
    failure.cmd = (CommandBase) codec.cmd;
    chctx.fireChannelRead(failure);
  }

  @Override
  public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    fail(ClosedConnectionException.INSTANCE);
    super.channelInactive(ctx);
  }
}
