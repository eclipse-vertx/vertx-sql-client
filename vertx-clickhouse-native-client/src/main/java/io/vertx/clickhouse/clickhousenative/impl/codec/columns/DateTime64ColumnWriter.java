package io.vertx.clickhouse.clickhousenative.impl.codec.columns;

import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseNativeColumnDescriptor;
import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseStreamDataSink;
import io.vertx.sqlclient.Tuple;

import java.math.BigInteger;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

public class DateTime64ColumnWriter extends ClickhouseColumnWriter {
  private final BigInteger invTickSize;
  private final long invTickLong;
  private final ZoneId zoneId;
  private final boolean saturateExtraNanos;

  public DateTime64ColumnWriter(List<Tuple> data, ClickhouseNativeColumnDescriptor descr, Integer precision, ZoneId zoneId, boolean saturateExtraNanos, int columnIndex) {
    super(data, descr, columnIndex);
    this.zoneId = zoneId;
    this.invTickSize = BigInteger.TEN.pow(precision);
    this.invTickLong = invTickSize.longValueExact();
    this.saturateExtraNanos = saturateExtraNanos;
  }

  @Override
  protected void serializeDataElement(ClickhouseStreamDataSink sink, Object val) {
    OffsetDateTime dt = (OffsetDateTime) val;
    //TODO: maybe check zone offset
    long tickCount = invTickSize.multiply(BigInteger.valueOf(dt.toEpochSecond())).longValue();
    long nanos = dt.getNano();
    if (nanos < invTickLong) {
      tickCount += nanos;
    } else {
      if (saturateExtraNanos) {
        tickCount += invTickLong - 1;
      } else {
        throw new IllegalArgumentException("nano adjustment " + nanos + " is too big, max " + invTickLong);
      }
    }
    sink.writeLongLE(tickCount);
  }

  @Override
  protected void serializeDataNull(ClickhouseStreamDataSink sink) {
    sink.writeZero(DateTime64Column.ELEMENT_SIZE);
  }
}
