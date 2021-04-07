package io.vertx.clickhouse.clickhousenative.impl.codec.columns;

import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseNativeColumnDescriptor;
import io.vertx.sqlclient.Tuple;

import java.math.BigInteger;
import java.util.List;

public class Int128Column extends ClickhouseColumn {
  public static final int ELEMENT_SIZE = 16;
  public static final BigInteger ZERO_VALUE = new BigInteger(new byte[ELEMENT_SIZE]);
  public static final BigInteger INT128_MIN_VALUE = new BigInteger("-170141183460469231731687303715884105728");
  public static final BigInteger INT128_MAX_VALUE = new BigInteger( "170141183460469231731687303715884105727");

  public Int128Column(ClickhouseNativeColumnDescriptor descriptor) {
    super(descriptor);
  }

  @Override
  public ClickhouseColumnReader reader(int nRows) {
    return new Int128ColumnReader(nRows, descriptor);
  }

  @Override
  public ClickhouseColumnWriter writer(List<Tuple> data, int columnIndex) {
    return new Int128ColumnWriter(data, descriptor, columnIndex);
  }

  public Object nullValue() {
    return ZERO_VALUE;
  }
}