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
import io.vertx.mysqlclient.impl.protocol.backend.ColumnDefinition;
import io.vertx.mysqlclient.impl.protocol.backend.OkPacket;
import io.vertx.mysqlclient.impl.util.BufferUtils;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.impl.RowDesc;
import io.vertx.sqlclient.impl.command.CommandResponse;
import io.vertx.sqlclient.impl.command.QueryCommandBase;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collector;

import static io.reactiverse.myclient.impl.protocol.backend.ServerStatusFlags.*;
import static io.vertx.mysqlclient.impl.protocol.backend.EofPacket.EOF_PACKET_HEADER;
import static io.vertx.mysqlclient.impl.protocol.backend.ErrPacket.ERROR_PACKET_HEADER;
import static io.vertx.mysqlclient.impl.protocol.backend.OkPacket.OK_PACKET_HEADER;

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
  void decodePayload(ByteBuf payload, MySQLEncoder encoder, int payloadLength, int sequenceId) {
    switch (commandHandlerState) {
      case INIT:
        // may receive ERR_Packet, OK_Packet, LOCAL INFILE Request, Text Resultset
        int firstByte = payload.getUnsignedByte(payload.readerIndex());
        if (firstByte == OK_PACKET_HEADER) {
          handleSingleResultsetDecodingCompleted(payload);
        } else if (firstByte == ERROR_PACKET_HEADER) {
          handleErrorPacketPayload(payload);
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
          // all column definitions have been handled, switch to row data handling
          commandHandlerState = CommandHandlerState.HANDLING_ROW_DATA_OR_END_PACKET;
          decoder = new RowResultDecoder<>(cmd.collector(), false/*cmd.isSingleton()*/, new MySQLRowDesc(columnDefinitions, format));
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
          handleErrorPacketPayload(payload);
           resetIntermediaryResult();
        }
        // enabling CLIENT_DEPRECATE_EOF capability will receive an OK_Packet with a EOF_Packet header here
        // we need check this is not a row data by checking packet length < 0xFFFFFF
        else if (first == EOF_PACKET_HEADER && payloadLength < 0xFFFFFF) {
          handleSingleResultsetDecodingCompleted(payload);
          resetIntermediaryResult();
        } else {
          // accept a row data
          decoder.decodeRow(columnDefinitions.length, payload);
        }
        break;
    }
  }

  private void handleSingleResultsetDecodingCompleted(ByteBuf payload) {
    // we have checked the header should be ERROR_PACKET_HEADER
    payload.readByte(); // skip header
    OkPacket okPacket = GenericPacketPayloadDecoder.decodeOkPacketBody(payload, StandardCharsets.UTF_8);
    handleResultsetEndPacket(okPacket);
    if ((okPacket.getServerStatusFlags() & SERVER_MORE_RESULTS_EXISTS) == 0) {
      // no more sql result
      handleAllResultsetDecodingCompleted(cmd);
    }
  }

  private void handleResultsetEndPacket(OkPacket okPacket) {
    this.result = false;
    T result;
    int size;
    RowDesc rowDesc;
    if (decoder != null) {
      result = decoder.complete();
      rowDesc = decoder.rowDesc;
      size = decoder.size();
      decoder.reset();
    } else {
      result = emptyResult(cmd.collector());
      size = 0;
      rowDesc = null;
    }
    cmd.resultHandler().handleResult((int) okPacket.getAffectedRows(), size, rowDesc, result);
  }

  private void handleAllResultsetDecodingCompleted(QueryCommandBase<?> cmd) {
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

  private static <A, T> T emptyResult(Collector<Row, A, T> collector) {
    return collector.finisher().apply(collector.supplier().get());
  }

}
