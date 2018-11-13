package io.reactiverse.pgclient.copy;

import io.reactiverse.pgclient.codec.DataType;
import io.reactiverse.pgclient.data.Interval;
import io.reactiverse.pgclient.data.Json;
import io.reactiverse.pgclient.data.Numeric;
import io.reactiverse.pgclient.data.Point;
import io.reactiverse.pgclient.impl.ArrayTuple;
import io.vertx.codegen.annotations.Fluent;
import io.vertx.core.buffer.Buffer;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.UUID;

class CopyTupleImpl extends ArrayTuple implements CopyTuple {

  CopyTupleImpl(int len) {
    super(len);
  }

  @Override
  public Object get(int index) {
    Object o = super.get(index);
    if (o instanceof CopyData) {
      return ((CopyData) o).getValue();
    }
    return o;
  }

  @Override
  public CopyTuple addValue(Object value) {
    if (value instanceof CopyData) {
      add(value);
    } else {
      super.addValue(value);
    }
    return this;
  }

  @Override
  public DataType getDataType(int index) {
    Object o = super.get(index);
    if (o instanceof CopyData) {
      return ((CopyData) o).getDataType();
    }
    return CopyDataImpl.guessDataType(o);
  }

  @Override
  public CopyTuple addBoolean(Boolean value) {
    super.add(new CopyDataImpl(value, DataType.BOOL));
    return this;
  }

  @Override
  public CopyTuple addShort(Short value) {
    super.add(new CopyDataImpl(value, DataType.INT2));
    return this;
  }

  @Override
  public CopyTuple addInteger(Integer value) {
    super.add(new CopyDataImpl(value, DataType.INT4));
    return this;
  }

  @Override
  public CopyTuple addLong(Long value) {
    super.add(new CopyDataImpl(value, DataType.INT8));
    return this;
  }

  @Override
  public CopyTuple addFloat(Float value) {
    super.add(new CopyDataImpl(value, DataType.FLOAT4));
    return this;
  }

  @Override
  public CopyTuple addDouble(Double value) {
    super.add(new CopyDataImpl(value, DataType.FLOAT8));
    return this;
  }

  @Override
  public CopyTuple addVarChar(String value) {
    super.add(new CopyDataImpl(value, DataType.VARCHAR));
    return this;
  }

  @Override
  public CopyTuple addString(String value) {
    super.add(new CopyDataImpl(value, DataType.TEXT));
    return this;
  }

  @Override
  public CopyTuple addJson(Json value) {
    super.add(new CopyDataImpl(value, DataType.JSON));
    return this;
  }

  @Override
  public CopyTuple addJsonb(Json value) {
    super.add(new CopyDataImpl(value, DataType.JSONB));
    return this;
  }

  @Override
  public CopyTuple addBuffer(Buffer value) {
    super.add(new CopyDataImpl(value, DataType.BYTEA));
    return this;
  }

  @Override
  public CopyTuple addLocalDate(LocalDate value) {
    super.add(new CopyDataImpl(value, DataType.DATE));
    return this;
  }

  @Override
  public CopyTuple addLocalTime(LocalTime value) {
    super.add(new CopyDataImpl(value, DataType.TIME));
    return this;
  }

  @Override
  public CopyTuple addLocalDateTime(LocalDateTime value) {
    super.add(new CopyDataImpl(value, DataType.TIMESTAMP));
    return this;
  }

  @Override
  public CopyTuple addOffsetTime(OffsetTime value) {
    super.add(new CopyDataImpl(value, DataType.TIMETZ));
    return this;
  }

  @Override
  public CopyTuple addOffsetDateTime(OffsetDateTime value) {
    super.add(new CopyDataImpl(value, DataType.TIMESTAMPTZ));
    return this;
  }

  @Override
  public CopyTuple addUUID(UUID value) {
    super.add(new CopyDataImpl(value, DataType.UUID));
    return this;
  }

  @Override
  public CopyTuple addBigDecimal(BigDecimal value) {
    super.add(new CopyDataImpl(value, DataType.NUMERIC));
    return this;
  }

