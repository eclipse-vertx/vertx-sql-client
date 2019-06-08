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
import io.vertx.mysqlclient.impl.util.BufferUtils;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.impl.RowDesc;
import io.vertx.sqlclient.impl.command.CommandResponse;
import io.vertx.sqlclient.impl.command.QueryCommandBase;

import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import java.util.stream.Collector;

import static io.vertx.mysqlclient.impl.codec.Packets.*;

abstract class QueryCommandBaseCodec<T, C extends QueryCommandBase<T>> extends CommandCodec<Boolean, C> {

  private final DataFormat format;

  protected CommandHandlerState commandHandlerState = CommandHandlerState.INIT;
  protected ColumnDefinition[] columnDefinitions;
  protected RowResultDecoder<?, T> decoder;
  private int currentColumn;

  QueryCommandBaseCodec(C cmd, DataFormat format) {
    super(cmd);
    this.format = format;
  }

  private static <A, T> T emptyResult(Collector<Row, A, T> collector) {
    return collector.finisher().apply(collector.supplier().get());
  }

  @Override
  void decodePayload(ByteBuf payload, MySQLEncoder encoder, int payloadLength, int sequenceId) {
    switch (commandHandlerState) {
      case INIT:
        handleInitPacket(payload);
        break;
      case HANDLING_COLUMN_DEFINITION:
        handleResultsetColumnDefinitions(payload);
        break;
      case HANDLING_ROW_DATA_OR_END_PACKET:
        handleRows(payload, payloadLength, this::handleSingleRow);
        break;
    }
  }

  protected abstract void handleInitPacket(ByteBuf payload);

  protected void handleResultsetColumnCountPacketBody(ByteBuf payload) {
    int columnCount = decodeColumnCountPacketPayload(payload);
    commandHandlerState = CommandHandlerState.HANDLING_COLUMN_DEFINITION;
    columnDefinitions = new ColumnDefinition[columnCount];
  }

  protected void handleResultsetColumnDefinitions(ByteBuf payload) {
    ColumnDefinition def = decodeColumnDefinitionPacketPayload(payload);
    columnDefinitions[currentColumn++] = def;
    if (currentColumn == columnDefinitions.length) {
      // all column definitions have been handled, switch to row data handling
      commandHandlerState = CommandHandlerState.HANDLING_ROW_DATA_OR_END_PACKET;
      decoder = new RowResultDecoder<>(cmd.collector(), false/*cmd.isSingleton()*/, new MySQLRowDesc(columnDefinitions, format));
    }
  }

  protected void handleRows(ByteBuf payload, int payloadLength, Consumer<ByteBuf> singleRowHandler) {
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
      handleSingleResultsetDecodingCompleted(payload);
    } else {
      singleRowHandler.accept(payload);
    }
  }

  protected void handleSingleRow(ByteBuf payload) {
    // accept a row data
    decoder.decodeRow(columnDefinitions.length, payload);
  }

  protected void handleSingleResultsetDecodingCompleted(ByteBuf payload) {
    OkPacket okPacket = decodeOkPacketPayload(payload, StandardCharsets.UTF_8);
    handleSingleResultsetEndPacket(okPacket);
    resetIntermediaryResult();
    if ((okPacket.serverStatusFlags() & ServerStatusFlags.SERVER_MORE_RESULTS_EXISTS) == 0) {
      // no more sql result
      handleAllResultsetDecodingCompleted(cmd);
    }
  }

  private void handleSingleResultsetEndPacket(OkPacket okPacket) {
    this.result = (okPacket.serverStatusFlags() & ServerStatusFlags.SERVER_STATUS_LAST_ROW_SENT) == 0;
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
    cmd.resultHandler().handleResult((int) okPacket.affectedRows(), size, rowDesc, result);
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

  protected enum CommandHandlerState {
    INIT,
    HANDLING_COLUMN_DEFINITION,
    HANDLING_ROW_DATA_OR_END_PACKET
  }
}
