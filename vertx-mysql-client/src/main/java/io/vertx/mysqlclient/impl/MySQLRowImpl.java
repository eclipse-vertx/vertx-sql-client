package io.vertx.mysqlclient.impl;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.data.Numeric;
import io.vertx.sqlclient.impl.ArrayTuple;
import io.vertx.sqlclient.impl.RowDesc;
import io.vertx.core.buffer.Buffer;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.temporal.Temporal;
import java.util.List;
import java.util.UUID;

public class MySQLRowImpl extends ArrayTuple implements Row {

  private final RowDesc rowDesc;

  public MySQLRowImpl(RowDesc rowDesc) {
    super(rowDesc.columnNames().size());
    this.rowDesc = rowDesc;
  }

  @Override
  public <T> T get(Class<T> type, int pos) {
    if (type == Boolean.class) {
      return type.cast(getBoolean(pos));
    } else if (type == Byte.class) {
      return type.cast(getByte(pos));
    } else if (type == Short.class) {
      return type.cast(getShort(pos));
    } else if (type == Integer.class) {
      return type.cast(getInteger(pos));
    } else if (type == Long.class) {
      return type.cast(getLong(pos));
    } else if (type == Float.class) {
      return type.cast(getFloat(pos));
    } else if (type == Double.class) {
      return type.cast(getDouble(pos));
    } else if (type == Numeric.class) {
      return type.cast(getNumeric(pos));
    } else if (type == String.class) {
      return type.cast(getString(pos));
    } else if (type == Buffer.class) {
      return type.cast(getBuffer(pos));
    } else if (type == LocalDate.class) {
      return type.cast(getLocalDate(pos));
    } else if (type == LocalDateTime.class) {
      return type.cast(getLocalDateTime(pos));
    } else if (type == Duration.class) {
      return type.cast(getDuration(pos));
    } else if (type == JsonObject.class) {
      return type.cast(getJsonObject(pos));
    } else if (type == JsonArray.class) {
      return type.cast(getJsonArray(pos));
    } else {
      throw new UnsupportedOperationException("Unsupported type " + type.getName());
    }
  }

  @Override
  public <T> T[] getValues(Class<T> type, int idx) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getColumnName(int pos) {
    List<String> columnNames = rowDesc.columnNames();
    return pos < 0 || columnNames.size() - 1 < pos ? null : columnNames.get(pos);
  }

  @Override
  public int getColumnIndex(String name) {
    if (name == null) {
      throw new NullPointerException();
    }
    return rowDesc.columnNames().indexOf(name);
  }

  @Override
  public Object getValue(String name) {
    int pos = rowDesc.columnIndex(name);
    return pos == -1 ? null : getValue(pos);
  }

  @Override
  public Boolean getBoolean(String name) {
    int pos = rowDesc.columnIndex(name);
    return pos == -1 ? null : getBoolean(pos);
  }

  @Override
  public Short getShort(String name) {
    int pos = rowDesc.columnIndex(name);
    return pos == -1 ? null : getShort(pos);
  }

  @Override
  public Integer getInteger(String name) {
    int pos = rowDesc.columnIndex(name);
    return pos == -1 ? null : getInteger(pos);
  }

  @Override
  public Long getLong(String name) {
    int pos = rowDesc.columnIndex(name);
    return pos == -1 ? null : getLong(pos);
  }

  @Override
  public Float getFloat(String name) {
    int pos = rowDesc.columnIndex(name);
    return pos == -1 ? null : getFloat(pos);
  }

  @Override
  public Double getDouble(String name) {
    int pos = rowDesc.columnIndex(name);
    return pos == -1 ? null : getDouble(pos);
  }

  public Numeric getNumeric(String name) {
    int pos = rowDesc.columnIndex(name);
    return pos == -1 ? null : getNumeric(pos);
  }

  @Override
  public String getString(String name) {
    int pos = rowDesc.columnIndex(name);
    return pos == -1 ? null : getString(pos);
  }

  @Override
  public Buffer getBuffer(String name) {
    int pos = rowDesc.columnIndex(name);
    return pos == -1 ? null : getBuffer(pos);
  }

  @Override
  public Temporal getTemporal(String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  public LocalDate getLocalDate(String name) {
    int pos = rowDesc.columnIndex(name);
    return pos == -1 ? null : getLocalDate(pos);
  }

  @Override
  public LocalTime getLocalTime(String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  public LocalDateTime getLocalDateTime(String name) {
    int pos = rowDesc.columnIndex(name);
    return pos == -1 ? null : getLocalDateTime(pos);
  }

  @Override
  public OffsetTime getOffsetTime(String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  public OffsetDateTime getOffsetDateTime(String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  public UUID getUUID(String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  public BigDecimal getBigDecimal(String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Integer[] getIntegerArray(String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Boolean[] getBooleanArray(String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Short[] getShortArray(String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Long[] getLongArray(String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Float[] getFloatArray(String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Double[] getDoubleArray(String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String[] getStringArray(String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  public LocalDate[] getLocalDateArray(String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  public LocalTime[] getLocalTimeArray(String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  public OffsetTime[] getOffsetTimeArray(String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  public LocalDateTime[] getLocalDateTimeArray(String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  public OffsetDateTime[] getOffsetDateTimeArray(String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Buffer[] getBufferArray(String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  public UUID[] getUUIDArray(String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Boolean getBoolean(int pos) {
    // in MySQL BOOLEAN type is mapped to TINYINT
    Object val = get(pos);
    if (val instanceof Boolean) {
      return (Boolean) val;
    } else if (val instanceof Number) {
      return ((Number) val).byteValue() != 0;
    }
    return null;
  }

  public Numeric getNumeric(int pos) {
    Object val = get(pos);
    if (val instanceof Numeric) {
      return (Numeric) val;
    } else if (val instanceof Number) {
      return Numeric.parse(val.toString());
    }
    return null;
  }

  private Byte getByte(int pos) {
    Object val = get(pos);
    if (val instanceof Byte) {
      return (Byte) val;
    } else if (val instanceof Number) {
      return ((Number) val).byteValue();
    }
    return null;
  }

  private Duration getDuration(int pos) {
    Object val = get(pos);
    if (val instanceof Duration) {
      return (Duration) val;
    }
    return null;
  }

  private JsonObject getJsonObject(int pos) {
    Object val = get(pos);
    if (val instanceof JsonObject) {
      return (JsonObject) val;
    }
    return null;
  }


  private JsonArray getJsonArray(int pos) {
    Object val = get(pos);
    if (val instanceof JsonArray) {
      return (JsonArray) val;
    }
    return null;
  }
}
