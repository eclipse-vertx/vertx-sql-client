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

import io.netty.buffer.ByteBuf;
import io.vertx.core.Handler;
import io.vertx.db2client.impl.command.InitialHandshakeCommand;
import io.vertx.db2client.impl.command.PingCommand;
import io.vertx.sqlclient.codec.CommandMessage;
import io.vertx.sqlclient.spi.protocol.CloseConnectionCommand;
import io.vertx.sqlclient.spi.protocol.CloseCursorCommand;
import io.vertx.sqlclient.spi.protocol.CloseStatementCommand;
import io.vertx.sqlclient.spi.protocol.CommandBase;
import io.vertx.sqlclient.codec.CommandResponse;
import io.vertx.sqlclient.spi.protocol.PrepareStatementCommand;
import io.vertx.sqlclient.spi.protocol.SimpleQueryCommand;

public abstract class DB2CommandMessage<R, C extends CommandBase<R>> extends CommandMessage<R, C> {

  Handler<? super CommandResponse<R>> completionHandler;
  public Throwable failure;
  public R result;
  DB2Encoder encoder;

  DB2CommandMessage(C cmd) {
    super(cmd);
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  public static DB2CommandMessage<?, ?> wrap(CommandBase<?> cmd) {
    DB2CommandMessage<?, ?> codec = null;
    if (cmd instanceof InitialHandshakeCommand) {
      codec = new InitialHandshakeDB2CommandMessage((InitialHandshakeCommand) cmd);
    } else if (cmd instanceof SimpleQueryCommand) {
      codec = new SimpleQueryDB2CommandMessage((SimpleQueryCommand) cmd);
    } else if (cmd instanceof CloseConnectionCommand) {
      codec = new CloseConnectionDB2CommandMessage((CloseConnectionCommand) cmd);
    } else if (cmd instanceof PrepareStatementCommand) {
      codec = new PrepareStatementDB2CommandMessage((PrepareStatementCommand) cmd);
    } else if (cmd instanceof CloseStatementCommand) {
      codec = new CloseStatementDB2CommandMessage((CloseStatementCommand) cmd);
    } else if (cmd instanceof CloseCursorCommand) {
      codec = new CloseCursorDB2CommandMessage((CloseCursorCommand) cmd);
    } else if (cmd instanceof PingCommand) {
      codec = new PingDB2CommandMessage((PingCommand) cmd);
//        } else if (cmd instanceof InitDbCommand) {
//            codec = new InitDbCommandCodec((InitDbCommand) cmd);
      // } else if (cmd instanceof StatisticsCommand) {
      // codec = new StatisticsCommandCodec((StatisticsCommand) cmd);
      // } else if (cmd instanceof SetOptionCommand) {
      // codec = new SetOptionCommandCodec((SetOptionCommand) cmd);
      // } else if (cmd instanceof ResetConnectionCommand) {
      // codec = new ResetConnectionCommandCodec((ResetConnectionCommand) cmd);
      // } else if (cmd instanceof DebugCommand) {
      // codec = new DebugCommandCodec((DebugCommand) cmd);
      // } else if (cmd instanceof ChangeUserCommand) {
      // codec = new ChangeUserCommandCodec((ChangeUserCommand) cmd);
    } else {
      UnsupportedOperationException uoe = new UnsupportedOperationException("Unsupported command type: " + cmd);
      DB2Encoder.LOG.error(uoe);
      throw uoe;
    }
    if (DB2Encoder.LOG.isDebugEnabled())
      DB2Encoder.LOG.debug(">>> ENCODE " + codec);
    return codec;
  }

  abstract void decodePayload(ByteBuf payload, int payloadLength);

  void encode(DB2Encoder encoder) {
    this.encoder = encoder;
  }

  ByteBuf allocateBuffer() {
    return encoder.chctx.alloc().ioBuffer();
  }

  ByteBuf allocateBuffer(int capacity) {
    return encoder.chctx.alloc().ioBuffer(capacity);
  }

  void sendPacket(ByteBuf packet, int payloadLength) {
    if (payloadLength >= DB2Codec.PACKET_PAYLOAD_LENGTH_LIMIT) {
      /*
       * The original packet exceeds the limit of packet length, split the packet
       * here. if payload length is exactly 16MBytes-1byte(0xFFFFFF), an empty packet
       * is needed to indicate the termination.
       */
      throw new UnsupportedOperationException("Sending split packets not implemented");
//            sendSplitPacket(packet);
    } else {
      sendNonSplitPacket(packet);
    }
  }

  void sendNonSplitPacket(ByteBuf packet) {
    encoder.chctx.writeAndFlush(packet, encoder.chctx.voidPromise());
  }
}
