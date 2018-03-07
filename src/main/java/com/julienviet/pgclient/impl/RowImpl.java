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

import com.julienviet.pgclient.Row;
import com.julienviet.pgclient.impl.codec.decoder.message.RowDescription;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.temporal.Temporal;
import java.util.UUID;

public class RowImpl extends ArrayTuple implements Row {

  // Linked list
  RowImpl next;
  private final RowDescription desc;

  public RowImpl(RowDescription desc) {
    super(desc.columns().length);
    this.desc = desc;
  }

  @Override
  public Boolean getBoolean(String name) {
    return getBoolean(desc.columnIndex(name));
  }

  @Override
  public Object getValue(String name) {
    return getValue(desc.columnIndex(name));
  }

  @Override
  public Integer getInteger(String name) {
    return getInteger(desc.columnIndex(name));
  }

  @Override
  public Long getLong(String name) {
    return getLong(desc.columnIndex(name));
  }

  @Override
  public Float getFloat(String name) {
    return getFloat(desc.columnIndex(name));
  }

  @Override
  public Double getDouble(String name) {
    return getDouble(desc.columnIndex(name));
  }

  @Override
  public Character getCharacter(String name) {
    return getCharacter(desc.columnIndex(name));
  }

  @Override
  public String getString(String name) {
    return getString(desc.columnIndex(name));
  }

  @Override
  public JsonObject getJsonObject(String name) {
    return getJsonObject(desc.columnIndex(name));
  }

  @Override
  public JsonArray getJsonArray(String name) {
    return getJsonArray(desc.columnIndex(name));
  }

  @Override
  public Buffer getBuffer(String name) {
    return getBuffer(desc.columnIndex(name));
  }

  @Override
  public Temporal getTemporal(String name) {
    return getTemporal(desc.columnIndex(name));
  }

  @Override
  public LocalDate getLocalDate(String name) {
    return getLocalDate(desc.columnIndex(name));
  }

  @Override
  public LocalTime getLocalTime(String name) {
    return getLocalTime(desc.columnIndex(name));
  }

  @Override
  public LocalDateTime getLocalDateTime(String name) {
    return getLocalDateTime(desc.columnIndex(name));
  }

  @Override
  public OffsetTime getOffsetTime(String name) {
    return getOffsetTime(desc.columnIndex(name));
  }

  @Override
  public OffsetDateTime getOffsetDateTime(String name) {
    return getOffsetDateTime(desc.columnIndex(name));
  }

  @Override
  public UUID getUUID(String name) {
    return getUUID(desc.columnIndex(name));
  }

  @Override
  public BigDecimal getBigDecimal(String name) {
    return getBigDecimal(desc.columnIndex(name));
  }
}
