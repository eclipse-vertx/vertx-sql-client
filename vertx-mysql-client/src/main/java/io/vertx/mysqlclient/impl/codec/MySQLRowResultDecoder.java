package io.vertx.mysqlclient.impl.codec;

import io.netty.buffer.ByteBuf;
import io.vertx.mysqlclient.impl.MySQLRowImpl;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.impl.RowResultDecoder;

import java.util.stream.Collector;

class MySQLRowResultDecoder<C, R> extends RowResultDecoder<C, R> {
  private static final int NULL = 0xFB;

  MySQLRowDesc rowDesc;

  MySQLRowResultDecoder(Collector<Row, C, R> collector, boolean singleton, MySQLRowDesc rowDesc) {
    super(collector, singleton);
    this.rowDesc = rowDesc;
  }

  @Override
  public void decodeRow(int len, ByteBuf in) {
    if (container == null) {
      container = collector.supplier().get();
    }
    if (singleton) {
      if (row == null) {
        row = new MySQLRowImpl(rowDesc);
      } else {
        row.clear();
      }
    } else {
      row = new MySQLRowImpl(rowDesc);
    }
    Row row = new MySQLRowImpl(rowDesc);
    if (rowDesc.dataFormat() == DataFormat.BINARY) {
      // BINARY row decoding
      // 0x00 packet header
      in.readByte();
      // null_bitmap
      int nullBitmapLength = (len + 7 + 2) / 8;
      byte[] nullBitmap = new byte[nullBitmapLength];
      in.readBytes(nullBitmap);

      // values
      final int offset = 2;
      for (int c = 0; c < len; c++) {
        Object decoded = null;

        int bytePos = (c + offset) / 8;
        int bitPos = (c + offset) % 8;
        byte nullByte = nullBitmap[bytePos];
        nullByte &= (1 << (7 - bitPos));

        if (nullByte == 0) {
          // non-null
          DataType dataType = rowDesc.columnDefinitions()[c].type();
          int columnDefinitionFlags = rowDesc.columnDefinitions()[c].flags();
          decoded = DataTypeCodec.decodeBinary(dataType,columnDefinitionFlags, in);
        }
        row.addValue(decoded);
      }
    } else {
      // TEXT row decoding
      for (int c = 0; c < len; c++) {
        Object decoded = null;
        if (in.getUnsignedByte(in.readerIndex()) == NULL) {
          in.skipBytes(1);
        } else {
          DataType dataType = rowDesc.columnDefinitions()[c].type();
          int columnDefinitionFlags = rowDesc.columnDefinitions()[c].flags();
          decoded = DataTypeCodec.decodeText(dataType, columnDefinitionFlags, in);
        }
        row.addValue(decoded);
      }
    }
    accumulator.accept(container, row);
    size++;
  }
}

