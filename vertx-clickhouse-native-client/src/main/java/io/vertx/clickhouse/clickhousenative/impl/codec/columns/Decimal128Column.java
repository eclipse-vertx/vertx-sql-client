package io.vertx.clickhouse.clickhousenative.impl.codec.columns;

import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseNativeColumnDescriptor;
import io.vertx.sqlclient.Tuple;

import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;

public class Decimal128Column extends ClickhouseColumn {
  public static final int ELEMENT_SIZE = 16;
  public static final int MAX_PRECISION = 38;
  public static final MathContext MATH_CONTEXT = new MathContext(MAX_PRECISION, RoundingMode.HALF_EVEN);

  public Decimal128Column(ClickhouseNativeColumnDescriptor descriptor) {
    super(descriptor);
  }

  @Override
  public ClickhouseColumnReader reader(int nRows) {
    return new GenericDecimalColumnReader(nRows, descriptor, MATH_CONTEXT);
  }

  @Override
  public ClickhouseColumnWriter writer(List<Tuple> data, int columnIndex) {
    throw new IllegalArgumentException("not implemented");
  }
}
