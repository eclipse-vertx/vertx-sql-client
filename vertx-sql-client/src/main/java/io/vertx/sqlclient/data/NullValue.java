/*
 * Copyright (c) 2011-2021 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.sqlclient.data;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.core.VertxException;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.math.BigDecimal;
import java.time.*;
import java.time.temporal.Temporal;
import java.util.UUID;

/**
 * Instances of this class indicate the type associated with a {@code NULL} column value.
 * This is useful when infering database types from {@link io.vertx.sqlclient.Tuple} items.
 */
@DataObject
public class NullValue {

  public static NullValue Boolean = new NullValue(Boolean.class);
  public static NullValue Short = new NullValue(Short.class);
  public static NullValue Integer = new NullValue(Integer.class);
  public static NullValue Long = new NullValue(Long.class);
  public static NullValue Float = new NullValue(Float.class);
  public static NullValue Double = new NullValue(Double.class);
  public static NullValue String = new NullValue(String.class);
  public static NullValue JsonObject = new NullValue(JsonObject.class);
  public static NullValue JsonArray = new NullValue(JsonArray.class);
  public static NullValue Temporal = new NullValue(Temporal.class);
  public static NullValue LocalDate = new NullValue(LocalDate.class);
  public static NullValue LocalTime = new NullValue(LocalTime.class);
  public static NullValue LocalDateTime = new NullValue(LocalDateTime.class);
  public static NullValue OffsetTime = new NullValue(OffsetTime.class);
  public static NullValue OffsetDateTime = new NullValue(OffsetDateTime.class);
  public static NullValue Buffer = new NullValue(Buffer.class);
  public static NullValue UUID = new NullValue(UUID.class);
  public static NullValue BigDecimal = new NullValue(BigDecimal.class);
  public static NullValue ArrayOfBoolean = new NullValue(Boolean[].class);
  public static NullValue ArrayOfShort = new NullValue(Short[].class);
  public static NullValue ArrayOfInteger = new NullValue(Integer[].class);
  public static NullValue ArrayOfLong = new NullValue(Long[].class);
  public static NullValue ArrayOfFloat = new NullValue(Float[].class);
  public static NullValue ArrayOfDouble = new NullValue(Double[].class);
  public static NullValue ArrayOfString = new NullValue(String[].class);
  public static NullValue ArrayOfJsonObject = new NullValue(JsonObject[].class);
  public static NullValue ArrayOfJsonArray = new NullValue(JsonArray[].class);
  public static NullValue ArrayOfTemporal = new NullValue(Temporal[].class);
  public static NullValue ArrayOfLocalDate = new NullValue(LocalDate[].class);
  public static NullValue ArrayOfLocalTime = new NullValue(LocalTime[].class);
  public static NullValue ArrayOfLocalDateTime = new NullValue(LocalDateTime[].class);
  public static NullValue ArrayOfOffsetTime = new NullValue(OffsetTime[].class);
  public static NullValue ArrayOfOffsetDateTime = new NullValue(OffsetDateTime[].class);
  public static NullValue ArrayOfBuffer = new NullValue(Buffer[].class);
  public static NullValue ArrayOfUUID = new NullValue(UUID[].class);
  public static NullValue ArrayOfBigDecimal = new NullValue(BigDecimal[].class);

  private Class<?> type;

  public NullValue() {
  }

  public NullValue(NullValue other) {
    type = other.type;
  }

  public NullValue(JsonObject json) {
    String type = json.getString("type");
    if (type != null) {
      try {
        this.type = Class.forName(type);
      } catch (ClassNotFoundException e) {
        throw new VertxException("Cannot create NullValue instance", e);
      }
    }
  }

  @SuppressWarnings("rawtypes")
  @GenIgnore
  public NullValue(Class type) {
    this.type = type;
  }

  public String getType() {
    return type.getName();
  }

  public void setType(String type) {
    try {
      this.type = Class.forName(type);
    } catch (ClassNotFoundException e) {
      throw new VertxException("Cannot set type for NullValue", e);
    }
  }

  /**
   * @return the type associated with a null value
   */
  @GenIgnore
  public Class<?> type() {
    return type;
  }
}
