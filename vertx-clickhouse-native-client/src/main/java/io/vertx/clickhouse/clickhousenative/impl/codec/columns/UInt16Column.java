package io.vertx.clickhouse.clickhousenative.impl.codec.columns;

import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseNativeColumnDescriptor;
import io.vertx.sqlclient.Tuple;

import java.util.List;

public class UInt16Column extends ClickhouseColumn {
  public static final Integer[] EMPTY_INT_ARRAY = new Integer[0];
  public static final Short[] EMPTY_SHORT_ARRAY = new Short[0];

  public UInt16Column(ClickhouseNativeColumnDescriptor descriptor) {
    super(descriptor);
  }

  @Override
  public ClickhouseColumnReader reader(int nRows) {
    return new UInt16ColumnReader(nRows, descriptor);
  }

  @Override
  public ClickhouseColumnWriter writer(List<Tuple> data, int columnIndex) {
    return new UInt16ColumnWriter(data, descriptor, columnIndex);
  }

  @Override
  public Object nullValue() {
    if (descriptor.isUnsigned()) {
      return (int) 0;
    } else {
      return (short) 0;
    }
  }

  @Override
  public Object[] emptyArray() {
    if (descriptor.isUnsigned()) {
      return EMPTY_INT_ARRAY;
    } else {
      return EMPTY_SHORT_ARRAY;
    }
  }
}
