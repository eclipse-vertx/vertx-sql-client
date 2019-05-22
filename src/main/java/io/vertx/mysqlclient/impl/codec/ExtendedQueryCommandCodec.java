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
import io.vertx.mysqlclient.impl.codec.datatype.DataTypeCodec;
import io.vertx.mysqlclient.impl.protocol.CommandType;
import io.vertx.mysqlclient.impl.protocol.backend.ColumnDefinition;
import io.vertx.sqlclient.Tuple;
import io.vertx.sqlclient.impl.command.ExtendedQueryCommand;

import static io.vertx.mysqlclient.impl.protocol.backend.EofPacket.EOF_PACKET_HEADER;
import static io.vertx.mysqlclient.impl.protocol.backend.ErrPacket.ERROR_PACKET_HEADER;
import static io.vertx.mysqlclient.impl.protocol.backend.OkPacket.OK_PACKET_HEADER;

public class ExtendedQueryCommandCodec<R> extends QueryCommandBaseCodec<R, ExtendedQueryCommand<R>> {

  // TODO handle re-bound situations?
  // Flag if parameters must be re-bound
  private final byte sendType = 1;

  private final MySQLPreparedStatement ps;

  public ExtendedQueryCommandCodec(ExtendedQueryCommand<R> cmd) {
    super(cmd, DataFormat.BINARY);
    ps = (MySQLPreparedStatement) cmd.preparedStatement();
    if (cmd.fetch() > 0 && ps.isCursorOpen) {
      // restore the state we need for decoding fetch response
      columnDefinitions = ps.rowDesc.columnDefinitions();
    }
  }

  @Override
  void encodePayload(MySQLEncoder encoder) {
    super.encodePayload(encoder);

    if (ps.isCursorOpen) {
      writeFetchMessage(encoder, ps.statementId, cmd.fetch());
      decoder = new RowResultDecoder<>(cmd.collector(), false, ps.rowDesc);
    } else {
      if (cmd.fetch() > 0) {
        //TODO Cursor_type is READ_ONLY?
        writeExecuteMessage(encoder, ps.statementId, ps.paramDesc.paramDefinitions(), sendType, cmd.params(), (byte) 0x01);
      } else {
        // CURSOR_TYPE_NO_CURSOR
        writeExecuteMessage(encoder, ps.statementId, ps.paramDesc.paramDefinitions(), sendType, cmd.params(), (byte) 0x00);
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

              writeFetchMessage(encoder, ps.statementId, cmd.fetch());
            }
            break;
        }
      } else {
        super.decodePayload(payload, encoder, payloadLength, sequenceId);
      }
    }
  }

  private void writeExecuteMessage(MySQLEncoder encoder, long statementId, ColumnDefinition[] paramsColumnDefinitions, byte sendType, Tuple params, byte cursorType) {
    ByteBuf payload = encoder.chctx.alloc().ioBuffer();

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
          if (value != null) {
            payload.writeByte(paramsColumnDefinitions[i].getType().id);
          } else {
            payload.writeByte(ColumnDefinition.ColumnType.MYSQL_TYPE_NULL);
          }
          // TODO handle parameter flag (unsigned or signed)
          payload.writeByte(0);
        }

        for (int i = 0; i < numOfParams; i++) {
          Object value = params.getValue(i);
          //FIXME make sure we have correctly handled null value here
          if (value != null) {
            DataTypeCodec.encodeBinary(paramsColumnDefinitions[i].getType(), value, payload);
          } else {
            nullBitmap[i / 8] |= (1 << (i & 7));
          }
        }

      }

      // padding null-bitmap content
      payload.setBytes(pos, nullBitmap);
    }

    encoder.writePacketAndFlush(sequenceId++, payload);
  }

  private void writeFetchMessage(MySQLEncoder encoder, long statementId, int count) {
    ByteBuf payload = encoder.chctx.alloc().ioBuffer();

    payload.writeByte(CommandType.COM_STMT_FETCH);
    payload.writeIntLE((int) statementId);
    payload.writeIntLE(count);

    encoder.writePacketAndFlush(sequenceId++, payload);
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
}
