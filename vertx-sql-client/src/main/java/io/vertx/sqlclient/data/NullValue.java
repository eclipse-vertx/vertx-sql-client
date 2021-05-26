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

  // We only cache instances for JDK types and some Vert.x types (JSON/Buffer)
  private static final Map<Class<?>, NullValue> cached = new HashMap<>();

  public static final NullValue Boolean = createAndCache(Boolean.class);
  public static final NullValue Short = createAndCache(Short.class);
  public static final NullValue Integer = createAndCache(Integer.class);
  public static final NullValue Long = createAndCache(Long.class);
  public static final NullValue Float = createAndCache(Float.class);
  public static final NullValue Double = createAndCache(Double.class);
  public static final NullValue String = createAndCache(String.class);
  public static final NullValue JsonObject = createAndCache(JsonObject.class);
  public static final NullValue JsonArray = createAndCache(JsonArray.class);
  public static final NullValue Temporal = createAndCache(Temporal.class);
  public static final NullValue LocalDate = createAndCache(LocalDate.class);
  public static final NullValue LocalTime = createAndCache(LocalTime.class);
  public static final NullValue LocalDateTime = createAndCache(LocalDateTime.class);
  public static final NullValue OffsetTime = createAndCache(OffsetTime.class);
  public static final NullValue OffsetDateTime = createAndCache(OffsetDateTime.class);
  public static final NullValue Buffer = createAndCache(Buffer.class);
  public static final NullValue UUID = createAndCache(UUID.class);
  public static final NullValue BigDecimal = createAndCache(BigDecimal.class);
  public static final NullValue ArrayOfBoolean = createAndCache(Boolean[].class);
  public static final NullValue ArrayOfShort = createAndCache(Short[].class);
  public static final NullValue ArrayOfInteger = createAndCache(Integer[].class);
  public static final NullValue ArrayOfLong = createAndCache(Long[].class);
  public static final NullValue ArrayOfFloat = createAndCache(Float[].class);
  public static final NullValue ArrayOfDouble = createAndCache(Double[].class);
  public static final NullValue ArrayOfString = createAndCache(String[].class);
  public static final NullValue ArrayOfJsonObject = createAndCache(JsonObject[].class);
  public static final NullValue ArrayOfJsonArray = createAndCache(JsonArray[].class);
  public static final NullValue ArrayOfTemporal = createAndCache(Temporal[].class);
  public static final NullValue ArrayOfLocalDate = createAndCache(LocalDate[].class);
  public static final NullValue ArrayOfLocalTime = createAndCache(LocalTime[].class);
  public static final NullValue ArrayOfLocalDateTime = createAndCache(LocalDateTime[].class);
  public static final NullValue ArrayOfOffsetTime = createAndCache(OffsetTime[].class);
  public static final NullValue ArrayOfOffsetDateTime = createAndCache(OffsetDateTime[].class);
  public static final NullValue ArrayOfBuffer = createAndCache(Buffer[].class);
  public static final NullValue ArrayOfUUID = createAndCache(UUID[].class);
  public static final NullValue ArrayOfBigDecimal = createAndCache(BigDecimal[].class);

  private static NullValue createAndCache(Class<?> type) {
    NullValue instance = new NullValue(type);
    cached.put(type, instance);
    return instance;
  }

  private final Class<?> type;

  private NullValue(Class<?> type) {
    this.type = type;
  }

  public static NullValue of(Class<?> type) {
    NullValue instance = cached.get(Objects.requireNonNull(type, "type cannot be null"));
    if (instance == null) {
      boolean array = type.isArray();
      Class<?> c = array ? type.getComponentType() : type;
      if (java.time.temporal.Temporal.class.isAssignableFrom(c)) {
        instance = array ? ArrayOfTemporal : Temporal;
      } else if (io.vertx.core.buffer.Buffer.class.isAssignableFrom(c)) {
        instance = array ? ArrayOfBuffer : Buffer;
      } else {
        instance = new NullValue(type);
      }
    }
    return instance;
  }

  /**
   * @return the type associated with a {@code NULL} column value
   */
  public Class<?> type() {
    return type;
  }
}
