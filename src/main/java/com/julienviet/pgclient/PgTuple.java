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
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.time.temporal.Temporal;

@VertxGen
public interface PgTuple {

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

  int size();

}
