/*
 * Copyright (C) 2017 Julien Viet
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.reactiverse.pgclient.impl;

import io.reactiverse.pgclient.Json;
import io.reactiverse.pgclient.Numeric;
import io.reactiverse.pgclient.Tuple;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.math.BigDecimal;
import java.time.*;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.UUID;

public class ArrayTuple extends ArrayList<Object> implements Tuple {

  public static Tuple EMPTY = new ArrayTuple(0);

  @Override
  public Boolean getBoolean(int pos) {
    Object val = get(pos);
    if (val instanceof Json) {
      val = ((Json)val).value();
    }
    if (val instanceof Boolean) {
      return (Boolean) val;
    }
    return null;
  }

  @Override
  public Object getValue(int pos) {
    return get(pos);
  }

  @Override
  public Short getShort(int pos) {
    Object val = get(pos);
    if (val instanceof Json) {
      val = ((Json) val).value();
    }
    if (val instanceof Short) {
      return (Short) val;
    } else if (val instanceof Number) {
      return ((Number) val).shortValue();
    }
    return null;
  }

  @Override
  public Integer getInteger(int pos) {
    Object val = get(pos);
    if (val instanceof Json) {
      val = ((Json)val).value();
    }
    if (val instanceof Integer) {
      return (Integer) val;
    } else if (val instanceof Number) {
      return ((Number) val).intValue();
    }
    return null;
  }

  @Override
  public Long getLong(int pos) {
    Object val = get(pos);
    if (val instanceof Json) {
      val = ((Json)val).value();
    }
    if (val instanceof Long) {
      return (Long) val;
    } else if (val instanceof Number) {
      return ((Number) val).longValue();
    }
    return null;
  }

  @Override
  public Float getFloat(int pos) {
    Object val = get(pos);
    if (val instanceof Json) {
      val = ((Json)val).value();
    }
    if (val instanceof Float) {
      return (Float) val;
    } else if (val instanceof Number) {
      return ((Number) val).floatValue();
    }
    return null;
  }

  @Override
  public Double getDouble(int pos) {
    Object val = get(pos);
    if (val instanceof Json) {
      val = ((Json)val).value();
    }
    if (val instanceof Double) {
      return (Double) val;
    } else if (val instanceof Number) {
      return ((Number) val).doubleValue();
    }
    return null;
  }

  @Override
  public BigDecimal getBigDecimal(int pos) {
    Object val = get(pos);
    if (val instanceof Json) {
      val = ((Json)val).value();
    }
    if (val instanceof BigDecimal) {
      return (BigDecimal) val;
    } else if (val instanceof Number) {
      return new BigDecimal(val.toString());
    }
    return null;
  }

  @Override
  public Numeric getNumeric(int pos) {
    Object val = get(pos);
    if (val instanceof Json) {
      val = ((Json)val).value();
    }
    if (val instanceof Numeric) {
      return (Numeric) val;
    } else if (val instanceof Number) {
      return Numeric.parse(val.toString());
    }
    return null;
  }

  @Override
  public int[] getIntegerArray(int pos) {
    Object val = get(pos);
    if (val instanceof int[]) {
      return (int[]) val;
    } else {
      return null;
    }
  }

  @Override
  public boolean[] getBooleanArray(int pos) {
    Object val = get(pos);
    if (val instanceof boolean[]) {
      return (boolean[]) val;
    } else {
      return null;
    }
  }

  @Override
  public short[] getShortArray(int pos) {
    Object val = get(pos);
    if (val instanceof short[]) {
      return (short[]) val;
    } else {
      return null;
    }
  }

  @Override
  public long[] getLongArray(int pos) {
    Object val = get(pos);
    if (val instanceof long[]) {
      return (long[]) val;
    } else {
      return null;
    }
  }

  @Override
  public float[] getFloatArray(int pos) {
    Object val = get(pos);
    if (val instanceof float[]) {
      return (float[]) val;
    } else {
      return null;
    }
  }

  @Override
  public double[] getDoubleArray(int pos) {
    Object val = get(pos);
    if (val instanceof double[]) {
      return (double[]) val;
    } else {
      return null;
    }
  }

  @Override
  public String[] getStringArray(int pos) {
    Object val = get(pos);
    if (val instanceof String[]) {
      return (String[]) val;
    } else {
      return null;
    }
  }

  @Override
  public LocalDate[] getLocalDateArray(int pos) {
    Object val = get(pos);
    if (val instanceof LocalDate[]) {
      return (LocalDate[]) val;
    } else {
      return null;
    }
  }

  @Override
  public LocalTime[] getLocalTimeArray(int pos) {
    Object val = get(pos);
    if (val instanceof LocalTime[]) {
      return (LocalTime[]) val;
    } else {
      return null;
    }
  }

  @Override
  public OffsetTime[] getOffsetTimeArray(int pos) {
    Object val = get(pos);
    if (val instanceof OffsetTime[]) {
      return (OffsetTime[]) val;
    } else {
      return null;
    }
  }

  @Override
  public LocalDateTime[] getLocalDateTimeArray(int pos) {
    Object val = get(pos);
    if (val instanceof LocalDateTime[]) {
      return (LocalDateTime[]) val;
    } else {
      return null;
    }
  }

  @Override
  public OffsetDateTime[] getOffsetDateTimeArray(int pos) {
    Object val = get(pos);
    if (val instanceof OffsetDateTime[]) {
      return (OffsetDateTime[]) val;
    } else {
      return null;
    }
  }

  @Override
  public Buffer[] getBufferArray(int pos) {
    Object val = get(pos);
    if (val instanceof Buffer[]) {
      return (Buffer[]) val;
    } else {
      return null;
    }
  }

  @Override
  public UUID[] getUUIDArray(int pos) {
    Object val = get(pos);
    if (val instanceof UUID[]) {
      return (UUID[]) val;
    } else {
      return null;
    }
  }

  @Override
  public String getString(int pos) {
    Object val = get(pos);
    if (val instanceof Json) {
      val = ((Json)val).value();
    }
    if (val instanceof String) {
      return (String) val;
    }
    return null;
  }

  @Override
  public Character getCharacter(int pos) {
    Object val = get(pos);
    if (val instanceof Character) {
      return (Character) val;
    }
    return null;
  }

  @Override
  public Json getJson(int pos) {
    Object val = get(pos);
    if (val instanceof Json) {
      return (Json) val;
    }
    return null;
  }

  @Override
  public JsonObject getJsonObject(int pos) {
    Object val = get(pos);
    if (val instanceof Json) {
      val = ((Json) val).value();
      if (val instanceof JsonObject) {
        return (JsonObject) val;
      }
    }
    return null;
  }

  @Override
  public JsonArray getJsonArray(int pos) {
    Object val = get(pos);
    if (val instanceof Json) {
      val = ((Json) val).value();
      if (val instanceof JsonArray) {
        return (JsonArray) val;
      }
    }
    return null;
  }

  @Override
  public Buffer getBuffer(int pos) {
    Object val = get(pos);
    if (val instanceof Buffer) {
      return (Buffer) val;
    }
    return null;
  }

  @Override
  public Temporal getTemporal(int pos) {
    Object val = get(pos);
    if (val instanceof Temporal) {
      return (Temporal) val;
    }
    return null;
  }

  @Override
  public LocalDate getLocalDate(int pos) {
    Object val = get(pos);
    if (val instanceof LocalDate) {
      return (LocalDate) val;
    }
    return null;
  }

  @Override
  public LocalTime getLocalTime(int pos) {
    Object val = get(pos);
    if (val instanceof LocalTime) {
      return (LocalTime) val;
    }
    return null;
  }

  @Override
  public LocalDateTime getLocalDateTime(int pos) {
    Object val = get(pos);
    if (val instanceof LocalDateTime) {
      return (LocalDateTime) val;
    }
    return null;
  }

  @Override
  public OffsetTime getOffsetTime(int pos) {
    Object val = get(pos);
    if (val instanceof OffsetTime) {
      return (OffsetTime) val;
    }
    return null;
  }

  @Override
  public OffsetDateTime getOffsetDateTime(int pos) {
    Object val = get(pos);
    if (val instanceof OffsetDateTime) {
      return (OffsetDateTime) val;
    }
    return null;
  }

  @Override
  public UUID getUUID(int pos) {
    Object val = get(pos);
    if (val instanceof UUID) {
      return (UUID) val;
    }
    return null;
  }

  @Override
  public char[] getCharacterArray(int pos) {
    Object val = get(pos);
    if (val instanceof char[]) {
      return (char[]) val;
    }
    return null;
  }

  @Override
  public Tuple addBoolean(Boolean value) {
    add(value);
    return this;
  }

  @Override
  public Tuple addValue(Object value) {
    if(value instanceof Boolean
      || value instanceof Number
      || value instanceof String
      || value instanceof Character
      || value instanceof JsonObject
      || value instanceof JsonArray
      || value instanceof Buffer
      || value instanceof LocalTime
      || value instanceof OffsetTime
      || value instanceof LocalDate
      || value instanceof LocalDateTime
      || value instanceof OffsetDateTime
      || value instanceof UUID
      || value instanceof boolean[]
      || value instanceof short[]
      || value instanceof int[]
      || value instanceof long[]
      || value instanceof float[]
      || value instanceof double[]
      || value instanceof char[]
      || value instanceof String[]
      || value instanceof LocalDate[]
      || value instanceof LocalTime[]
      || value instanceof OffsetTime[]
      || value instanceof LocalDateTime[]
      || value instanceof OffsetDateTime[]
      || value instanceof UUID[]
      || value instanceof Buffer[]) {
      add(value);
    } else {
      add(null);
    }
    return this;
  }

  @Override
  public Tuple addShort(Short value) {
    add(value);
    return this;
  }

  @Override
  public Tuple addInteger(Integer value) {
    add(value);
    return this;
  }

  @Override
  public Tuple addLong(Long value) {
    add(value);
    return this;
  }

  @Override
  public Tuple addFloat(Float value) {
    add(value);
    return this;
  }

  @Override
  public Tuple addDouble(Double value) {
    add(value);
    return this;
  }

  @Override
  public Tuple addString(String value) {
    add(value);
    return this;
  }

  @Override
  public Tuple addCharacter(Character value) {
    add(value);
    return this;
  }

  @Override
  public Tuple addJsonObject(JsonObject value) {
    add(Json.create(value));
    return this;
  }

  @Override
  public Tuple addJsonArray(JsonArray value) {
    add(Json.create(value));
    return this;
  }

  @Override
  public Tuple addJson(Json value) {
    add(value);
    return this;
  }

  @Override
  public Tuple addBuffer(Buffer value) {
    add(value);
    return this;
  }

  @Override
  public Tuple addTemporal(Temporal value) {
    add(value);
    return this;
  }

  @Override
  public Tuple addLocalDate(LocalDate value) {
    add(value);
    return this;
  }

  @Override
  public Tuple addLocalTime(LocalTime value) {
    add(value);
    return this;
  }

  @Override
  public Tuple addLocalDateTime(LocalDateTime value) {
    add(value);
    return this;
  }

  @Override
  public Tuple addOffsetTime(OffsetTime value) {
    add(value);
    return this;
  }

  @Override
  public Tuple addOffsetDateTime(OffsetDateTime value) {
    add(value);
    return this;
  }

  @Override
  public Tuple addUUID(UUID value) {
    add(value);
    return this;
  }

  @Override
  public Tuple addBigDecimal(BigDecimal value) {
    add(value);
    return this;
  }

  @Override
  public Tuple addNumeric(Numeric value) {
    add(value);
    return this;
  }

  @Override
  public Tuple addIntArray(int[] value) {
    add(value);
    return this;
  }

  @Override
  public Tuple addBooleanArray(boolean[] value) {
    add(value);
    return this;
  }

  @Override
  public Tuple addShortArray(short[] value) {
    add(value);
    return this;
  }

  @Override
  public Tuple addLongArray(long[] value) {
    add(value);
    return this;
  }

  @Override
  public Tuple addFloatArray(float[] value) {
    add(value);
    return this;
  }

  @Override
  public Tuple addDoubleArray(double[] value) {
    add(value);
    return this;
  }

  @Override
  public Tuple addStringArray(String[] value) {
    add(value);
    return this;
  }

  @Override
  public Tuple addLocalDateArray(LocalDate[] value) {
    add(value);
    return this;
  }

  @Override
  public Tuple addLocalTimeArray(LocalTime[] value) {
    add(value);
    return this;
  }

  @Override
  public Tuple addOffsetTimeArray(OffsetTime[] value) {
    add(value);
    return this;
  }

  @Override
  public Tuple addLocalDateTimeArray(LocalDateTime[] value) {
    add(value);
    return this;
  }

  @Override
  public Tuple addOffsetDateTimeArray(OffsetDateTime[] value) {
    add(value);
    return this;
  }

  @Override
  public Tuple addBufferArray(Buffer[] value) {
    add(value);
    return this;
  }

  @Override
  public Tuple addUUIDArray(UUID[] value) {
    add(value);
    return this;
  }

  @Override
  public Tuple addCharacterArray(char[] value) {
    add(value);
    return this;
  }

  public ArrayTuple(int len) {
    super(len);
  }


}
