package io.vertx.clickhouse.clickhousenative.impl.codec.columns;

import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseNativeColumnDescriptor;
import io.vertx.sqlclient.Tuple;
import io.vertx.sqlclient.data.Numeric;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;

public class Decimal64Column extends ClickhouseColumn {
  public static final int ELEMENT_SIZE = 8;
  public static final int MAX_PRECISION = 18;
  public static final MathContext MATH_CONTEXT = new MathContext(MAX_PRECISION, RoundingMode.HALF_EVEN);
  private final Numeric zeroValue;

  public Decimal64Column(ClickhouseNativeColumnDescriptor descriptor) {
    super(descriptor);
    zeroValue = Numeric.create(new BigDecimal(BigInteger.ZERO, descriptor.getPrecision(), MATH_CONTEXT));
  }

  @Override
  public ClickhouseColumnReader reader(int nRows) {
    return new Decimal64ColumnReader(nRows, descriptor, MATH_CONTEXT);
  }

  @Override
  public ClickhouseColumnWriter writer(List<Tuple> data, int columnIndex) {
    return new Decimal64ColumnWriter(data, descriptor, columnIndex);
  }

  @Override
  public Object nullValue() {
    return zeroValue;
  }

  @Override
  public Object[] emptyArray() {
    return Decimal128Column.EMPTY_ARRAY;
  }
}
