/*
 * Copyright (C) 2018 Julien Viet
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
package io.reactiverse.pgclient.data;

import io.reactiverse.pgclient.impl.data.JsonImpl;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

@VertxGen
public interface Json {

  static Json create(Object value) {
    if (value == null) {
      return JsonImpl.NULL;
    } else if (value instanceof String || value instanceof Number || value instanceof JsonObject || value instanceof JsonArray || value instanceof Boolean) {
      return new JsonImpl(value);
    } else {
      throw new IllegalArgumentException("Invalid json value " + value + " with class " + value.getClass().getName());
    }
  }

  Object value();

}
