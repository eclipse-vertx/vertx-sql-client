package io.vertx.mysqlclient.impl.codec;

import io.netty.buffer.ByteBuf;
import io.vertx.mysqlclient.impl.MySQLRowImpl;
import io.vertx.mysqlclient.impl.codec.datatype.DataFormat;
import io.vertx.mysqlclient.impl.codec.datatype.DataType;
import io.vertx.mysqlclient.impl.codec.datatype.DataTypeCodec;
import io.vertx.mysqlclient.impl.util.BufferUtils;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.impl.RowDecoder;

import java.util.function.BiConsumer;
import java.util.stream.Collector;

class RowResultDecoder<C, R> implements RowDecoder {
  private static final int NULL = 0xFB;

  private final Collector<Row, C, R> collector;
  private final boolean singleton;
  private final BiConsumer<C, Row> accumulator;
  MySQLRowDesc rowDesc;

  private int size;
  private C container;
  private Row row;

  RowResultDecoder(Collector<Row, C, R> collector, boolean singleton, MySQLRowDesc rowDesc) {
    this.collector = collector;
    this.singleton = singleton;
    this.accumulator = collector.accumulator();
    this.rowDesc = rowDesc;
  }

  public int size() {
    return size;
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
          DataType dataType = rowDesc.columnDefinitions()[c].getType();
          int columnDefinitionFlags = rowDesc.columnDefinitions()[c].getFlags();
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
          DataType dataType = rowDesc.columnDefinitions()[c].getType();
          int columnDefinitionFlags = rowDesc.columnDefinitions()[c].getFlags();
          decoded = DataTypeCodec.decodeText(dataType, columnDefinitionFlags, in);
        }
        row.addValue(decoded);
      }
    }
    accumulator.accept(container, row);
    size++;
  }

  public R complete() {
    if (container == null) {
      container = collector.supplier().get();
    }
    return collector.finisher().apply(container);
  }

  public void reset() {
    container = null;
    size = 0;
  }
}

