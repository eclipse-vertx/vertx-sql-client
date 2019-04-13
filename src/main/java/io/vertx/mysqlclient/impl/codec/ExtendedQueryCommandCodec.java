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

  private final MySQLPreparedStatement ps;

  ExtendedQueryCommandCodec(ExtendedQueryCommand<R> cmd) {
    super(cmd, DataFormat.BINARY);
    ps = (MySQLPreparedStatement) cmd.preparedStatement();
    if (cmd.fetch() > 0 && ps.isCursorOpen) {
      // restore the state we need for decoding fetch response
      columnDefinitions = ps.rowDesc.columnDefinitions();
    }
  }

  @Override
  void encode(MySQLEncoder encoder) {
    super.encode(encoder);

    if (ps.isCursorOpen) {
      writeFetchMessage(ps.statementId, cmd.fetch());
      decoder = new RowResultDecoder<>(cmd.collector(), false, ps.rowDesc);
    } else {
      if (cmd.fetch() > 0) {
        //TODO Cursor_type is READ_ONLY?
        writeExecuteMessage(ps.statementId, ps.paramDesc.paramDefinitions(), sendType, cmd.params(), (byte) 0x01);
      } else {
        // CURSOR_TYPE_NO_CURSOR
        writeExecuteMessage(ps.statementId, ps.paramDesc.paramDefinitions(), sendType, cmd.params(), (byte) 0x00);
      }
    }
  }

  @Override
  void decodePayload(ByteBuf payload, MySQLEncoder encoder, int payloadLength, int sequenceId) {
    if (ps.isCursorOpen) {
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
              decoder = new RowResultDecoder<>(cmd.collector(), false, ps.rowDesc);

              ps.isCursorOpen = true;

              writeFetchMessage(ps.statementId, cmd.fetch());
            }
            break;
        }
      } else {
        super.decodePayload(payload, encoder, payloadLength, sequenceId);
      }
    }
  }

  private void writeExecuteMessage(long statementId, ColumnDefinition[] paramsColumnDefinitions, byte sendType, Tuple params, byte cursorType) {
    encodePacket(payload -> {
      payload.writeByte(CommandType.COM_STMT_EXECUTE);
      payload.writeIntLE((int) statementId);
      payload.writeByte(cursorType);
      // iteration count, always 1
      payload.writeIntLE(1);

      int numOfParams = paramsColumnDefinitions.length;
      int bitmapLength = (numOfParams + 7) / 8;
      byte[] nullBitmap = new byte[bitmapLength];

      int pos = payload.writerIndex();

      if (numOfParams > 0) {
        // write a dummy bitmap first
        payload.writeBytes(nullBitmap);
        payload.writeByte(sendType);
        if (sendType == 1) {
          for (int i = 0; i < numOfParams; i++) {
            Object value = params.getValue(i);
            payload.writeByte(parseDataTypeByEncodingValue(value).id);
            payload.writeByte(0); // parameter flag: signed
          }
        }

        for (int i = 0; i < numOfParams; i++) {
          Object value = params.getValue(i);
          if (value != null) {
            DataTypeCodec.encodeBinary(parseDataTypeByEncodingValue(value), value, payload);
          } else {
            nullBitmap[i / 8] |= (1 << (i & 7));
          }
        }

        // padding null-bitmap content
        payload.setBytes(pos, nullBitmap);
      }
    });
  }

  private void writeFetchMessage(long statementId, int count) {
    encodePacket(payload -> {
      payload.writeByte(CommandType.COM_STMT_FETCH);
      payload.writeIntLE((int) statementId);
      payload.writeIntLE(count);
    });
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
    } else if (value instanceof LocalDateTime) {
      // ProtocolBinary::MYSQL_TYPE_DATETIME, ProtocolBinary::MYSQL_TYPE_TIMESTAMP
      return DataType.DATETIME;
    } else {
      /*
        ProtocolBinary::MYSQL_TYPE_STRING, ProtocolBinary::MYSQL_TYPE_VARCHAR, ProtocolBinary::MYSQL_TYPE_VAR_STRING,
        ProtocolBinary::MYSQL_TYPE_ENUM, ProtocolBinary::MYSQL_TYPE_SET, ProtocolBinary::MYSQL_TYPE_LONG_BLOB,
        ProtocolBinary::MYSQL_TYPE_MEDIUM_BLOB, ProtocolBinary::MYSQL_TYPE_BLOB, ProtocolBinary::MYSQL_TYPE_TINY_BLOB,
        ProtocolBinary::MYSQL_TYPE_GEOMETRY, ProtocolBinary::MYSQL_TYPE_BIT, ProtocolBinary::MYSQL_TYPE_DECIMAL,
        ProtocolBinary::MYSQL_TYPE_NEWDECIMAL
       */
      return DataType.STRING;
    }
  }
}
