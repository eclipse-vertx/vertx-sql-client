/*
 * Copyright (c) 2011-2019 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.mysqlclient.impl.codec;

import io.netty.buffer.ByteBuf;
import io.vertx.mysqlclient.impl.MySQLRowDesc;
import io.vertx.mysqlclient.impl.datatype.DataFormat;
import io.vertx.mysqlclient.impl.datatype.DataType;
import io.vertx.mysqlclient.impl.datatype.DataTypeCodec;
import io.vertx.mysqlclient.impl.protocol.ColumnDefinition;
import io.vertx.mysqlclient.impl.protocol.CommandType;
import io.vertx.sqlclient.Tuple;
import io.vertx.sqlclient.impl.command.ExtendedQueryCommand;

import static io.vertx.mysqlclient.impl.protocol.Packets.*;

abstract class ExtendedQueryCommandBaseCodec<R, C extends ExtendedQueryCommand<R>> extends QueryCommandBaseCodec<R, C> {

  protected final MySQLPreparedStatement statement;

  ExtendedQueryCommandBaseCodec(C cmd) {
    super(cmd, DataFormat.BINARY);
    statement = (MySQLPreparedStatement) cmd.preparedStatement();
  }

  @Override
  protected void handleInitPacket(ByteBuf payload) {
    // may receive ERR_Packet, OK_Packet, Binary Protocol Resultset
    int firstByte = payload.getUnsignedByte(payload.readerIndex());
    if (firstByte == OK_PACKET_HEADER) {
      OkPacket okPacket = decodeOkPacketPayload(payload);
      handleSingleResultsetDecodingCompleted(okPacket.serverStatusFlags(), okPacket.affectedRows(), okPacket.lastInsertId());
    } else if (firstByte == ERROR_PACKET_HEADER) {
      handleErrorPacketPayload(payload);
    } else {
      handleResultsetColumnCountPacketBody(payload);
    }
  }

  @Override
  protected final void handleResultsetColumnCountPacketBody(ByteBuf payload) {
    int columnCount = decodeColumnCountPacketPayload(payload);
    if (encoder.socketConnection.isOptionalMetadataSupported) {
      boolean metadataFollows = payload.readBoolean();
      if (!metadataFollows) {
        commandHandlerState = CommandHandlerState.HANDLING_ROW_DATA_OR_END_PACKET;
        decoder = new RowResultDecoder<>(cmd.collector(), statement.rowDesc);
        return;
      }
    }
    commandHandlerState = CommandHandlerState.HANDLING_COLUMN_DEFINITION;
    columnDefinitions = new ColumnDefinition[columnCount];
  }

  protected final void sendStatementExecuteCommand(MySQLPreparedStatement statement, boolean sendTypesToServer, Tuple params, byte cursorType) {
    ByteBuf packet = allocateBuffer();
    // encode packet header
    int packetStartIdx = packet.writerIndex();
    packet.writeMediumLE(0); // will set payload length later by calculation
    packet.writeByte(sequenceId);

    // encode packet payload
    packet.writeByte(CommandType.COM_STMT_EXECUTE);
    packet.writeIntLE((int) statement.statementId);
    packet.writeByte(cursorType);
    // iteration count, always 1
    packet.writeIntLE(1);

    int numOfParams = statement.bindingTypes().length;
    int bitmapLength = (numOfParams + 7) / 8;
    byte[] nullBitmap = new byte[bitmapLength];

    int pos = packet.writerIndex();

    if (numOfParams > 0) {
      // write a dummy bitmap first
      packet.writeBytes(nullBitmap);
      packet.writeBoolean(sendTypesToServer);

      if (sendTypesToServer) {
        for (DataType bindingType : statement.bindingTypes()) {
          packet.writeByte(bindingType.id);
          packet.writeByte(0); // parameter flag: signed
        }
      }

      for (int i = 0; i < numOfParams; i++) {
        Object value = params.getValue(i);
        if (value != null) {
          DataTypeCodec.encodeBinary(statement.bindingTypes()[i], value, encoder.encodingCharset, packet);
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

  protected final void sendCloseStatementCommand(MySQLPreparedStatement statement) {
    ByteBuf packet = allocateBuffer(9);
    // encode packet header
    packet.writeMediumLE(5);
    packet.writeByte(0); // sequenceId set to zero

    // encode packet payload
    packet.writeByte(CommandType.COM_STMT_CLOSE);
    packet.writeIntLE((int) statement.statementId);

    sendNonSplitPacket(packet);
  }
}
