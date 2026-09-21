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
import io.vertx.sqlclient.codec.SocketConnectionBase;

import java.util.ArrayDeque;

public class PgCodec extends CombinedChannelDuplexHandler<PgDecoder, PgEncoder> {

  private SocketConnectionBase connection;
  private boolean commandPipelineSuspended;

  private final ArrayDeque<PgCommandMessage<?, ?>> inflight;
  private final PgDecoder decoder;
  private final PgEncoder encoder;

  public PgCodec(boolean useLayer7Proxy) {
    inflight =  new ArrayDeque<>();
    decoder = new PgDecoder(this);
    encoder = new PgEncoder(useLayer7Proxy, this);
    init(decoder, encoder);
  }

  public void setConnection(SocketConnectionBase connection) {
    this.connection = connection;
  }

  @Override
  public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    clearConnectionReference();
    super.channelInactive(ctx);
  }

  @Override
  public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
    clearConnectionReference();
    super.handlerRemoved(ctx);
  }

  private void clearConnectionReference() {
    connection = null;
    commandPipelineSuspended = false;
  }

  void suspendCommandPipeline() {
    if (!commandPipelineSuspended) {
      commandPipelineSuspended = true;
      if (connection != null) {
        connection.suspendPipeline();
      }
    }
  }

  void resumeCommandPipeline() {
    if (commandPipelineSuspended) {
      commandPipelineSuspended = false;
      if (connection != null) {
        connection.resumePipeline();
      }
    }
  }

  void add(PgCommandMessage<?, ?> codec) {
    codec.decoder = decoder;
    inflight.add(codec);
  }

  PgCommandMessage<?, ?> peek() {
    return inflight.peek();
  }

  PgCommandMessage<?, ?> poll() {
    return inflight.poll();
  }
}
