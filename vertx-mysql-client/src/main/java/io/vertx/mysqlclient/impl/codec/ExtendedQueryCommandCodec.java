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

import io.netty.buffer.ByteBuf;
import io.vertx.mysqlclient.impl.datatype.DataTypeCodec;
import io.vertx.mysqlclient.impl.protocol.ColumnDefinition;
import io.vertx.mysqlclient.impl.protocol.CommandType;
import io.vertx.sqlclient.Tuple;
import io.vertx.sqlclient.impl.command.ExtendedQueryCommand;

import static io.vertx.mysqlclient.impl.protocol.Packets.ERROR_PACKET_HEADER;
import static io.vertx.mysqlclient.impl.protocol.Packets.EnumCursorType.CURSOR_TYPE_NO_CURSOR;
import static io.vertx.mysqlclient.impl.protocol.Packets.EnumCursorType.CURSOR_TYPE_READ_ONLY;

class ExtendedQueryCommandCodec<R> extends ExtendedQueryCommandBaseCodec<R, ExtendedQueryCommand<R>> {
  ExtendedQueryCommandCodec(ExtendedQueryCommand<R> cmd) {
    super(cmd);
    if (cmd.fetch() > 0 && statement.isCursorOpen) {
      // restore the state we need for decoding fetch response
      columnDefinitions = statement.rowDesc.columnDefinitions();
    }
  }

  @Override
  void encode(MySQLEncoder encoder) {
    super.encode(encoder);

    if (statement.isCursorOpen) {
      decoder = new RowResultDecoder<>(cmd.collector(), statement.rowDesc);
      sendStatementFetchCommand(statement.statementId, cmd.fetch());
    } else {
      if (cmd.fetch() > 0) {
        sendStatementExecuteCommand(statement, true, cmd.params(), CURSOR_TYPE_READ_ONLY);
      } else {
        // CURSOR_TYPE_NO_CURSOR
        sendStatementExecuteCommand(statement, statement.paramDesc.sendTypesToServer(), cmd.params(), CURSOR_TYPE_NO_CURSOR);
      }
    }
  }

  @Override
  void decodePayload(ByteBuf payload, int payloadLength) {
    if (statement.isCursorOpen) {
      int first = payload.getUnsignedByte(payload.readerIndex());
      if (first == ERROR_PACKET_HEADER) {
        handleErrorPacketPayload(payload);
      } else {
        // decoding COM_STMT_FETCH response
        handleRows(payload, payloadLength, super::handleSingleRow);
      }
    } else {
      // decoding COM_STMT_EXECUTE response
      if (cmd.fetch() > 0) {
        switch (commandHandlerState) {
          case INIT:
            int first = payload.getUnsignedByte(payload.readerIndex());
            if (first == ERROR_PACKET_HEADER) {
              handleErrorPacketPayload(payload);
            } else {
              handleResultsetColumnCountPacketBody(payload);
            }
            break;
          case HANDLING_COLUMN_DEFINITION:
            handleResultsetColumnDefinitions(payload);
            break;
          case COLUMN_DEFINITIONS_DECODING_COMPLETED:
            // accept an EOF_Packet when DEPRECATE_EOF is not enabled
            skipEofPacketIfNeeded(payload);
            // do not need a break clause here
          case HANDLING_ROW_DATA_OR_END_PACKET:
            handleResultsetColumnDefinitionsDecodingCompleted();
            // need to reset packet number so that we can send a fetch request
            encoder.sequenceId = 0;
            // send fetch after cursor opened
            decoder = new RowResultDecoder<>(cmd.collector(), statement.rowDesc);

            statement.isCursorOpen = true;

            sendStatementFetchCommand(statement.statementId, cmd.fetch());
            break;
          default:
            throw new IllegalStateException("Unexpected state for decoding COM_STMT_EXECUTE response with cursor opening");
        }
      } else {
        super.decodePayload(payload, payloadLength);
      }
    }
  }

  private void sendStatementExecuteCommand(MySQLPreparedStatement statement, boolean sendTypesToServer, Tuple params, byte cursorType) {
    ByteBuf packet = allocateBuffer();
    // encode packet header
    int packetStartIdx = packet.writerIndex();
    packet.writeMediumLE(0); // will set payload length later by calculation
    packet.writeByte(encoder.sequenceId);

    // encode packet payload
    packet.writeByte(CommandType.COM_STMT_EXECUTE);
    packet.writeIntLE((int) statement.statementId);
    packet.writeByte(cursorType);
    // iteration count, always 1
    packet.writeIntLE(1);

    ColumnDefinition[] paramsColumnDefinitions = statement.paramDesc.paramDefinitions();
    int numOfParams = paramsColumnDefinitions.length;
    int bitmapLength = (numOfParams + 7) / 8;
    byte[] nullBitmap = new byte[bitmapLength];

    int pos = packet.writerIndex();

    if (numOfParams > 0) {
      // write a dummy bitmap first
      packet.writeBytes(nullBitmap);
      packet.writeBoolean(sendTypesToServer);

      if (sendTypesToServer) {
        for (ColumnDefinition paramsColumnDefinition : paramsColumnDefinitions) {
          packet.writeByte(paramsColumnDefinition.getType().id);
          packet.writeByte(0); // parameter flag: signed
        }
      }

      for (int i = 0; i < numOfParams; i++) {
        Object value = params.getValue(i);
        if (value != null) {
          DataTypeCodec.encodeBinary(paramsColumnDefinitions[i].getType(), value, encoder.encodingCharset, packet);
        } else {
          nullBitmap[i / 8] |= (1 << (i & 7));
        }
      }

      // padding null-bitmap content
      packet.setBytes(pos, nullBitmap);
    }

    // set payload length
    int payloadLength = packet.writerIndex() - packetStartIdx - 4;
    packet.setMediumLE(packetStartIdx, payloadLength);

    sendPacket(packet, payloadLength);
  }

  private void sendStatementFetchCommand(long statementId, int count) {
    ByteBuf packet = allocateBuffer();
    // encode packet header
    int packetStartIdx = packet.writerIndex();
    packet.writeMediumLE(0); // will set payload length later by calculation
    packet.writeByte(encoder.sequenceId);

    // encode packet payload
    packet.writeByte(CommandType.COM_STMT_FETCH);
    packet.writeIntLE((int) statementId);
    packet.writeIntLE(count);

    // set payload length
    int lenOfPayload = packet.writerIndex() - packetStartIdx - 4;
    packet.setMediumLE(packetStartIdx, lenOfPayload);

    encoder.chctx.writeAndFlush(packet);
  }
}
