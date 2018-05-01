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
package io.reactiverse.pgclient;

import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.math.BigDecimal;
import java.time.*;
import java.time.temporal.Temporal;
import java.util.UUID;

@VertxGen
public interface Row extends Tuple {

  /**
   * Get a boolean value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  Boolean getBoolean(String name);

  /**
   * Get an object value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  Object getValue(String name);

  /**
   * Get a short value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  Short getShort(String name);

  /**
   * Get an integer value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  Integer getInteger(String name);

  /**
   * Get a long value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  Long getLong(String name);

  /**
   * Get a float value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  Float getFloat(String name);

  /**
   * Get a double value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  Double getDouble(String name);

  /**
   * Get a string value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  Character getCharacter(String name);

  /**
   * Get a string value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  String getString(String name);

  /**
   * Get a json value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  Json getJson(String name);

  /**
   * Get a json object value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  JsonObject getJsonObject(String name);

  /**
   * Get a json array value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  JsonArray getJsonArray(String name);

  /**
   * Get a buffer value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  Buffer getBuffer(String name);

  /**
   * Get a temporal value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  @GenIgnore
  Temporal getTemporal(String name);

  /**
   * Get {@link java.time.LocalDate} value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  @GenIgnore
  LocalDate getLocalDate(String name);

  /**
   * Get {@link java.time.LocalTime} value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  @GenIgnore
  LocalTime getLocalTime(String name);

  /**
   * Get {@link java.time.LocalDateTime} value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  @GenIgnore
  LocalDateTime getLocalDateTime(String name);

  /**
   * Get {@link java.time.OffsetTime} value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  @GenIgnore
  OffsetTime getOffsetTime(String name);

  /**
   * Get {@link java.time.OffsetDateTime} value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  @GenIgnore
  OffsetDateTime getOffsetDateTime(String name);

  /**
   * Get {@link java.util.UUID} value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  @GenIgnore
  UUID getUUID(String name);

  /**
   * Get {@link BigDecimal} value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  @GenIgnore
  BigDecimal getBigDecimal(String name);

  /**
   * Get {@link Numeric} value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  @GenIgnore
  Numeric getNumeric(String name);

  /**
   * Get {@link Integer} value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  @GenIgnore
  int[] getIntArray(String name);

  /**
   * Get {@link Boolean} value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  @GenIgnore
  boolean[] getBooleanArray(String name);

  /**
   * Get {@link Short} value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  @GenIgnore
  short[] getShortArray(String name);

  /**
   * Get {@link Long} value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  @GenIgnore
  long[] getLongArray(String name);

  /**
   * Get {@link Float} value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  @GenIgnore
  float[] getFloatArray(String name);

  /**
   * Get {@link Double} value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  @GenIgnore
  double[] getDoubleArray(String name);

  /**
   * Get {@link String} value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  @GenIgnore
  String[] getStringArray(String name);

  /**
   * Get {@link LocalDate} value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  @GenIgnore
  LocalDate[] getLocalDateArray(String name);

  /**
   * Get {@link LocalTime} value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  @GenIgnore
  LocalTime[] getLocalTimeArray(String name);

  /**
   * Get {@link OffsetTime} value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  @GenIgnore
  OffsetTime[] getOffsetTimeArray(String name);

  /**
   * Get {@link LocalDateTime} value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  @GenIgnore
  LocalDateTime[] getLocalDateTimeArray(String name);

  /**
   * Get {@link OffsetDateTime} value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  @GenIgnore
  OffsetDateTime[] getOffsetDateTimeArray(String name);

  /**
   * Get {@link Buffer} value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  @GenIgnore
  Buffer[] getBufferArray(String name);

  /**
   * Get {@link UUID} value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  @GenIgnore
  UUID[] getUUIDArray(String name);

  /**
   * Get {@link Numeric} value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  @GenIgnore
  Numeric[] getNumericArray(String name);

  /**
   * Get {@link Character} value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  @GenIgnore
  char[] getCharacterArray(String name);

}
