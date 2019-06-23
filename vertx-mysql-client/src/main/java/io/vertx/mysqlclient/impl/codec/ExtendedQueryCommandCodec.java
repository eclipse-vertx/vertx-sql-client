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
import io.vertx.sqlclient.impl.command.ExtendedQueryCommand;

import static io.vertx.mysqlclient.impl.codec.Packets.*;

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
      sendStatementFetchCommand(statement.statementId, cmd.fetch());
      decoder = new RowResultDecoder<>(cmd.collector(), false, statement.rowDesc);
    } else {
      if (cmd.fetch() > 0) {
        //TODO Cursor_type is READ_ONLY?
        sendStatementExecuteCommand(statement.statementId, statement.paramDesc.paramDefinitions(), sendType, cmd.params(), (byte) 0x01);
      } else {
        // CURSOR_TYPE_NO_CURSOR
        sendStatementExecuteCommand(statement.statementId, statement.paramDesc.paramDefinitions(), sendType, cmd.params(), (byte) 0x00);
      }
    }
  }

  @Override
  void decodePayload(ByteBuf payload, int payloadLength, int sequenceId) {
    if (statement.isCursorOpen) {
      // decoding COM_STMT_FETCH response
      handleRows(payload, payloadLength, super::handleSingleRow);
    } else {
      // decoding COM_STMT_EXECUTE response
      if (cmd.fetch() > 0) {
        switch (commandHandlerState) {
          case INIT:
            handleResultsetColumnCountPacketBody(payload);
            break;
          case HANDLING_COLUMN_DEFINITION:
            handleResultsetColumnDefinitions(payload);
            break;
          case HANDLING_ROW_DATA_OR_END_PACKET:
            /*
              Resultset row can begin with 0xfe byte (when using text protocol with a field length > 0xffffff)
              To ensure that packets beginning with 0xfe correspond to the ending packet (EOF_Packet or OK_Packet with a 0xFE header),
              the packet length must be checked and must be less than 0xffffff in length.
             */
            int first = payload.getUnsignedByte(payload.readerIndex());
            if (first == ERROR_PACKET_HEADER) {
              handleErrorPacketPayload(payload);
            }
            // enabling CLIENT_DEPRECATE_EOF capability will receive an OK_Packet with a EOF_Packet header here
            // we need check this is not a row data by checking packet length < 0xFFFFFF
            else if (first == EOF_PACKET_HEADER && payloadLength < 0xFFFFFF) {
              // need to reset packet number so that we can send a fetch request
              this.sequenceId = 0;
              // send fetch after cursor opened
              decoder = new RowResultDecoder<>(cmd.collector(), false, statement.rowDesc);

              statement.isCursorOpen = true;

              sendStatementFetchCommand(statement.statementId, cmd.fetch());
            }
            break;
        }
      } else {
        super.decodePayload(payload, payloadLength, sequenceId);
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

    encoder.chctx.writeAndFlush(packet);
  }
}
