package io.vertx.mysqlclient.impl.codec;

import io.netty.buffer.ByteBuf;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.mysqlclient.impl.MySQLCollation;
import io.vertx.mysqlclient.impl.MySQLRowImpl;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.impl.RowDecoder;

import java.nio.charset.Charset;
import java.util.function.BiConsumer;
import java.util.stream.Collector;

class RowResultDecoder<C, R> implements RowDecoder {
  private static final int NULL = 0xFB;

  private final Collector<Row, C, R> collector;
  private final boolean singleton;
  private BiConsumer<C, Row> accumulator;
  MySQLRowDesc rowDesc;

  private int size;
  private C container;
  private Row row;
  private Throwable failure;
  private R result;

  RowResultDecoder(Collector<Row, C, R> collector, boolean singleton, MySQLRowDesc rowDesc) {
    this.collector = collector;
    this.singleton = singleton;
    this.rowDesc = rowDesc;

    try {
      this.container = collector.supplier().get();
    } catch (Exception e) {
      failure = e;
    }

  }

  public int size() {
    return size;
  }

  @Override
  public void decodeRow(int len, ByteBuf in) {
    if (failure != null) {
      return;
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
      // null_bitmap
      int nullBitmapLength = (len + 7 + 2) >>  3;
      int nullBitmapIdx = 1 + in.readerIndex();
      in.skipBytes(1 + nullBitmapLength);

      // values
      for (int c = 0; c < len; c++) {
        int val = c + 2;
        int bytePos = val >> 3;
        int bitPos = val & 7;
        byte mask = (byte) (1 << bitPos);
        byte nullByte = (byte) (in.getByte(nullBitmapIdx + bytePos) & mask);
        Object decoded = null;
        if (nullByte == 0) {
          // non-null
          ColumnDefinition columnDef = rowDesc.columnDefinitions()[c];
          DataType dataType = columnDef.type();
          int collationId = rowDesc.columnDefinitions()[c].characterSet();
          Charset charset = Charset.forName(MySQLCollation.valueOfId(collationId).mappedJavaCharsetName());
          int columnDefinitionFlags = columnDef.flags();
          decoded = DataTypeCodec.decodeBinary(dataType, charset, columnDefinitionFlags, in);
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
          int collationId = rowDesc.columnDefinitions()[c].characterSet();
          Charset charset = Charset.forName(MySQLCollation.valueOfId(collationId).mappedJavaCharsetName());
          decoded = DataTypeCodec.decodeText(dataType, charset, columnDefinitionFlags, in);
        }
        row.addValue(decoded);
      }
    }
    if (accumulator == null) {
      try {
        accumulator = collector.accumulator();
      } catch (Exception e) {
        failure = e;
        return;
      }
    }
    try {
      accumulator.accept(container, row);
    } catch (Exception e) {
      failure = e;
      return;
    }
    size++;
  }

  public R result() {
    return result;
  }

  public Throwable complete() {
    if (failure == null) {
      try {
        result = collector.finisher().apply(container);
      } catch (Exception e) {
        failure = e;
      }
    }
    return failure;
  }

  public void reset() {
    container = null;
    size = 0;
    failure = null;
    result = null;
  }
}

