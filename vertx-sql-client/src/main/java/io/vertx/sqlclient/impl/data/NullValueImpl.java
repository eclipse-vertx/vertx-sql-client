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

package io.vertx.sqlclient.impl.data;

import io.vertx.core.buffer.Buffer;
import io.vertx.sqlclient.data.NullValue;

import java.time.temporal.Temporal;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;

public class NullValueImpl implements NullValue {

  private static final Map<Class<?>, NullValueImpl> LOOKUP = Stream.of(
    Boolean.class,
    Short.class,
    Integer.class,
    Long.class,
    Float.class,
    Double.class,
    String.class,
    io.vertx.core.json.JsonObject.class,
    io.vertx.core.json.JsonArray.class,
    java.time.LocalDate.class,
    java.time.LocalTime.class,
    java.time.LocalDateTime.class,
    java.time.OffsetTime.class,
    java.time.OffsetDateTime.class,
    java.util.UUID.class,
    java.math.BigDecimal.class,
    Boolean[].class,
    Short[].class,
    Integer[].class,
    Long[].class,
    Float[].class,
    Double[].class,
    String[].class,
    io.vertx.core.json.JsonObject[].class,
    io.vertx.core.json.JsonArray[].class,
    java.time.LocalDate[].class,
    java.time.LocalTime[].class,
    java.time.LocalDateTime[].class,
    java.time.OffsetTime[].class,
    java.time.OffsetDateTime[].class,
    java.util.UUID[].class,
    java.math.BigDecimal[].class
  ).collect(toMap(Function.identity(), NullValueImpl::new));

  private static final NullValueImpl TEMPORAL_ARRAY = new NullValueImpl(Temporal[].class);
  private static final NullValueImpl TEMPORAL = new NullValueImpl(Temporal.class);
  private static final NullValueImpl BUFFER_ARRAY = new NullValueImpl(Buffer[].class);
  private static final NullValueImpl BUFFER = new NullValueImpl(Buffer.class);

  private final Class<?> type;

  private NullValueImpl(Class<?> type) {
    this.type = Objects.requireNonNull(type, "type cannot be null");
  }

  public static NullValue of(Object o) {
    Class<?> type = o instanceof Class ? (Class<?>) o : Objects.requireNonNull(o, "type cannot be null").getClass();
    NullValue instance = LOOKUP.get(type);
    if (instance == null) {
      boolean array = type.isArray();
      Class<?> c = array ? type.getComponentType() : type;
      if (java.time.temporal.Temporal.class.isAssignableFrom(c)) {
        return array ? TEMPORAL_ARRAY : TEMPORAL;
      }
      if (io.vertx.core.buffer.Buffer.class.isAssignableFrom(c)) {
        return array ? BUFFER_ARRAY : BUFFER;
      }
    }
    return new NullValueImpl(type);
  }

  @Override
  public Class<?> type() {
    return type;
  }
}
