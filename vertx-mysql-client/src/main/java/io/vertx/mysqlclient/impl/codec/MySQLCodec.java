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
import io.vertx.mysqlclient.impl.MySQLSocketConnection;
import io.vertx.sqlclient.ClosedConnectionException;

import java.util.ArrayDeque;
import java.util.Iterator;

public class MySQLCodec extends CombinedChannelDuplexHandler<MySQLDecoder, MySQLEncoder> {

  private final ArrayDeque<MySQLCommand<?, ?>> inflight;

  public MySQLCodec(MySQLSocketConnection mySQLSocketConnection) {
    inflight = new ArrayDeque<>();
    MySQLEncoder encoder = new MySQLEncoder(this, mySQLSocketConnection);
    MySQLDecoder decoder = new MySQLDecoder(this);
    init(decoder, encoder);
  }

  public void add(MySQLCommand<?, ?> codec) {
    inflight.add(codec);
  }

  public MySQLCommand<?, ?> poll() {
    return inflight.poll();
  }

  public MySQLCommand<?, ?> peek() {
    return inflight.peek();
  }

  /**
   * check the pending command queue and complete handling the command directly if the command request will not receive a server response
   */
  void checkFireAndForgetCommands() {
    // check if there is any completed command
    MySQLCommand<?, ?> commandMsg;
    while ((commandMsg = inflight.peek()) != null && commandMsg.expectNoResponsePacket()) {
      commandMsg.decodePayload(null, 0);
    }
  }
}
