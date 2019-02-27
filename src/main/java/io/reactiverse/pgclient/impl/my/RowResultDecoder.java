package io.reactiverse.pgclient.impl.my;

import io.netty.buffer.ByteBuf;
import io.reactiverse.pgclient.impl.my.codec.datatype.DataFormat;
import io.reactiverse.pgclient.impl.my.codec.datatype.DataType;
import io.reactiverse.pgclient.impl.my.codec.datatype.DataTypeCodec;
import io.reactiverse.pgclient.impl.my.codec.decoder.RowDecoder;
import io.reactiverse.pgclient.impl.my.util.BufferUtils;
import io.reactiverse.pgclient.Row;

import java.util.function.BiConsumer;
import java.util.stream.Collector;

public class RowResultDecoder<C, R> implements RowDecoder {
  private static final int NULL = 0xFB;

  private final Collector<Row, C, R> collector;
  private final boolean singleton;
  private final BiConsumer<C, Row> accumulator;

  private ColumnMetadata columnMetadata;
  private int size;
  private C container;
  private Row row;

  public RowResultDecoder(Collector<Row, C, R> collector, boolean singleton, ColumnMetadata columnMetadata) {
    this.collector = collector;
    this.singleton = singleton;
    this.accumulator = collector.accumulator();
    this.columnMetadata = columnMetadata;
  }

  public ColumnMetadata columnMetadata() {
    return columnMetadata;
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
        row = new MySQLRowImpl(columnMetadata);
      } else {
        row.clear();
      }
    } else {
      row = new MySQLRowImpl(columnMetadata);
    }
    Row row = new MySQLRowImpl(columnMetadata);
    if (columnMetadata.getDataFormat() == DataFormat.BINARY) {
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
          DataType dataType = columnMetadata.getColumnDefinitions()[c].getType();
          decoded = DataTypeCodec.decodeBinary(dataType, in);
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
          DataType dataType = columnMetadata.getColumnDefinitions()[c].getType();
          int length = (int) BufferUtils.readLengthEncodedInteger(in);
          ByteBuf data = in.slice(in.readerIndex(), length);
          in.skipBytes(length);
          decoded = DataTypeCodec.decodeText(dataType, data);
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

