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
import io.vertx.mysqlclient.impl.protocol.CommandType;
import io.vertx.sqlclient.Tuple;
import io.vertx.sqlclient.impl.command.CommandResponse;
import io.vertx.sqlclient.impl.command.ExtendedQueryCommand;

import static io.vertx.mysqlclient.impl.protocol.Packets.ERROR_PACKET_HEADER;
import static io.vertx.mysqlclient.impl.protocol.Packets.EnumCursorType.CURSOR_TYPE_NO_CURSOR;
import static io.vertx.mysqlclient.impl.protocol.Packets.EnumCursorType.CURSOR_TYPE_READ_ONLY;

class ExtendedQueryCommandCodec<R> extends ExtendedQueryCommandBaseCodec<R, ExtendedQueryCommand<R>> {
  ExtendedQueryCommandCodec(ExtendedQueryCommand<R> cmd) {
    super(cmd);
    if (cmd.fetch() > 0 && statement.isCursorOpen) {
      // restore the state we need for decoding fetch response based on the prepared statement
      columnDefinitions = statement.rowDesc.columnDefinitions();
    }
  }

  @Override
  void encode(MySQLEncoder encoder) {
    super.encode(encoder);

    if (statement.isCursorOpen) {
      if (decoder == null) {
        // restore the state we need for decoding if column definitions are not included in the fetch response
        decoder = new RowResultDecoder<>(cmd.collector(), statement.rowDesc);
      }
      sendStatementFetchCommand(statement.statementId, cmd.fetch());
    } else {
      Tuple params = cmd.params();
      // binding parameters
      String bindMsg = statement.bindParameters(params);
      if (bindMsg != null) {
        encoder.handleCommandResponse(CommandResponse.failure(bindMsg));
        return;
      }

      if (cmd.fetch() > 0) {
        sendStatementExecuteCommand(statement, true, params, CURSOR_TYPE_READ_ONLY);
      } else {
        // CURSOR_TYPE_NO_CURSOR
        sendStatementExecuteCommand(statement, statement.sendTypesToServer(), params, CURSOR_TYPE_NO_CURSOR);

        // Close managed prepare statement
        MySQLPreparedStatement ps = (MySQLPreparedStatement) this.cmd.ps;
        if (ps.closeAfterUsage) {
          sendCloseStatementCommand(ps);
        }
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
        handleRows(payload, payloadLength);
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
            sequenceId = 0;
            // send fetch after cursor opened
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

  private void sendStatementFetchCommand(long statementId, int count) {
    ByteBuf packet = allocateBuffer();
    // encode packet header
    int packetStartIdx = packet.writerIndex();
    packet.writeMediumLE(0); // will set payload length later by calculation
    packet.writeByte(sequenceId);

    // encode packet payload
    packet.writeByte(CommandType.COM_STMT_FETCH);
    packet.writeIntLE((int) statementId);
    packet.writeIntLE(count);

    // set payload length
    int lenOfPayload = packet.writerIndex() - packetStartIdx - 4;
    packet.setMediumLE(packetStartIdx, lenOfPayload);

    encoder.chctx.writeAndFlush(packet, encoder.chctx.voidPromise());
  }
}
