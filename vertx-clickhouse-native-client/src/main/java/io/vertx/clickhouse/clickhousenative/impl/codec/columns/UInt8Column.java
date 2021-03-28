package io.vertx.clickhouse.clickhousenative.impl.codec.columns;

import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseNativeColumnDescriptor;
import io.vertx.sqlclient.Tuple;

import java.util.List;

public class UInt8Column extends ClickhouseColumn {
  public UInt8Column(ClickhouseNativeColumnDescriptor descriptor) {
    super(descriptor);
  }

  @Override
  public ClickhouseColumnReader reader(int nRows) {
    return new UInt8ColumnReader(nRows, descriptor);
  }

  @Override
  public ClickhouseColumnWriter writer(List<Tuple> data, int columnIndex) {
    return new UInt8ColumnWriter(data, descriptor, columnIndex);
  }

  @Override
  public Object nullValue() {
    if  (descriptor.isUnsigned()) {
      return (short) 0;
    } else {
      return (byte) 0;
    }
  }
}
