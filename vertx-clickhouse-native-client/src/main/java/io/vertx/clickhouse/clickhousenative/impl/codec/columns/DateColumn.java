package io.vertx.clickhouse.clickhousenative.impl.codec.columns;

import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseNativeColumnDescriptor;
import io.vertx.sqlclient.Tuple;

import java.util.List;

public class DateColumn extends UInt16Column {
  public DateColumn(ClickhouseNativeColumnDescriptor descriptor) {
    super(descriptor);
  }

  @Override
  public ClickhouseColumnReader reader(int nRows) {
    return new DateColumnReader(nRows, descriptor);
  }

  @Override
  public ClickhouseColumnWriter writer(List<Tuple> data, int columnIndex) {
    return new DateColumnWriter(data, descriptor, columnIndex);
  }

  @Override
  public Object nullValue() {
    return DateColumnReader.MIN_VALUE;
  }

  @Override
  public Object[] emptyArray() {
    return DateColumnReader.EMPTY_ARRAY;
  }
}
