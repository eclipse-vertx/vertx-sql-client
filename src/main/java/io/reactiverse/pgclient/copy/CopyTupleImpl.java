package io.reactiverse.pgclient.copy;

import io.reactiverse.pgclient.codec.DataType;
import io.reactiverse.pgclient.data.Json;
import io.reactiverse.pgclient.impl.ArrayTuple;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.Arrays;

class CopyTupleImpl extends ArrayTuple implements CopyTuple {
  
  private static class CopyData {

    private static boolean matchesArrayType(Class<?> target, Object value) {
      if (!target.isArray()) {
        return false;
      }
      Class<?> targetType = target.getComponentType();
      return targetType != Object.class &&
        targetType.isAssignableFrom(value.getClass().getComponentType());
    }

    private static boolean matchesType(Class<?> target, Object value) {
      return target != Object.class && target.isAssignableFrom(value.getClass());
    }

    private static boolean matches(Class<?> target, Object value, boolean isArray) {
      return isArray ? matchesArrayType(target, value) : matchesType(target, value);
    }

    // take our best guess at the type of this data
    static DataType guessDataType(Object obj) {
      if (obj != null) {
        return Arrays.stream(DataType.values())
          .filter(t -> matches(t.encodingType, obj, obj.getClass().isArray()))
          .findFirst().orElse(DataType.UNKNOWN);
      }
      return DataType.UNKNOWN;
    }

    private final Object value;
    private final DataType type;

    CopyData(Object value) {
      this(value, null);
    }

    CopyData(Object value, DataType type) {
      this.value = value;
      this.type = type;
    }

    public Object getValue() {
      return value;
    }

    public DataType getDataType() {
      if (type == null) {
        return guessDataType(value);
      }
      return type;
    }
  }
  
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
      add(new CopyData(value));
    }
    return this;
  }
  
  public CopyTuple addValue(Object value, DataType type) {
    add(new CopyData(value, type));
    return this;
  }

  @Override
  public DataType getDataType(int index) {
    Object o = super.get(index);
    if (o instanceof CopyData) {
      return ((CopyData) o).getDataType();
    }
    return CopyData.guessDataType(o);
  }

  //we add any overrides here to ensure we preserve the correct type when there
  //could be ambiguity for the user-desired Postgres type

  @Override
  public CopyTuple addShort(Short value) {
    super.add(new CopyData(value, DataType.INT2));
    return this;
  }

  @Override
  public CopyTuple addInteger(Integer value) {
    super.add(new CopyData(value, DataType.INT4));
    return this;
  }

  @Override
  public CopyTuple addLong(Long value) {
    super.add(new CopyData(value, DataType.INT8));
    return this;
  }

  @Override
  public CopyTuple addFloat(Float value) {
    super.add(new CopyData(value, DataType.FLOAT4));
    return this;
  }

  @Override
  public CopyTuple addDouble(Double value) {
    super.add(new CopyData(value, DataType.FLOAT8));
    return this;
  }

  @Override
  public CopyTuple addString(String value) {
    super.add(new CopyData(value, DataType.TEXT));
    return this;
  }

  @Override
  public CopyTuple addVarChar(String value) {
    super.add(new CopyData(value, DataType.VARCHAR));
    return this;
  }

  @Override
  public CopyTuple addJson(Json value) {
    super.add(new CopyData(value, DataType.JSON));
    return this;
  }

  @Override
  public CopyTuple addJsonb(Json value) {
    super.add(new CopyData(value, DataType.JSONB));
    return this;
  }

  @Override
  public CopyTuple addLocalDate(LocalDate value) {
    super.add(new CopyData(value, DataType.DATE));
    return this;
  }

  @Override
  public CopyTuple addLocalTime(LocalTime value) {
    super.add(new CopyData(value, DataType.TIME));
    return this;
  }

  @Override
  public CopyTuple addLocalDateTime(LocalDateTime value) {
    super.add(new CopyData(value, DataType.TIMESTAMP));
    return this;
  }

  @Override
  public CopyTuple addOffsetTime(OffsetTime value) {
    super.add(new CopyData(value, DataType.TIMETZ));
    return this;
  }

  @Override
  public CopyTuple addOffsetDateTime(OffsetDateTime value) {
    super.add(new CopyData(value, DataType.TIMESTAMPTZ));
    return this;
  }

  @Override
  public CopyTuple addIntegerArray(Integer[] value) {
    super.add(new CopyData(value, DataType.INT4_ARRAY));
    return this;
  }

  @Override
  public CopyTuple addShortArray(Short[] value) {
    super.add(new CopyData(value, DataType.INT2_ARRAY));
    return this;
  }

  @Override
  public CopyTuple addLongArray(Long[] value) {
    super.add(new CopyData(value, DataType.INT8_ARRAY));
    return this;
  }

  @Override
  public CopyTuple addFloatArray(Float[] value) {
    super.add(new CopyData(value, DataType.FLOAT4_ARRAY));
    return this;
  }

  @Override
  public CopyTuple addDoubleArray(Double[] value) {
    super.add(new CopyData(value, DataType.FLOAT8_ARRAY));
    return this;
  }

  @Override
  public CopyTuple addVarCharArray(String[] value) {
    super.add(new CopyData(value, DataType.VARCHAR_ARRAY));
    return this;
  }

  @Override
  public CopyTuple addStringArray(String[] value) {
    super.add(new CopyData(value, DataType.TEXT));
    return this;
  }

  @Override
  public CopyTuple addLocalDateArray(LocalDate[] value) {
    super.add(new CopyData(value, DataType.DATE_ARRAY));
    return this;
  }

  @Override
  public CopyTuple addLocalTimeArray(LocalTime[] value) {
    super.add(new CopyData(value, DataType.TIME_ARRAY));
    return this;
  }

  @Override
  public CopyTuple addOffsetTimeArray(OffsetTime[] value) {
    super.add(new CopyData(value, DataType.TIMETZ_ARRAY));
    return this;
  }

  @Override
  public CopyTuple addLocalDateTimeArray(LocalDateTime[] value) {
    super.add(new CopyData(value, DataType.TIMESTAMP_ARRAY));
    return this;
  }

  @Override
  public CopyTuple addOffsetDateTimeArray(OffsetDateTime[] value) {
    super.add(new CopyData(value, DataType.TIMESTAMPTZ_ARRAY));
    return this;
  }

  @Override
  public CopyTuple addJsonArray(Json[] value) {
    super.add(new CopyData(value, DataType.JSON_ARRAY));
    return this;
  }

  @Override
  public CopyTuple addJsonbArray(Json[] value) {
    super.add(new CopyData(value, DataType.JSONB_ARRAY));
    return this;
  }
}
