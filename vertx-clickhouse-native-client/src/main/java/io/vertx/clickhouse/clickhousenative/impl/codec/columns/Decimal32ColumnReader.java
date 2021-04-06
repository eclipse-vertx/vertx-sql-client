package io.vertx.clickhouse.clickhousenative.impl.codec.columns;

import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseNativeColumnDescriptor;
import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseStreamDataSource;
import io.vertx.sqlclient.data.Numeric;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;

public class Decimal32ColumnReader extends ClickhouseColumnReader {
  private final MathContext mc;

  protected Decimal32ColumnReader(int nRows, ClickhouseNativeColumnDescriptor columnDescriptor, MathContext mc) {
    super(nRows, columnDescriptor);
    this.mc = mc;
  }

  @Override
  protected Object readItems(ClickhouseStreamDataSource in) {
    if (in.readableBytes() >= Decimal32Column.ELEMENT_SIZE * nRows) {
      Numeric[] data = new Numeric[nRows];
      int scale = columnDescriptor.getScale();
      for (int i = 0; i < nRows; ++i) {
        if (nullsMap == null || !nullsMap.get(i)) {
          int item = in.readIntLE();
          data[i] = Numeric.create(new BigDecimal(BigInteger.valueOf(item), scale, mc));
        } else {
          in.skipBytes(Decimal32Column.ELEMENT_SIZE);
        }
      }
      return data;
    }
    return null;
  }
}
