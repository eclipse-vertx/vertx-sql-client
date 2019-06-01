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
import io.vertx.core.buffer.Buffer;
import io.vertx.mysqlclient.impl.codec.datatype.DataFormat;
import io.vertx.mysqlclient.impl.codec.datatype.DataType;
import io.vertx.mysqlclient.impl.codec.datatype.DataTypeCodec;
import io.vertx.mysqlclient.impl.protocol.CommandType;
import io.vertx.mysqlclient.impl.protocol.backend.ColumnDefinition;
import io.vertx.sqlclient.Tuple;
import io.vertx.sqlclient.impl.command.ExtendedQueryCommand;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static io.vertx.mysqlclient.impl.protocol.backend.EofPacket.EOF_PACKET_HEADER;
import static io.vertx.mysqlclient.impl.protocol.backend.ErrPacket.ERROR_PACKET_HEADER;
import static io.vertx.mysqlclient.impl.protocol.backend.OkPacket.OK_PACKET_HEADER;

class ExtendedQueryCommandCodec<R> extends QueryCommandBaseCodec<R, ExtendedQueryCommand<R>> {

  // TODO handle re-bound situations?
  // Flag if parameters must be re-bound
  private final byte sendType = 1;

  private final MySQLPreparedStatement statement;

  ExtendedQueryCommandCodec(ExtendedQueryCommand<R> cmd) {
    super(cmd, DataFormat.BINARY);
    statement = (MySQLPreparedStatement) cmd.preparedStatement();
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
  void decodePayload(ByteBuf payload, MySQLEncoder encoder, int payloadLength, int sequenceId) {
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
        super.decodePayload(payload, encoder, payloadLength, sequenceId);
      }
    }
  }

  private void sendStatementExecuteCommand(long statementId, ColumnDefinition[] paramsColumnDefinitions, byte sendType, Tuple params, byte cursorType) {
    ByteBuf packet = allocateBuffer();
    // encode packet header
    int packetStartIdx = packet.writerIndex();
    packet.writeMediumLE(0); // will set payload length later by calculation
    packet.writeByte(sequenceId);

    // encode packet payload
    packet.writeByte(CommandType.COM_STMT_EXECUTE);
    packet.writeIntLE((int) statementId);
    packet.writeByte(cursorType);
    // iteration count, always 1
    packet.writeIntLE(1);

    int numOfParams = paramsColumnDefinitions.length;
    int bitmapLength = (numOfParams + 7) / 8;
    byte[] nullBitmap = new byte[bitmapLength];

    int pos = packet.writerIndex();

    if (numOfParams > 0) {
      // write a dummy bitmap first
      packet.writeBytes(nullBitmap);
      packet.writeByte(sendType);
      if (sendType == 1) {
        for (int i = 0; i < numOfParams; i++) {
          Object value = params.getValue(i);
          packet.writeByte(parseDataTypeByEncodingValue(value).id);
          packet.writeByte(0); // parameter flag: signed
        }
      }

      for (int i = 0; i < numOfParams; i++) {
        Object value = params.getValue(i);
        if (value != null) {
          DataTypeCodec.encodeBinary(parseDataTypeByEncodingValue(value), value, packet);
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

  @Override
  protected void handleInitPacket(ByteBuf payload) {
    // may receive ERR_Packet, OK_Packet, Binary Protocol Resultset
    int firstByte = payload.getUnsignedByte(payload.readerIndex());
    if (firstByte == OK_PACKET_HEADER) {
      handleSingleResultsetDecodingCompleted(payload);
    } else if (firstByte == ERROR_PACKET_HEADER) {
      handleErrorPacketPayload(payload);
    } else {
      handleResultsetColumnCountPacketBody(payload);
    }
  }

  private DataType parseDataTypeByEncodingValue(Object value) {
    if (value == null) {
      // ProtocolBinary::MYSQL_TYPE_NULL
      return DataType.NULL;
    } else if (value instanceof Byte) {
      // ProtocolBinary::MYSQL_TYPE_TINY
      return DataType.INT1;
    } else if (value instanceof Boolean) {
      // ProtocolBinary::MYSQL_TYPE_TINY
      return DataType.INT1;
    } else if (value instanceof Short) {
      // ProtocolBinary::MYSQL_TYPE_SHORT, ProtocolBinary::MYSQL_TYPE_YEAR
      return DataType.INT2;
    } else if (value instanceof Integer) {
      // ProtocolBinary::MYSQL_TYPE_LONG, ProtocolBinary::MYSQL_TYPE_INT24
      return DataType.INT4;
    } else if (value instanceof Long) {
      // ProtocolBinary::MYSQL_TYPE_LONGLONG
      return DataType.INT8;
    } else if (value instanceof Double) {
      // ProtocolBinary::MYSQL_TYPE_DOUBLE
      return DataType.DOUBLE;
    } else if (value instanceof Float) {
      // ProtocolBinary::MYSQL_TYPE_FLOAT
      return DataType.FLOAT;
    } else if (value instanceof LocalDate) {
      // ProtocolBinary::MYSQL_TYPE_DATE
      return DataType.DATE;
    } else if (value instanceof Duration) {
      // ProtocolBinary::MYSQL_TYPE_TIME
      return DataType.TIME;
    } else if (value instanceof Buffer) {
      // ProtocolBinary::MYSQL_TYPE_LONG_BLOB, ProtocolBinary::MYSQL_TYPE_MEDIUM_BLOB, ProtocolBinary::MYSQL_TYPE_BLOB, ProtocolBinary::MYSQL_TYPE_TINY_BLOB
      return DataType.BLOB;
    } else if (value instanceof LocalDateTime) {
      // ProtocolBinary::MYSQL_TYPE_DATETIME, ProtocolBinary::MYSQL_TYPE_TIMESTAMP
      return DataType.DATETIME;
    } else {
      /*
        ProtocolBinary::MYSQL_TYPE_STRING, ProtocolBinary::MYSQL_TYPE_VARCHAR, ProtocolBinary::MYSQL_TYPE_VAR_STRING,
        ProtocolBinary::MYSQL_TYPE_ENUM, ProtocolBinary::MYSQL_TYPE_SET, ProtocolBinary::MYSQL_TYPE_GEOMETRY,
        ProtocolBinary::MYSQL_TYPE_BIT, ProtocolBinary::MYSQL_TYPE_DECIMAL, ProtocolBinary::MYSQL_TYPE_NEWDECIMAL
       */
      return DataType.STRING;
    }
  }
}
