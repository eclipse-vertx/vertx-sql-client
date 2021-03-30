package io.vertx.clickhouse.clickhousenative.impl.codec.columns;

import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseNativeColumnDescriptor;
import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseStreamDataSink;
import io.vertx.sqlclient.Tuple;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

public class DateTimeColumnWriter extends ClickhouseColumnWriter {
  public final OffsetDateTime maxValue;

  public DateTimeColumnWriter(List<Tuple> data, ClickhouseNativeColumnDescriptor columnDescriptor, ZoneId zoneId, int columnIndex) {
    super(data, columnDescriptor, columnIndex);
    this.maxValue = Instant.ofEpochSecond(DateTimeColumnReader.MAX_EPOCH_SECOND).atZone(zoneId).toOffsetDateTime();
  }

  @Override
  protected void serializeDataElement(ClickhouseStreamDataSink sink, Object val) {
    OffsetDateTime dateTime = (OffsetDateTime) val;
    long epochSecond = dateTime.toInstant().getEpochSecond();
    if (epochSecond > DateTimeColumnReader.MAX_EPOCH_SECOND) {
      throw new IllegalArgumentException("value " + dateTime + " is too big; max epoch seconds: " + maxValue);
    }
    sink.writeIntLE((int) epochSecond);
  }

  @Override
  protected void serializeDataNull(ClickhouseStreamDataSink sink) {
    sink.writeIntLE(0);
  }

}
