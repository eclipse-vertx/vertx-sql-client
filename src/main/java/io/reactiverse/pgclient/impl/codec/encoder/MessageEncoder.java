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
package io.reactiverse.pgclient.impl.codec.encoder;

import io.netty.buffer.ByteBuf;
import io.vertx.core.impl.NetSocketInternal;

import java.util.ArrayDeque;

public class MessageEncoder {

  private final NetSocketInternal socket;
  private boolean cork = false;
  private ArrayDeque<OutboundMessage> outbound = new ArrayDeque<>();

  public MessageEncoder(NetSocketInternal socket) {
    this.socket = socket;
  }

  public void begin() {
    cork = true;
  }

  public void end() {
    cork = false;
    if (outbound.size() > 0) {
      ByteBuf out = null;
      try {
        out = socket.channelHandlerContext().alloc().ioBuffer();
        OutboundMessage msg;
        while ((msg = outbound.poll()) != null) {
          msg.encode(out);
        }
        socket.writeMessage(out);
        out = null;
      } finally {
        if (out != null) {
          out.release();
        }
      }
    }
  }

  public void writeMessage(OutboundMessage cmd) {
    if (cork) {
      outbound.add(cmd);
    } else {
      ByteBuf out = null;
      try {
        out = socket.channelHandlerContext().alloc().ioBuffer();
        cmd.encode(out);
        socket.writeMessage(out);
        out = null;
      } finally {
        if (out != null) {
          out.release();
        }
      }
    }
  }
}
