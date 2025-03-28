/*
 * Copyright (C) 2017 Julien Viet
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
package io.vertx.mysqlclient.impl.codec;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.CombinedChannelDuplexHandler;
import io.vertx.core.Completable;
import io.vertx.mysqlclient.impl.MySQLSocketConnection;
import io.vertx.sqlclient.ClosedConnectionException;
import io.vertx.sqlclient.internal.command.CommandBase;
import io.vertx.sqlclient.internal.command.CommandResponse;

import java.util.ArrayDeque;
import java.util.Iterator;

public class MySQLCodec extends CombinedChannelDuplexHandler<MySQLDecoder, MySQLEncoder> {

  private final ArrayDeque<CommandCodec<?, ?>> inflight;
  private ChannelHandlerContext chctx;
  private Throwable failure;

  public MySQLCodec(MySQLSocketConnection mySQLSocketConnection) {
    inflight = new ArrayDeque<>();
    MySQLEncoder encoder = new MySQLEncoder(this, mySQLSocketConnection);
    MySQLDecoder decoder = new MySQLDecoder(this);
    init(decoder, encoder);
  }

  @Override
  public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
    chctx = ctx;
    super.handlerAdded(ctx);
  }

  @Override
  public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    clearInflightCommands(ClosedConnectionException.INSTANCE);
    super.channelInactive(ctx);
  }

  public boolean add(CommandCodec<?, ?> codec) {
    if (failure == null) {
      inflight.add(codec);
      return true;
    } else {
      fail(codec, failure);
      return false;
    }
  }

  public CommandCodec<?, ?> poll() {
    return inflight.poll();
  }

  public CommandCodec<?, ?> peek() {
    return inflight.peek();
  }

  private void clearInflightCommands(Throwable cause) {
    for (Iterator<CommandCodec<?, ?>> it = inflight.iterator(); it.hasNext(); ) {
      CommandCodec<?, ?> codec = it.next();
      it.remove();
      fail(codec, cause);
    }
  }

  private void fail(CommandCodec<?, ?> codec, Throwable cause) {
    if (failure == null) {
      failure = cause;
      Completable<?> handler = codec.cmd.handler;
      if (handler != null) {
        handler.complete(null, cause);
      }
    }
  }

  /**
   * check the pending command queue and complete handling the command directly if the command request will not receive a server response
   */
  void checkFireAndForgetCommands() {
    // check if there is any completed command
    CommandCodec<?, ?> commandCodec;
    while ((commandCodec = inflight.peek()) != null && commandCodec.expectNoResponsePacket()) {
      commandCodec.decodePayload(null, 0);
    }
  }
}
