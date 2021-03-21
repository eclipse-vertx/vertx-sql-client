package io.vertx.clickhouse.clickhousenative.impl.codec.columns;

import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseNativeColumnDescriptor;
import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseStreamDataSource;
import io.vertx.sqlclient.data.Numeric;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;

public class Decimal64ColumnReader extends ClickhouseColumnReader {
  public static final int ELEMENT_SIZE = 8;
  public static final int MAX_PRECISION = 18;
  public static final MathContext MATH_CONTEXT = new MathContext(MAX_PRECISION, RoundingMode.HALF_EVEN);

  protected Decimal64ColumnReader(int nRows, ClickhouseNativeColumnDescriptor columnDescriptor) {
    super(nRows, columnDescriptor);
  }

  @Override
  protected Object readItems(ClickhouseStreamDataSource in) {
    if (in.readableBytes() >= ELEMENT_SIZE * nRows) {
      Numeric[] data = new Numeric[nRows];
      int scale = columnDescriptor.getScale();
      for (int i = 0; i < nRows; ++i) {
        if (nullsMap == null || !nullsMap.get(i)) {
          long item = in.readLongLE();
          data[i] = Numeric.create(new BigDecimal(BigInteger.valueOf(item), scale, MATH_CONTEXT));
        } else {
          in.skipBytes(ELEMENT_SIZE);
        }
      }
      return data;
    }
    return null;
  }
}
