package io.vertx.clickhouse.clickhousenative.impl.codec.columns;

import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseNativeColumnDescriptor;
import io.vertx.sqlclient.Tuple;
import io.vertx.sqlclient.data.Numeric;

import java.math.BigInteger;
import java.util.List;

public class UInt64Column extends ClickhouseColumn {
  public static final Numeric[] EMPTY_NUMERIC_ARRAY = new Numeric[0];
  public static final Numeric UINT64_MIN = Numeric.create(BigInteger.ZERO);

  public UInt64Column(ClickhouseNativeColumnDescriptor descriptor) {
    super(descriptor);
  }

  @Override
  public ClickhouseColumnReader reader(int nRows) {
    return new UInt64ColumnReader(nRows, descriptor);
  }

  @Override
  public ClickhouseColumnWriter writer(List<Tuple> data, int columnIndex) {
    return new UInt64ColumnWriter(data, descriptor, columnIndex);
  }

  @Override
  public Object nullValue() {
    if (descriptor.isUnsigned()) {
      return UINT64_MIN;
    }
    return 0L;
  }

  @Override
  public Object[] emptyArray() {
    if (descriptor.isUnsigned()) {
      return EMPTY_NUMERIC_ARRAY;
    }
    return UInt32Column.EMPTY_LONG_ARRAY;
  }
}
