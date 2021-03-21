package io.vertx.clickhouse.clickhousenative.impl.codec.columns;

import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseNativeColumnDescriptor;
import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseStreamDataSink;
import io.vertx.sqlclient.Tuple;

import java.util.List;

public abstract class ClickhouseColumnWriter {
  protected final List<Tuple> data;
  protected final ClickhouseNativeColumnDescriptor columnDescriptor;
  protected final int columnIndex;

  public ClickhouseColumnWriter(List<Tuple> data, ClickhouseNativeColumnDescriptor columnDescriptor, int columnIndex) {
    this.data = data;
    this.columnDescriptor = columnDescriptor;
    this.columnIndex = columnIndex;
  }

  public void serializeColumn(ClickhouseStreamDataSink sink, int fromRow, int toRow) {
    serializeStatePrefix(sink, fromRow, toRow);
    serializeData(sink, fromRow, toRow);
  }

  protected void serializeStatePrefix(ClickhouseStreamDataSink sink, int fromRow, int toRow) {
  }

  protected void serializeData(ClickhouseStreamDataSink sink, int fromRow, int toRow) {
    ensureCapacity(sink, fromRow, toRow);
    if (columnDescriptor.isNullable()) {
      serializeNullsMap(sink, fromRow, toRow);
    }
    serializeDataInternal(sink, fromRow, toRow);
  }

  protected void ensureCapacity(ClickhouseStreamDataSink sink, int fromRow, int toRow) {
    int nRows = toRow - fromRow;
    int requiredSize = 0;
    if (columnDescriptor.isNullable() && !columnDescriptor.isLowCardinality()) {
      requiredSize += nRows;
    }
    if (columnDescriptor.getElementSize() > 0) {
      requiredSize += nRows * columnDescriptor.getElementSize();
    }
    sink.ensureWritable(requiredSize);
  }

  protected void serializeNullsMap(ClickhouseStreamDataSink sink, int fromRow, int toRow) {
    for (int rowNo = fromRow; rowNo < toRow; ++rowNo) {
      Object val = data.get(rowNo).getValue(columnIndex);
      sink.writeBoolean(val == null);
    }
  }

  protected void serializeDataInternal(ClickhouseStreamDataSink sink, int fromRow, int toRow) {
    for (int rowNo = fromRow; rowNo < toRow; ++rowNo) {
      Object val = data.get(rowNo).getValue(columnIndex);
      if (val == null) {
        serializeDataNull(sink);
      } else {
        serializeDataElement(sink, val);
      }
    }
  }

  protected abstract void serializeDataElement(ClickhouseStreamDataSink sink, Object val);

  //TODO: maybe perform ByteBuf.writerIndex(writerIndex() + elemSize) (is allocated memory is zero-filled ?)
  protected abstract void serializeDataNull(ClickhouseStreamDataSink sink);
}
