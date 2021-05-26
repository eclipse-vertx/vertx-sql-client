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

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.math.BigDecimal;
import java.time.*;
import java.time.temporal.Temporal;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Instances of this class indicate the type associated with a {@code NULL} column value.
 * This is useful when infering database types from {@link io.vertx.sqlclient.Tuple} items.
 */
public class NullValue {

  private static final Map<Class<?>, NullValue> cached = new HashMap<>();

  public static final NullValue Boolean = new NullValue(Boolean.class);
  public static final NullValue Short = new NullValue(Short.class);
  public static final NullValue Integer = new NullValue(Integer.class);
  public static final NullValue Long = new NullValue(Long.class);
  public static final NullValue Float = new NullValue(Float.class);
  public static final NullValue Double = new NullValue(Double.class);
  public static final NullValue String = new NullValue(String.class);
  public static final NullValue JsonObject = new NullValue(JsonObject.class);
  public static final NullValue JsonArray = new NullValue(JsonArray.class);
  public static final NullValue Temporal = new NullValue(Temporal.class, false);
  public static final NullValue LocalDate = new NullValue(LocalDate.class);
  public static final NullValue LocalTime = new NullValue(LocalTime.class);
  public static final NullValue LocalDateTime = new NullValue(LocalDateTime.class);
  public static final NullValue OffsetTime = new NullValue(OffsetTime.class);
  public static final NullValue OffsetDateTime = new NullValue(OffsetDateTime.class);
  public static final NullValue Buffer = new NullValue(Buffer.class, false);
  public static final NullValue UUID = new NullValue(UUID.class);
  public static final NullValue BigDecimal = new NullValue(BigDecimal.class);
  public static final NullValue ArrayOfBoolean = new NullValue(Boolean[].class);
  public static final NullValue ArrayOfShort = new NullValue(Short[].class);
  public static final NullValue ArrayOfInteger = new NullValue(Integer[].class);
  public static final NullValue ArrayOfLong = new NullValue(Long[].class);
  public static final NullValue ArrayOfFloat = new NullValue(Float[].class);
  public static final NullValue ArrayOfDouble = new NullValue(Double[].class);
  public static final NullValue ArrayOfString = new NullValue(String[].class);
  public static final NullValue ArrayOfJsonObject = new NullValue(JsonObject[].class);
  public static final NullValue ArrayOfJsonArray = new NullValue(JsonArray[].class);
  public static final NullValue ArrayOfTemporal = new NullValue(Temporal[].class, false);
  public static final NullValue ArrayOfLocalDate = new NullValue(LocalDate[].class);
  public static final NullValue ArrayOfLocalTime = new NullValue(LocalTime[].class);
  public static final NullValue ArrayOfLocalDateTime = new NullValue(LocalDateTime[].class);
  public static final NullValue ArrayOfOffsetTime = new NullValue(OffsetTime[].class);
  public static final NullValue ArrayOfOffsetDateTime = new NullValue(OffsetDateTime[].class);
  public static final NullValue ArrayOfBuffer = new NullValue(Buffer[].class, false);
  public static final NullValue ArrayOfUUID = new NullValue(UUID[].class);
  public static final NullValue ArrayOfBigDecimal = new NullValue(BigDecimal[].class);

  private final Class<?> type;

  private NullValue(Class<?> type) {
    this(type, true);
  }

  private NullValue(Class<?> type, boolean cache) {
    this.type = type;
    if (cache) {
      cached.put(type, this);
    }
  }

  public static NullValue of(Class<?> type) {
    NullValue instance = cached.get(Objects.requireNonNull(type, "type cannot be null"));
    if (instance == null) {
      boolean array = type.isArray();
      Class<?> c = array ? type.getComponentType() : type;
      if (java.time.temporal.Temporal.class.isAssignableFrom(c)) {
        return array ? ArrayOfTemporal : Temporal;
      }
      if (io.vertx.core.buffer.Buffer.class.isAssignableFrom(c)) {
        return array ? ArrayOfBuffer : Buffer;
      }
    }
    return new NullValue(type, false);

  }

  /**
   * @return the type associated with a {@code NULL} column value
   */
  public Class<?> type() {
    return type;
  }
}
