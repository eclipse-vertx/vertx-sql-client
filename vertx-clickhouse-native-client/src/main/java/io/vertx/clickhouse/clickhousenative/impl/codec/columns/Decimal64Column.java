package io.vertx.clickhouse.clickhousenative.impl.codec.columns;

import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseNativeColumnDescriptor;
import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseStreamDataSource;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;

public class Decimal64Column extends ClickhouseColumn {
  public static final int ELEMENT_SIZE = 8;
  public static final int MAX_PRECISION = 18;
  public static final MathContext MATH_CONTEXT = new MathContext(MAX_PRECISION, RoundingMode.HALF_EVEN);

  protected Decimal64Column(int nRows, ClickhouseNativeColumnDescriptor columnDescriptor) {
    super(nRows, columnDescriptor);
  }

  @Override
  protected Object readItems(ClickhouseStreamDataSource in) {
    if (in.readableBytes() >= ELEMENT_SIZE * nRows) {
      BigDecimal[] data = new BigDecimal[nRows];
      int scale = columnDescriptor.getScale();
      for (int i = 0; i < nRows; ++i) {
        long item = in.readLongLE();
        data[i] = new BigDecimal(BigInteger.valueOf(item), scale, MATH_CONTEXT);
      }
      return data;
    }
    return null;
  }
}
