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
package io.reactiverse.pgclient.impl.data;

import io.reactiverse.pgclient.data.Json;

import java.util.Objects;

public class JsonImpl implements Json {

  public static final JsonImpl NULL = new JsonImpl(null);

  private final Object value;

  public JsonImpl(Object value) {
    this.value = value;
  }

  @Override
  public Object value() {
    return value;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(value);
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof Json && Objects.equals(value, ((Json) obj).value());
  }

  @Override
  public String toString() {
    return String.valueOf(value);
  }
}
