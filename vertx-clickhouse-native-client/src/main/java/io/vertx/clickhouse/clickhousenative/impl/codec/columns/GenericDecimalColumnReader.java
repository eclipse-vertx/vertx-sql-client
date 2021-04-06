package io.vertx.clickhouse.clickhousenative.impl.codec.columns;

import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseNativeColumnDescriptor;
import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseStreamDataSource;
import io.vertx.sqlclient.data.Numeric;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;

//TODO smagellan: maybe introduce separate universal reader/column for Decimal128 and Decimal256
public class GenericDecimalColumnReader extends ClickhouseColumnReader {
  private final MathContext mc;

  protected GenericDecimalColumnReader(int nRows, ClickhouseNativeColumnDescriptor columnDescriptor, MathContext mathContext) {
    super(nRows, columnDescriptor);
    this.mc = mathContext;
  }

  @Override
  protected Object readItems(ClickhouseStreamDataSource in) {
    int elementSize = columnDescriptor.getElementSize();
    if (in.readableBytes() >= elementSize * nRows) {
      Numeric[] data = new Numeric[nRows];
      int scale = columnDescriptor.getScale();
      byte[] readBuffer = new byte[elementSize];
      for (int i = 0; i < nRows; ++i) {
        if (nullsMap == null || !nullsMap.get(i)) {
          in.readBytes(readBuffer);
          BigInteger bi = new BigInteger(ColumnUtils.reverse(readBuffer));
          data[i] = Numeric.create(new BigDecimal(bi, scale, mc));
        } else {
          in.skipBytes(elementSize);
        }
      }
      return data;
    }
    return null;
  }
}
