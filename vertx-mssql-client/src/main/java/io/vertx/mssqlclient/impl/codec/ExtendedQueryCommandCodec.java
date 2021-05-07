/*
 * Copyright (c) 2011-2021 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.mssqlclient.impl.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.vertx.mssqlclient.impl.protocol.MessageStatus;
import io.vertx.mssqlclient.impl.protocol.MessageType;
import io.vertx.mssqlclient.impl.protocol.TdsMessage;
import io.vertx.mssqlclient.impl.protocol.client.rpc.ProcId;
import io.vertx.mssqlclient.impl.protocol.datatype.MSSQLDataTypeId;
import io.vertx.mssqlclient.impl.protocol.server.DoneToken;
import io.vertx.mssqlclient.impl.protocol.token.DataPacketStreamTokenType;
import io.vertx.sqlclient.Tuple;
import io.vertx.sqlclient.data.Numeric;
import io.vertx.sqlclient.impl.command.ExtendedQueryCommand;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;

import static io.vertx.mssqlclient.impl.codec.MSSQLDataTypeCodec.inferenceParamDefinitionByValueType;

class ExtendedQueryCommandCodec<T> extends QueryCommandBaseCodec<T, ExtendedQueryCommand<T>> {

  private int rowCount;

  ExtendedQueryCommandCodec(ExtendedQueryCommand cmd) {
    super(cmd);
  }

  @Override
  void encode(TdsMessageEncoder encoder) {
    super.encode(encoder);
    MSSQLPreparedStatement ps = (MSSQLPreparedStatement) cmd.preparedStatement();
    if (ps.handle > 0) {
      sendExecRequest();
    } else {
      sendPrepexecRequest();
    }
  }

  @Override
  void decodeMessage(TdsMessage message, TdsMessageEncoder encoder) {
    ByteBuf messageBody = message.content();
    while (messageBody.isReadable()) {
      DataPacketStreamTokenType tokenType = DataPacketStreamTokenType.valueOf(messageBody.readUnsignedByte());
      if (tokenType == null) {
        throw new UnsupportedOperationException("Unsupported token: " + tokenType);
      }
      MSSQLPreparedStatement ps = (MSSQLPreparedStatement) cmd.preparedStatement();
      switch (tokenType) {
        case COLMETADATA_TOKEN:
          MSSQLRowDesc rowDesc = decodeColmetadataToken(messageBody);
          rowResultDecoder = new RowResultDecoder<>(cmd.collector(), rowDesc);
          break;
        case ROW_TOKEN:
          handleRow(messageBody);
          break;
        case NBCROW_TOKEN:
          handleNbcRow(messageBody);
          break;
        case DONE_TOKEN:
          messageBody.skipBytes(12); // this should only be after ERROR_TOKEN?
          break;
        case INFO_TOKEN:
        case ORDER_TOKEN:
          int tokenLength = messageBody.readUnsignedShortLE();
          messageBody.skipBytes(tokenLength);
          break;
        case ERROR_TOKEN:
          handleErrorToken(messageBody);
          break;
        case DONEPROC_TOKEN:
          messageBody.skipBytes(messageBody.readableBytes());
          handleResultSetDone(rowCount);
          break;
        case DONEINPROC_TOKEN:
          short status = messageBody.readShortLE();
          short curCmd = messageBody.readShortLE();
          long doneRowCount = messageBody.readLongLE();
          if ((status | DoneToken.STATUS_DONE_FINAL) != 0) {
            rowCount += doneRowCount;
          } else {
            handleResultSetDone((int) doneRowCount);
          }
          break;
        case RETURNSTATUS_TOKEN:
          messageBody.skipBytes(4);
          break;
        case RETURNVALUE_TOKEN:
          if (ps.handle == 0) {
            messageBody.skipBytes(2); // skip ordinal position
            messageBody.skipBytes(2 * messageBody.readUnsignedByte()); // skip param name
            messageBody.skipBytes(1); // skip status
            messageBody.skipBytes(4); // skip user type
            messageBody.skipBytes(2); // skip flags
            messageBody.skipBytes(1); // skip type id
            messageBody.skipBytes(2); // max length and length
            ps.handle = messageBody.readIntLE();
          }
          messageBody.skipBytes(messageBody.readableBytes()); // FIXME
          handleResultSetDone(rowCount);
          break;
        default:
          throw new UnsupportedOperationException("Unsupported token: " + tokenType);
      }
    }
    complete();
  }

  private void sendPrepexecRequest() {
    ChannelHandlerContext chctx = encoder.chctx;

    ByteBuf packet = chctx.alloc().ioBuffer();

    // packet header
    packet.writeByte(MessageType.RPC.value());
    packet.writeByte(MessageStatus.NORMAL.value() | MessageStatus.END_OF_MESSAGE.value());
    int packetLenIdx = packet.writerIndex();
    packet.writeShort(0); // set length later
    packet.writeShort(0x00);
    packet.writeByte(0x00); // FIXME packet ID
    packet.writeByte(0x00);

    int start = packet.writerIndex();
    packet.writeIntLE(0x00); // TotalLength for ALL_HEADERS
    encodeTransactionDescriptor(packet);
    // set TotalLength for ALL_HEADERS
    packet.setIntLE(start, packet.writerIndex() - start);

    /*
      RPCReqBatch
     */
    packet.writeShortLE(0xFFFF);
    packet.writeShortLE(ProcId.Sp_PrepExec);

    // Option flags
    packet.writeShortLE(0x0000);

    // Parameter

    // OUT Parameter
    packet.writeByte(0x00);
    packet.writeByte(0x01); // By reference
    packet.writeByte(MSSQLDataTypeId.INTNTYPE_ID);
    packet.writeByte(0x04);
    packet.writeByte(0x04);
    MSSQLPreparedStatement ps = (MSSQLPreparedStatement) cmd.ps;
    packet.writeIntLE(ps.handle);

    Tuple params = cmd.params();

    // Param definitions
    String paramDefinitions = parseParamDefinitions(params);
    encodeNVarcharParameter(packet, paramDefinitions);

    // SQL text
    encodeNVarcharParameter(packet, cmd.sql());

    // Param values
    for (int i = 0; i < params.size(); i++) {
      encodeParamValue(packet, params.getValue(i));
    }

    int packetLen = packet.writerIndex() - packetLenIdx + 2;
    packet.setShort(packetLenIdx, packetLen);

    chctx.writeAndFlush(packet);
  }

  private void sendExecRequest() {
    ChannelHandlerContext chctx = encoder.chctx;

    ByteBuf packet = chctx.alloc().ioBuffer();

    // packet header
    packet.writeByte(MessageType.RPC.value());
    packet.writeByte(MessageStatus.NORMAL.value() | MessageStatus.END_OF_MESSAGE.value());
    int packetLenIdx = packet.writerIndex();
    packet.writeShort(0); // set length later
    packet.writeShort(0x00);
    packet.writeByte(0x00); // FIXME packet ID
    packet.writeByte(0x00);

    int start = packet.writerIndex();
    packet.writeIntLE(0x00); // TotalLength for ALL_HEADERS
    encodeTransactionDescriptor(packet);
    // set TotalLength for ALL_HEADERS
    packet.setIntLE(start, packet.writerIndex() - start);

    /*
      RPCReqBatch
     */
    packet.writeShortLE(0xFFFF);
    packet.writeShortLE(ProcId.Sp_Execute);

    // Option flags
    packet.writeShortLE(0x0000);

    // Parameter

    // OUT Parameter
    packet.writeByte(0x00);
    packet.writeByte(0x00);
    packet.writeByte(MSSQLDataTypeId.INTNTYPE_ID);
    packet.writeByte(0x04); // Max length
    packet.writeByte(0x04); // Length
    MSSQLPreparedStatement ps = (MSSQLPreparedStatement) cmd.ps;
    packet.writeIntLE(ps.handle);

    Tuple params = cmd.params();

    // Param values
    for (int i = 0; i < params.size(); i++) {
      encodeParamValue(packet, params.getValue(i));
    }

    int packetLen = packet.writerIndex() - packetLenIdx + 2;
    packet.setShort(packetLenIdx, packetLen);

    chctx.writeAndFlush(packet, encoder.chctx.voidPromise());
  }

  private String parseParamDefinitions(Tuple params) {
    StringBuilder stringBuilder = new StringBuilder();
    for (int i = 0; i < params.size(); i++) {
      Object param = params.getValue(i);
      stringBuilder.append("@P").append(i + 1).append(" ");
      stringBuilder.append(inferenceParamDefinitionByValueType(param));
      if (i != params.size() - 1) {
        stringBuilder.append(",");
      }
    }
    return stringBuilder.toString();
  }

  private void encodeNVarcharParameter(ByteBuf payload, String value) {
    payload.writeByte(0x00); // name length
    payload.writeByte(0x00); // status flags
    payload.writeByte(MSSQLDataTypeId.NVARCHARTYPE_ID);
    payload.writeShortLE(8000); // maximal length
    payload.writeByte(0x09);
    payload.writeByte(0x04);
    payload.writeByte(0xd0);
    payload.writeByte(0x00);
    payload.writeByte(0x34); // Collation for param definitions TODO always this value?
    writeUnsignedShortLenVarChar(payload, value);
  }

  private void encodeParamValue(ByteBuf payload, Object value) {
    if (value == null) {
      encodeNullParameter(payload);
    } else if (value instanceof Byte) {
      encodeIntNParameter(payload, 1, value);
    } else if (value instanceof Short) {
      encodeIntNParameter(payload, 2, value);
    } else if (value instanceof Integer) {
      encodeIntNParameter(payload, 4, value);
    } else if (value instanceof Long) {
      encodeIntNParameter(payload, 8, value);
    } else if (value instanceof Float) {
      encodeFloat4Parameter(payload, (Float) value);
    } else if (value instanceof Double) {
      encodeFloat8Parameter(payload, (Double) value);
    } else if (value instanceof String) {
      encodeNVarcharParameter(payload, (String) value);
    } else if (value instanceof Enum){
      encodeNVarcharParameter(payload, ((Enum<?>)value).name());
    } else if (value instanceof Boolean) {
      encodeBitNParameter(payload, (Boolean) value);
    } else if (value instanceof LocalDate) {
      encodeDateNParameter(payload, (LocalDate) value);
    } else if (value instanceof LocalTime) {
      encodeTimeNParameter(payload, (LocalTime) value, (byte) 6);
    } else if (value instanceof LocalDateTime) {
      encodeDateTimeNParameter(payload, (LocalDateTime) value, (byte) 6);
    } else if (value instanceof OffsetDateTime) {
      encodeOffsetDateTimeNParameter(payload, (OffsetDateTime) value, (byte) 6);
    } else if (value instanceof Numeric) {
      encodeNumericParameter(payload, (Numeric) value);
    } else {
      throw new UnsupportedOperationException("Unsupported type");
    }
  }

  private void encodeNullParameter(ByteBuf payload) {
    payload.writeByte(0x00);
    payload.writeByte(0x00);
    payload.writeByte(MSSQLDataTypeId.NULLTYPE_ID);
  }

  private void encodeIntNParameter(ByteBuf payload, int n, Object value) {
    payload.writeByte(0x00);
    payload.writeByte(0x00);
    payload.writeByte(MSSQLDataTypeId.INTNTYPE_ID);
    payload.writeByte(n);
    payload.writeByte(n);
    switch (n) {
      case 1:
        payload.writeByte((Byte) value);
        break;
      case 2:
        payload.writeShortLE((Short) value);
        break;
      case 4:
        payload.writeIntLE((Integer) value);
        break;
      case 8:
        payload.writeLongLE((Long) value);
        break;
      default:
        throw new UnsupportedOperationException();
    }
  }

  private void encodeBitNParameter(ByteBuf payload, Boolean bit) {
    payload.writeByte(0x00);
    payload.writeByte(0x00);
    payload.writeByte(MSSQLDataTypeId.BITNTYPE_ID);
    payload.writeByte(1);
    payload.writeByte(1);
    payload.writeBoolean(bit);
  }

  private void encodeFloat4Parameter(ByteBuf payload, Float value) {
    payload.writeByte(0x00);
    payload.writeByte(0x00);
    payload.writeByte(MSSQLDataTypeId.FLTNTYPE_ID);
    payload.writeByte(4);
    payload.writeByte(4);
    payload.writeFloatLE(value);
  }

  private void encodeFloat8Parameter(ByteBuf payload, Double value) {
    payload.writeByte(0x00);
    payload.writeByte(0x00);
    payload.writeByte(MSSQLDataTypeId.FLTNTYPE_ID);
    payload.writeByte(8);
    payload.writeByte(8);
    payload.writeDoubleLE(value);
  }

  private void encodeDateNParameter(ByteBuf payload, LocalDate date) {
    payload.writeByte(0x00);
    payload.writeByte(0x00);
    payload.writeByte(MSSQLDataTypeId.DATENTYPE_ID);
    if (date == null) {
      // null
      payload.writeByte(0);
    } else {
      payload.writeByte(3);
      long days = ChronoUnit.DAYS.between(MSSQLDataTypeCodec.START_DATE, date);
      payload.writeMediumLE((int) days);
    }
  }

  private void encodeTimeNParameter(ByteBuf payload, LocalTime time, byte scale) {
    payload.writeByte(0x00);
    payload.writeByte(0x00);
    payload.writeByte(MSSQLDataTypeId.TIMENTYPE_ID);

    payload.writeByte(scale); //FIXME scale?
    if (time == null) {
      payload.writeByte(0);
    } else {
      int length;
      if (scale <= 2) {
        length = 3;
      } else if (scale <= 4) {
        length = 4;
      } else {
        length = 5;
      }
      payload.writeByte(length);
      long nanos = time.getNano();
      int seconds = time.toSecondOfDay();
      long value = (long) ((long) seconds * Math.pow(10, scale) + nanos);
      encodeInt40(payload, value);
    }
  }

  private void encodeDateTimeNParameter(ByteBuf payload, LocalDateTime dateTime, byte scale) {
    payload.writeByte(0x00);
    payload.writeByte(0x00);
    payload.writeByte(MSSQLDataTypeId.DATETIME2NTYPE_ID);

    payload.writeByte(scale); //FIXME scale?
    if (dateTime == null) {
      payload.writeByte(0);
    } else {
      int length;
      if (scale <= 2) {
        length = 3;
      } else if (scale <= 4) {
        length = 4;
      } else {
        length = 5;
      }
      length += 3;
      payload.writeByte(length);
      LocalTime localTime = dateTime.toLocalTime();
      long nanos = localTime.getNano();
      int seconds = localTime.toSecondOfDay();
      long value = (long) ((long) seconds * Math.pow(10, scale) + nanos);
      encodeInt40(payload, value);
      long days = ChronoUnit.DAYS.between(MSSQLDataTypeCodec.START_DATE, dateTime.toLocalDate());
      payload.writeMediumLE((int) days);
    }
  }

  private void encodeOffsetDateTimeNParameter(ByteBuf payload, OffsetDateTime offsetDateTime, byte scale) {
    payload.writeByte(0x00);
    payload.writeByte(0x00);
    payload.writeByte(MSSQLDataTypeId.DATETIMEOFFSETNTYPE_ID);

    payload.writeByte(scale); //FIXME scale?
    if (offsetDateTime == null) {
      payload.writeByte(0);
    } else {
      int length;
      if (scale <= 2) {
        length = 3;
      } else if (scale <= 4) {
        length = 4;
      } else {
        length = 5;
      }
      length += 5;
      payload.writeByte(length);
      int minutes = offsetDateTime.getOffset().getTotalSeconds() / 60;
      LocalDateTime localDateTime = offsetDateTime.toLocalDateTime().minusMinutes(minutes);
      LocalTime localTime = localDateTime.toLocalTime();
      long nanos = localTime.getNano();
      int seconds = localTime.toSecondOfDay();
      long value = (long) ((long) seconds * Math.pow(10, scale) + nanos);
      encodeInt40(payload, value);
      long days = ChronoUnit.DAYS.between(MSSQLDataTypeCodec.START_DATE, localDateTime.toLocalDate());
      payload.writeMediumLE((int) days);
      payload.writeShortLE(minutes);
    }
  }

  private void encodeInt40(ByteBuf buffer, long value) {
    buffer.writeIntLE((int) (value % 0x100000000L));
    buffer.writeByte((int) (value / 0x100000000L));
  }

  private void encodeNumericParameter(ByteBuf payload, Numeric value) {
    BigDecimal bigDecimal = value.bigDecimalValue();
    if (bigDecimal == null) {
      encodeNullParameter(payload);
      return;
    }

    payload.writeByte(0x00);
    payload.writeByte(0x00);
    payload.writeByte(MSSQLDataTypeId.DECIMALNTYPE_ID);

    payload.writeByte(17); // maximum length
    payload.writeByte(38); // maximum precision

    int sign = bigDecimal.signum() < 0 ? 0 : 1;
    byte[] bytes = (sign == 0 ? bigDecimal.negate() : bigDecimal).unscaledValue().toByteArray();

    payload.writeByte(Math.max(0, bigDecimal.scale()));
    payload.writeByte(1 + bytes.length);
    payload.writeByte(sign);
    for (int i = bytes.length - 1; i >= 0; i--) {
      payload.writeByte(bytes[i]);
    }
  }
}
