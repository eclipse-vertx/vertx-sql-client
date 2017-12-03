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

import com.julienviet.pgclient.impl.ArrayTuple;
import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.time.LocalDateTime;
import java.time.temporal.Temporal;
import java.util.Collections;

@VertxGen
public interface Tuple {

  static Tuple tuple() {
    return new ArrayTuple(10);
  }

  static Tuple of(Object val1) {
    ArrayTuple tuple = new ArrayTuple(1);
    tuple.add(val1);
    return tuple;
  }

  static Tuple of(Object val1, Object val2) {
    ArrayTuple tuple = new ArrayTuple(2);
    tuple.add(val1);
    tuple.add(val2);
    return tuple;
  }

  static Tuple of(Object val1, Object val2, Object val3) {
    ArrayTuple tuple = new ArrayTuple(3);
    tuple.add(val1);
    tuple.add(val2);
    tuple.add(val3);
    return tuple;
  }

  static Tuple of(Object val1, Object val2, Object val3, Object val4) {
    ArrayTuple tuple = new ArrayTuple(4);
    tuple.add(val1);
    tuple.add(val2);
    tuple.add(val3);
    tuple.add(val4);
    return tuple;
  }

  static Tuple of(Object val1, Object val2, Object val3, Object val4, Object val5) {
    ArrayTuple tuple = new ArrayTuple(5);
    tuple.add(val1);
    tuple.add(val2);
    tuple.add(val3);
    tuple.add(val4);
    tuple.add(val5);
    return tuple;
  }

  static Tuple of(Object val1, Object val2, Object val3, Object val4, Object val5, Object val6) {
    ArrayTuple tuple = new ArrayTuple(5);
    tuple.add(val1);
    tuple.add(val2);
    tuple.add(val3);
    tuple.add(val4);
    tuple.add(val5);
    tuple.add(val6);
    return tuple;
  }

  @GenIgnore
  static Tuple of(Object... vals) {
    ArrayTuple tuple = new ArrayTuple(vals.length);
    Collections.addAll(tuple, vals);
    return tuple;
  }

  Boolean getBoolean(int pos);

  Object getValue(int pos);

  Integer getInteger(int pos);

  Long getLong(int pos);

  Float getFloat(int pos);

  Double getDouble(int pos);

  String getString(int pos);

  JsonObject getJsonObject(int pos);

  JsonArray getJsonArray(int pos);

  @GenIgnore
  Temporal getTemporal(int pos);

  Buffer getBinary(int pos);

  @Fluent
  Tuple addBoolean(Boolean value);

  @Fluent
  Tuple addValue(Object value);

  @Fluent
  Tuple addInteger(Integer value);

  @Fluent
  Tuple addLong(Long value);

  @Fluent
  Tuple addFloat(Float value);

  @Fluent
  Tuple addDouble(Double value);

  @Fluent
  Tuple addString(String value);

  @Fluent
  Tuple addJsonObject(JsonObject value);

  @Fluent
  Tuple addJsonArray(JsonArray value);

  @Fluent
  Tuple addBinary(Buffer value);

  @GenIgnore
  Tuple addTimestamp(LocalDateTime value);

  @GenIgnore
  Tuple getTimestampTz(Temporal value);

  int size();

}
