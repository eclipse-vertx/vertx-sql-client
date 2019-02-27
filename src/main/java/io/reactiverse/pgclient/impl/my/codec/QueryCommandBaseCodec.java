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
package io.reactiverse.pgclient.impl.my.codec;

import io.netty.buffer.ByteBuf;
import io.reactiverse.pgclient.impl.my.ColumnMetadata;
import io.reactiverse.pgclient.impl.my.RowResultDecoder;
import io.reactiverse.pgclient.impl.my.codec.datatype.DataFormat;
import io.reactiverse.pgclient.impl.my.protocol.backend.ColumnDefinition;
import io.reactiverse.pgclient.impl.my.protocol.backend.OkPacket;
import io.reactiverse.pgclient.impl.my.util.BufferUtils;
import io.reactiverse.pgclient.impl.command.CommandResponse;
import io.reactiverse.pgclient.impl.command.QueryCommandBase;

import java.nio.charset.StandardCharsets;

import static io.reactiverse.pgclient.impl.my.protocol.backend.EofPacket.EOF_PACKET_HEADER;
import static io.reactiverse.pgclient.impl.my.protocol.backend.ErrPacket.ERROR_PACKET_HEADER;
import static io.reactiverse.pgclient.impl.my.protocol.backend.OkPacket.OK_PACKET_HEADER;

abstract class QueryCommandBaseCodec<T, C extends QueryCommandBase<T>> extends CommandCodec<Boolean, C> {

  private enum CommandHandlerState {
    INIT,
    HANDLING_COLUMN_DEFINITION,
    HANDLING_ROW_DATA_OR_END_PACKET // for COM_QUERY
  }

  private final DataFormat format;
  private CommandHandlerState commandHandlerState = CommandHandlerState.INIT;
  private ColumnDefinition[] columnDefinitions;
  private int currentColumn;
  private RowResultDecoder<?, T> decoder;

  public QueryCommandBaseCodec(C cmd, DataFormat format) {
    super(cmd);
    this.format = format;
  }

  @Override
  void decodePayload(ByteBuf payload, MyEncoder encoder, int payloadLength, int sequenceId) {

    switch (commandHandlerState) {
      case INIT:
        // may receive ERR_Packet, OK_Packet, LOCAL INFILE Request, Text Resultset
        int firstByte = payload.getUnsignedByte(payload.readerIndex());
        if (firstByte == OK_PACKET_HEADER) {
//          payload.readByte();
//          cmd.handleEndPacket(GenericPacketPayloadDecoder.decodeOkPacketBody(payload, charset));
//          handleResultsetDecodingCompleted(ctx, cmd);
        } else if (firstByte == ERROR_PACKET_HEADER) {
//          handleErrorPacketPayload(ctx, payload);
        } else if (firstByte == 0xFB) {
          //TODO LOCAL INFILE Request support
        } else {
          //regarded as Resultset handling
          int columnCount = decodeColumnCountPacketPayload(payload);
          commandHandlerState = CommandHandlerState.HANDLING_COLUMN_DEFINITION;
          columnDefinitions = new ColumnDefinition[columnCount];
        }
        break;
      case HANDLING_COLUMN_DEFINITION:
        ColumnDefinition def = decodeColumnDefinitionPacketPayload(payload);
        columnDefinitions[currentColumn++] = def;
        if (currentColumn == columnDefinitions.length) {
//          // all column definitions have been handled, switch to row data handling
          commandHandlerState = CommandHandlerState.HANDLING_ROW_DATA_OR_END_PACKET;
          decoder = new RowResultDecoder<>(cmd.collector(), false/*cmd.isSingleton()*/, new ColumnMetadata(columnDefinitions, format));
        }
        break;
      case HANDLING_ROW_DATA_OR_END_PACKET:
            /*
              Resultset row can begin with 0xfe byte (when using text protocol with a field length > 0xffffff)
              To ensure that packets beginning with 0xfe correspond to the ending packet (EOF_Packet or OK_Packet with a 0xFE header),
              the packet length must be checked and must be less than 0xffffff in length.
             */
        int first = payload.getUnsignedByte(payload.readerIndex());
        if (first == ERROR_PACKET_HEADER) {
          // handleErrorPacketPayload(ctx, payload);
          // resetIntermediaryResult();
        }
        // enabling CLIENT_DEPRECATE_EOF capability will receive an OK_Packet with a EOF_Packet header here
        // we need check this is not a row data by checking packet length < 0xFFFFFF
        else if (first == EOF_PACKET_HEADER && payloadLength + 4 < 0xFFFFFF) {
          payload.readByte();
          handleEndPacket(GenericPacketPayloadDecoder.decodeOkPacketBody(payload, StandardCharsets.UTF_8));
          handleResultsetDecodingCompleted(cmd);
          resetIntermediaryResult();
        } else {
          // accept a row data
          decoder.decodeRow(columnDefinitions.length, payload);
        }
        break;
    }

  }

  private void handleEndPacket(OkPacket okPacket) {
    this.result = false;
    T result;
    int size;
    ColumnMetadata columnMetadata;
    if (decoder != null) {
      result = decoder.complete();
      columnMetadata = decoder.columnMetadata();
      size = decoder.size();
      decoder.reset();
    } else {
      result = null;
      size = 0;
      columnMetadata = null;
    }
    cmd.resultHandler().handleResult((int) okPacket.getAffectedRows(), size, null, result);
  }

  private void handleResultsetDecodingCompleted(QueryCommandBase<?> cmd) {
    CommandResponse<Boolean> response;
    if (this.failure != null) {
      response = CommandResponse.failure(this.failure);
    } else {
      response = CommandResponse.success(this.result);
    }
    completionHandler.handle(response);
  }

  private int decodeColumnCountPacketPayload(ByteBuf payload) {
    long columnCount = BufferUtils.readLengthEncodedInteger(payload);
    return (int) columnCount;
  }

  private void resetIntermediaryResult() {
    commandHandlerState = CommandHandlerState.INIT;
    columnDefinitions = null;
    currentColumn = 0;
  }

}
