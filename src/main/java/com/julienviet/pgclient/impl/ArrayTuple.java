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

package com.julienviet.pgclient.impl;

import com.julienviet.pgclient.Tuple;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.temporal.Temporal;
import java.util.ArrayList;

public class ArrayTuple extends ArrayList<Object> implements Tuple {

  public static Tuple EMPTY = new ArrayTuple(0);

  @Override
  public Boolean getBoolean(int pos) {
    if(get(pos) instanceof Boolean)
      return (Boolean) get(pos);
    return null;
  }

  @Override
  public Object getValue(int pos) {
    return get(pos);
  }

  @Override
  public Integer getInteger(int pos) {
    if(get(pos) instanceof Integer)
      return (Integer) get(pos);
    return null;
  }

  @Override
  public Long getLong(int pos) {
    if(get(pos) instanceof Long)
      return (Long) get(pos);
    return null;
  }

  @Override
  public Float getFloat(int pos) {
    if(get(pos) instanceof Float)
      return (Float) get(pos);
    return null;
  }

  @Override
  public Double getDouble(int pos) {
    if(get(pos) instanceof Double)
      return (Double) get(pos);
    return null;
  }

  @Override
  public String getString(int pos) {
    if(get(pos) instanceof String)
      return (String) get(pos);
    return null;
  }

  @Override
  public JsonObject getJsonObject(int pos) {
    if(get(pos) instanceof JsonObject)
      return (JsonObject) get(pos);
    return null;
  }

  @Override
  public JsonArray getJsonArray(int pos) {
    if(get(pos) instanceof JsonArray)
      return (JsonArray) get(pos);
    return null;
  }

  @Override
  public Buffer getBuffer(int pos) {
    if(get(pos) instanceof Buffer)
      return (Buffer) get(pos);
    return null;
  }

  @Override
  public Temporal getTemporal(int pos) {
    if(get(pos) instanceof Temporal)
      return (Temporal) get(pos);
    return null;
  }

  @Override
  public LocalDate getLocalDate(int pos) {
    if(get(pos) instanceof LocalDate)
      return (LocalDate) get(pos);
    return null;
  }

  @Override
  public LocalTime getLocalTime(int pos) {
    if(get(pos) instanceof LocalTime)
      return (LocalTime) get(pos);
    return null;
  }

  @Override
  public LocalDateTime getLocalDateTime(int pos) {
    if(get(pos) instanceof LocalDateTime)
      return (LocalDateTime) get(pos);
    return null;
  }

  @Override
  public OffsetTime getOffsetTime(int pos) {
    if(get(pos) instanceof OffsetTime)
      return (OffsetTime) get(pos);
    return null;
  }

  @Override
  public OffsetDateTime getOffsetDateTime(int pos) {
    if(get(pos) instanceof OffsetDateTime)
      return (OffsetDateTime) get(pos);
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
      || value instanceof Character
      || value instanceof String
      || value instanceof JsonObject
      || value instanceof JsonArray
      || value instanceof Buffer
      || value instanceof LocalTime
      || value instanceof OffsetTime
      || value instanceof LocalDate
      || value instanceof LocalDateTime
      || value instanceof OffsetDateTime) {
      add(value);
    } else {
      add(null);
    }
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
  public Tuple addJsonObject(JsonObject value) {
    add(value);
    return this;
  }

  @Override
  public Tuple addJsonArray(JsonArray value) {
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

  public ArrayTuple(int len) {
    super(len);
  }


}
