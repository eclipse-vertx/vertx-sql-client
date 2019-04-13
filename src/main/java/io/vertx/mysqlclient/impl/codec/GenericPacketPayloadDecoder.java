package io.vertx.mysqlclient.impl.codec;

import io.netty.buffer.ByteBuf;
import io.vertx.mysqlclient.impl.codec.datatype.DataType;
import io.vertx.mysqlclient.impl.protocol.backend.ColumnDefinition;
import io.vertx.mysqlclient.impl.protocol.backend.ErrPacket;
import io.vertx.mysqlclient.impl.protocol.backend.OkPacket;
import io.vertx.mysqlclient.impl.util.BufferUtils;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

final class GenericPacketPayloadDecoder {
  private static final int SQL_STATE_MARKER_LENGTH = 1;
  private static final int SQL_STATE_LENGTH = 5;

  static OkPacket decodeOkPacketPayload(ByteBuf payloadBody, Charset charset) {
    long affectedRows = BufferUtils.readLengthEncodedInteger(payloadBody);
    long lastInsertId = BufferUtils.readLengthEncodedInteger(payloadBody);
    int serverStatusFlags = payloadBody.readUnsignedShortLE();
    int numberOfWarnings = payloadBody.readUnsignedShortLE();
    String info = readRestOfPacketString(payloadBody, charset);
    return new OkPacket(affectedRows, lastInsertId, serverStatusFlags, numberOfWarnings, info);
  }

  static ErrPacket decodeErrPacketPayload(ByteBuf payload, Charset charset) {
    int errorCode = payload.readUnsignedShortLE();
    String sqlStateMarker = BufferUtils.readFixedLengthString(payload, SQL_STATE_MARKER_LENGTH, charset);
    String sqlState = BufferUtils.readFixedLengthString(payload, SQL_STATE_LENGTH, charset);
    String errorMessage = readRestOfPacketString(payload, charset);
    return new ErrPacket(errorCode, sqlStateMarker, sqlState, errorMessage);
  }

  static String readRestOfPacketString(ByteBuf payload, Charset charset) {
    return BufferUtils.readFixedLengthString(payload, payload.readableBytes(), charset);
  }

  static ColumnDefinition decodeColumnDefinitionPacketPayload(ByteBuf payload) {
    String catalog = BufferUtils.readLengthEncodedString(payload, StandardCharsets.UTF_8);
    String schema = BufferUtils.readLengthEncodedString(payload, StandardCharsets.UTF_8);
    String table = BufferUtils.readLengthEncodedString(payload, StandardCharsets.UTF_8);
    String orgTable = BufferUtils.readLengthEncodedString(payload, StandardCharsets.UTF_8);
    String name = BufferUtils.readLengthEncodedString(payload, StandardCharsets.UTF_8);
    String orgName = BufferUtils.readLengthEncodedString(payload, StandardCharsets.UTF_8);
    long lengthOfFixedLengthFields = BufferUtils.readLengthEncodedInteger(payload);
    int characterSet = payload.readUnsignedShortLE();
    long columnLength = payload.readUnsignedIntLE();
    DataType type = DataType.valueOf(payload.readUnsignedByte());
    int flags = payload.readUnsignedShortLE();
    byte decimals = payload.readByte();
    return new ColumnDefinition(catalog, schema, table, orgTable, name, orgName, characterSet, columnLength, type, flags, decimals);
  }
}
