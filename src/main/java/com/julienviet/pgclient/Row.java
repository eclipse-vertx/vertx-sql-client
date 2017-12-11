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
package com.julienviet.pgclient;

import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.time.temporal.Temporal;

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
  String getString(String name);

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
   * Get a temporal value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  @GenIgnore
  Temporal getTemporal(String name);

}
