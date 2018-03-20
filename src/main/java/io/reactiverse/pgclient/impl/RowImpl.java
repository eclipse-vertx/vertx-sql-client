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
import io.reactiverse.pgclient.Row;
import io.reactiverse.pgclient.impl.codec.decoder.message.RowDescription;
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
    int pos = desc.columnIndex(name);
    return pos == -1 ? null : getBoolean(pos);
  }

  @Override
  public Object getValue(String name) {
    int pos = desc.columnIndex(name);
    return pos == -1 ? null : getValue(pos);
  }

  @Override
  public Integer getInteger(String name) {
    int pos = desc.columnIndex(name);
    return pos == -1 ? null : getInteger(pos);
  }

  @Override
  public Long getLong(String name) {
    int pos = desc.columnIndex(name);
    return pos == -1 ? null : getLong(pos);
  }

  @Override
  public Float getFloat(String name) {
    int pos = desc.columnIndex(name);
    return pos == -1 ? null : getFloat(pos);
  }

  @Override
  public Double getDouble(String name) {
    int pos = desc.columnIndex(name);
    return pos == -1 ? null : getDouble(pos);
  }

  @Override
  public Character getCharacter(String name) {
    int pos = desc.columnIndex(name);
    return pos == -1 ? null : getCharacter(pos);
  }

  @Override
  public String getString(String name) {
    int pos = desc.columnIndex(name);
    return pos == -1 ? null : getString(pos);
  }

  @Override
  public Json getJson(String name) {
    int pos = desc.columnIndex(name);
    return pos == -1 ? null : getJson(pos);
  }

  @Override
  public JsonObject getJsonObject(String name) {
    int pos = desc.columnIndex(name);
    return pos == -1 ? null : getJsonObject(pos);
  }

  @Override
  public JsonArray getJsonArray(String name) {
    int pos = desc.columnIndex(name);
    return pos == -1 ? null : getJsonArray(pos);
  }

  @Override
  public Buffer getBuffer(String name) {
    int pos = desc.columnIndex(name);
    return pos == -1 ? null : getBuffer(pos);
  }

  @Override
  public Temporal getTemporal(String name) {
    int pos = desc.columnIndex(name);
    return pos == -1 ? null : getTemporal(pos);
  }

  @Override
  public LocalDate getLocalDate(String name) {
    int pos = desc.columnIndex(name);
    return pos == -1 ? null : getLocalDate(pos);
  }

  @Override
  public LocalTime getLocalTime(String name) {
    int pos = desc.columnIndex(name);
    return pos == -1 ? null : getLocalTime(pos);
  }

  @Override
  public LocalDateTime getLocalDateTime(String name) {
    int pos = desc.columnIndex(name);
    return pos == -1 ? null : getLocalDateTime(pos);
  }

  @Override
  public OffsetTime getOffsetTime(String name) {
    int pos = desc.columnIndex(name);
    return pos == -1 ? null : getOffsetTime(pos);
  }

  @Override
  public OffsetDateTime getOffsetDateTime(String name) {
    int pos = desc.columnIndex(name);
    return pos == -1 ? null : getOffsetDateTime(pos);
  }

  @Override
  public UUID getUUID(String name) {
    int pos = desc.columnIndex(name);
    return pos == -1 ? null : getUUID(pos);
  }

  @Override
  public BigDecimal getBigDecimal(String name) {
    int pos = desc.columnIndex(name);
    return pos == -1 ? null : getBigDecimal(pos);
  }

  @Override
  public Numeric getNumeric(String name) {
    int pos = desc.columnIndex(name);
    return pos == -1 ? null : getNumeric(pos);
  }
}
