package io.vertx.mysqlclient.impl.codec;

import io.netty.buffer.ByteBuf;
import io.vertx.core.buffer.Buffer;
import io.vertx.sqlclient.Tuple;
import io.vertx.sqlclient.impl.command.ExtendedQueryCommandBase;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static io.vertx.mysqlclient.impl.codec.Packets.*;

abstract class ExtendedQueryCommandBaseCodec<R, C extends ExtendedQueryCommandBase<R>> extends QueryCommandBaseCodec<R, C> {
  // TODO handle re-bound situations?
  // Flag if parameters must be re-bound
  protected final byte sendType = 1;

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
      OkPacket okPacket = decodeOkPacketPayload(payload, StandardCharsets.UTF_8);
      handleSingleResultsetDecodingCompleted(okPacket.serverStatusFlags(), (int) okPacket.affectedRows(), (int) okPacket.lastInsertId());
    } else if (firstByte == ERROR_PACKET_HEADER) {
      handleErrorPacketPayload(payload);
    } else {
      handleResultsetColumnCountPacketBody(payload);
    }
  }

  protected void sendStatementExecuteCommand(long statementId, ColumnDefinition[] paramsColumnDefinitions, byte sendType, Tuple params, byte cursorType) {
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
          DataTypeCodec.encodeBinary(parseDataTypeByEncodingValue(value), value, encoder.encodingCharset, packet);
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
    }
//    else if (value instanceof JsonObject || value instanceof JsonArray) {
////     note we don't need this in MySQL
//      // ProtocolBinary::MYSQL_TYPE_JSON
//      return DataType.JSON;
//    }
    else {
      /*
        ProtocolBinary::MYSQL_TYPE_STRING, ProtocolBinary::MYSQL_TYPE_VARCHAR, ProtocolBinary::MYSQL_TYPE_VAR_STRING,
        ProtocolBinary::MYSQL_TYPE_ENUM, ProtocolBinary::MYSQL_TYPE_SET, ProtocolBinary::MYSQL_TYPE_GEOMETRY,
        ProtocolBinary::MYSQL_TYPE_BIT, ProtocolBinary::MYSQL_TYPE_DECIMAL, ProtocolBinary::MYSQL_TYPE_NEWDECIMAL
       */
      return DataType.STRING;
    }
  }
}
