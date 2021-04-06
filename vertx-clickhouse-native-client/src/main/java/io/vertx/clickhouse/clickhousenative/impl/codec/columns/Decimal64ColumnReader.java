package io.vertx.clickhouse.clickhousenative.impl.codec.columns;

import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseNativeColumnDescriptor;
import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseStreamDataSource;
import io.vertx.sqlclient.data.Numeric;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;

public class Decimal64ColumnReader extends ClickhouseColumnReader {
  private final MathContext mc;

  protected Decimal64ColumnReader(int nRows, ClickhouseNativeColumnDescriptor columnDescriptor, MathContext mathContext) {
    super(nRows, columnDescriptor);
    this.mc = mathContext;
  }

  @Override
  protected Object readItems(ClickhouseStreamDataSource in) {
    if (in.readableBytes() >= Decimal64Column.ELEMENT_SIZE * nRows) {
      Numeric[] data = new Numeric[nRows];
      int scale = columnDescriptor.getScale();
      for (int i = 0; i < nRows; ++i) {
        if (nullsMap == null || !nullsMap.get(i)) {
          long item = in.readLongLE();
          data[i] = Numeric.create(new BigDecimal(BigInteger.valueOf(item), scale, mc));
        } else {
          in.skipBytes(Decimal64Column.ELEMENT_SIZE);
        }
      }
      return data;
    }
    return null;
  }
}
