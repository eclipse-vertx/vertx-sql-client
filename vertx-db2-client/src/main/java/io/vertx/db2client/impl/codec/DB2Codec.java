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

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.CombinedChannelDuplexHandler;
import io.vertx.db2client.impl.DB2SocketConnection;
import io.vertx.sqlclient.ClosedChannelException;

import java.util.ArrayDeque;

public class DB2Codec extends CombinedChannelDuplexHandler<DB2Decoder, DB2Encoder> {

  // TODO @AGG check what packet length limit actually is for DB2
  static final int PACKET_PAYLOAD_LENGTH_LIMIT = 0xFFFFFF;

  private final ArrayDeque<CommandCodec<?, ?>> inflight = new ArrayDeque<>();

  public DB2Codec(DB2SocketConnection db2SocketConnection) {
    DB2Encoder encoder = new DB2Encoder(inflight, db2SocketConnection);
    DB2Decoder decoder = new DB2Decoder(inflight);
    init(decoder, encoder);
  }

  @Override
  public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    clearInflightCommands(ClosedChannelException.INSTANCE);
    super.channelInactive(ctx);
  }

  private void clearInflightCommands(Throwable failure) {
    for (CommandCodec<?, ?> commandCodec : inflight) {
      commandCodec.cmd.fail(failure);
    }
  }
}
