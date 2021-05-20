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

import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.impl.data.NullValueImpl;

import java.math.BigDecimal;
import java.time.*;
import java.time.temporal.Temporal;
import java.util.UUID;

/**
 * Instances of this class indicate the type associated with a {@code NULL} column value.
 * This is useful when infering database types from {@link io.vertx.sqlclient.Tuple} items.
 */
@VertxGen
public interface NullValue {

  NullValue Boolean = NullValueImpl.of(Boolean.class);
  NullValue Short = NullValueImpl.of(Short.class);
  NullValue Integer = NullValueImpl.of(Integer.class);
  NullValue Long = NullValueImpl.of(Long.class);
  NullValue Float = NullValueImpl.of(Float.class);
  NullValue Double = NullValueImpl.of(Double.class);
  NullValue String = NullValueImpl.of(String.class);
  NullValue JsonObject = NullValueImpl.of(JsonObject.class);
  NullValue JsonArray = NullValueImpl.of(JsonArray.class);
  NullValue Temporal = NullValueImpl.of(Temporal.class);
  NullValue LocalDate = NullValueImpl.of(LocalDate.class);
  NullValue LocalTime = NullValueImpl.of(LocalTime.class);
  NullValue LocalDateTime = NullValueImpl.of(LocalDateTime.class);
  NullValue OffsetTime = NullValueImpl.of(OffsetTime.class);
  NullValue OffsetDateTime = NullValueImpl.of(OffsetDateTime.class);
  NullValue Buffer = NullValueImpl.of(Buffer.class);
  NullValue UUID = NullValueImpl.of(UUID.class);
  NullValue BigDecimal = NullValueImpl.of(BigDecimal.class);
  NullValue ArrayOfBoolean = NullValueImpl.of(Boolean[].class);
  NullValue ArrayOfShort = NullValueImpl.of(Short[].class);
  NullValue ArrayOfInteger = NullValueImpl.of(Integer[].class);
  NullValue ArrayOfLong = NullValueImpl.of(Long[].class);
  NullValue ArrayOfFloat = NullValueImpl.of(Float[].class);
  NullValue ArrayOfDouble = NullValueImpl.of(Double[].class);
  NullValue ArrayOfString = NullValueImpl.of(String[].class);
  NullValue ArrayOfJsonObject = NullValueImpl.of(JsonObject[].class);
  NullValue ArrayOfJsonArray = NullValueImpl.of(JsonArray[].class);
  NullValue ArrayOfTemporal = NullValueImpl.of(Temporal[].class);
  NullValue ArrayOfLocalDate = NullValueImpl.of(LocalDate[].class);
  NullValue ArrayOfLocalTime = NullValueImpl.of(LocalTime[].class);
  NullValue ArrayOfLocalDateTime = NullValueImpl.of(LocalDateTime[].class);
  NullValue ArrayOfOffsetTime = NullValueImpl.of(OffsetTime[].class);
  NullValue ArrayOfOffsetDateTime = NullValueImpl.of(OffsetDateTime[].class);
  NullValue ArrayOfBuffer = NullValueImpl.of(Buffer[].class);
  NullValue ArrayOfUUID = NullValueImpl.of(UUID[].class);
  NullValue ArrayOfBigDecimal = NullValueImpl.of(BigDecimal[].class);

  /**
   * @return an instance of {@link NullValue} for the given {@code type}
   */
  static NullValue of(Object type) {
    return NullValueImpl.of(type);
  }

  /**
   * @return the type associated with a null value
   */
  @GenIgnore
  Class<?> type();

}
