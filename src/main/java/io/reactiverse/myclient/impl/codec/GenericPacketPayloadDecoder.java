package io.reactiverse.myclient.impl.codec;

import io.netty.buffer.ByteBuf;
import io.reactiverse.myclient.impl.protocol.backend.ErrPacket;
import io.reactiverse.myclient.impl.protocol.backend.OkPacket;
import io.reactiverse.myclient.impl.util.BufferUtils;

import java.nio.charset.Charset;

public final class GenericPacketPayloadDecoder {
  private static final int SQL_STATE_MARKER_LENGTH = 1;
  private static final int SQL_STATE_LENGTH = 5;

  public static OkPacket decodeOkPacketBody(ByteBuf payloadBody, Charset charset) {
    long affectedRows = BufferUtils.readLengthEncodedInteger(payloadBody);
    long lastInsertId = BufferUtils.readLengthEncodedInteger(payloadBody);
    int serverStatusFlags = payloadBody.readUnsignedShortLE();
    int numberOfWarnings = payloadBody.readUnsignedShortLE();
    String info = readRestOfPacketString(payloadBody, charset);
    return new OkPacket(affectedRows, lastInsertId, serverStatusFlags, numberOfWarnings, info);
  }

  public static ErrPacket decodeErrPacketBody(ByteBuf payload, Charset charset) {
    int errorCode = payload.readUnsignedShortLE();
    String sqlStateMarker = BufferUtils.readFixedLengthString(payload, SQL_STATE_MARKER_LENGTH, charset);
    String sqlState = BufferUtils.readFixedLengthString(payload, SQL_STATE_LENGTH, charset);
    String errorMessage = readRestOfPacketString(payload, charset);
    return new ErrPacket(errorCode, sqlStateMarker, sqlState, errorMessage);
  }

  public static String readRestOfPacketString(ByteBuf payload, Charset charset) {
    return BufferUtils.readFixedLengthString(payload, payload.readableBytes(), charset);
  }
}