  @Override
  public CopyTuple addPoint(Point value) {
    super.add(new CopyDataImpl(value, DataType.POINT));
    return this;
  }

  @Override
  public CopyTuple addInterval(Interval value) {
    super.add(new CopyDataImpl(value, DataType.INTERVAL));
    return this;
  }

  @Override
  public CopyTuple addNumericArray(Numeric[] value) {
    super.add(new CopyDataImpl(value, DataType.NUMERIC_ARRAY));
    return this;
  }

  @Override
  public CopyTuple addPointArray(Point[] value) {
    super.add(new CopyDataImpl(value, DataType.POINT_ARRAY));
    return this;
  }

  @Override
  public CopyTuple addIntervalArray(Interval[] value) {
    super.add(new CopyDataImpl(value, DataType.INTERVAL_ARRAY));
    return this;
  }

  @Override
  public CopyTuple addIntegerArray(Integer[] value) {
    super.add(new CopyDataImpl(value, DataType.INT4_ARRAY));
    return this;
  }

  @Override
  public CopyTuple addBooleanArray(Boolean[] value) {
    super.add(new CopyDataImpl(value, DataType.BOOL_ARRAY));
    return this;
  }

  @Override
  public CopyTuple addShortArray(Short[] value) {
    super.add(new CopyDataImpl(value, DataType.INT2_ARRAY));
    return this;
  }

  @Override
  public CopyTuple addLongArray(Long[] value) {
    super.add(new CopyDataImpl(value, DataType.INT8_ARRAY));
    return this;
  }

  @Override
  public CopyTuple addFloatArray(Float[] value) {
    super.add(new CopyDataImpl(value, DataType.FLOAT4_ARRAY));
    return this;
  }

  @Override
  public CopyTuple addDoubleArray(Double[] value) {
    super.add(new CopyDataImpl(value, DataType.FLOAT8_ARRAY));
    return this;
  }

  @Override
  public CopyTuple addVarCharArray(String[] value) {
    super.add(new CopyDataImpl(value, DataType.VARCHAR_ARRAY));
    return this;
  }

  @Override
  public CopyTuple addStringArray(String[] value) {
    super.add(new CopyDataImpl(value, DataType.TEXT_ARRAY));
    return this;
  }

  @Override
  public CopyTuple addLocalDateArray(LocalDate[] value) {
    super.add(new CopyDataImpl(value, DataType.DATE_ARRAY));
    return this;
  }

  @Override
  public CopyTuple addLocalTimeArray(LocalTime[] value) {
    super.add(new CopyDataImpl(value, DataType.TIME_ARRAY));
    return this;
  }

  @Override
  public CopyTuple addOffsetTimeArray(OffsetTime[] value) {
    super.add(new CopyDataImpl(value, DataType.TIMETZ_ARRAY));
    return this;
  }

  @Override
  public CopyTuple addLocalDateTimeArray(LocalDateTime[] value) {
    super.add(new CopyDataImpl(value, DataType.TIMESTAMP_ARRAY));
    return this;
  }

  @Override
  public CopyTuple addOffsetDateTimeArray(OffsetDateTime[] value) {
    super.add(new CopyDataImpl(value, DataType.TIMESTAMPTZ_ARRAY));
    return this;
  }

  @Override
  public CopyTuple addBufferArray(Buffer[] value) {
    super.add(new CopyDataImpl(value, DataType.BYTEA_ARRAY));
    return this;
  }

  @Override
  public CopyTuple addUUIDArray(UUID[] value) {
    super.add(new CopyDataImpl(value, DataType.UUID_ARRAY));
    return this;
  }

  @Override
  public CopyTuple addJsonArray(Json[] value) {
    super.add(new CopyDataImpl(value, DataType.JSON_ARRAY));
    return this;
  }

  @Override
  public CopyTuple addJsonbArray(Json[] value) {
    super.add(new CopyDataImpl(value, DataType.JSONB_ARRAY));
    return this;
  }
}
