/*
 * Copyright (C) 2019,2020 IBM Corporation
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
 */
package io.vertx.db2client.impl.codec;

import java.util.ArrayDeque;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.vertx.core.Completable;
import io.vertx.core.internal.logging.Logger;
import io.vertx.core.internal.logging.LoggerFactory;
import io.vertx.db2client.impl.DB2SocketConnection;
import io.vertx.sqlclient.codec.CommandResponse;

class DB2Encoder extends ChannelOutboundHandlerAdapter {

  public static final Logger LOG = LoggerFactory.getLogger(DB2Encoder.class);

  private final ArrayDeque<DB2CommandMessage<?, ?>> inflight;
  ChannelHandlerContext chctx;

  final DB2SocketConnection socketConnection;

  DB2Encoder(ArrayDeque<DB2CommandMessage<?, ?>> inflight, DB2SocketConnection db2SocketConnection) {
    this.inflight = inflight;
    this.socketConnection = db2SocketConnection;
  }

  @Override
  public void handlerAdded(ChannelHandlerContext ctx) {
    chctx = ctx;
  }

  @Override
  public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
    if (msg instanceof DB2CommandMessage<?, ?>) {
      DB2CommandMessage<?, ?> cmd = (DB2CommandMessage<?, ?>) msg;
      write(cmd);
    } else {
      super.write(ctx, msg, promise);
    }
  }

  void fireCommandFailure(DB2CommandMessage msg, Throwable err) {
    DB2CommandMessage<?, ?> c = inflight.poll();
    if (c == msg) {
      chctx.fireChannelRead(CommandResponse.failure(err));
    } else {
      throw new IllegalStateException();
    }
  }

  <R> void fireCommandSuccess(DB2CommandMessage msg, R result) {
    DB2CommandMessage<?, ?> c = inflight.poll();
    if (c == msg) {
      chctx.fireChannelRead(CommandResponse.success(result));
    } else {
      throw new IllegalStateException();
    }
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  void write(DB2CommandMessage<?, ?> msg) {
    try {
      inflight.add(msg);
      msg.encode(this);
    } catch (Throwable e) {
      inflight.pollLast();
      LOG.error("FATAL: Unable to encode command: " + msg, e);
      chctx.fireChannelRead(CommandResponse.failure(e));
    }
  }
}
