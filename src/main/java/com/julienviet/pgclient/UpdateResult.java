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

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

@DataObject(generateConverter = true)
public class UpdateResult {

  private int updated;
  private JsonArray keys;

  /**
   * Default constructor
   */
  public UpdateResult() {
  }

  /**
   * Copy constructor
   *
   * @param other  the result to copy
   */
  public UpdateResult(UpdateResult other) {
    this.updated = other.updated;
    this.keys = other.getKeys();
  }

  /**
   * Constructor from JSON
   *
   * @param json  the json
   */
  @SuppressWarnings("unchecked")
  public UpdateResult(JsonObject json) {
    UpdateResultConverter.fromJson(json, this);
  }

  /**
   * Constructor
   *
   * @param updated  number of rows updated
   * @param keys  any generated keys
   */
  public UpdateResult(int updated, JsonArray keys) {
    this.updated = updated;
    this.keys = keys;
  }

  /**
   * Convert to JSON
   *
   * @return  the json
   */
  public JsonObject toJson() {
    JsonObject obj = new JsonObject();
    UpdateResultConverter.toJson(this, obj);
    return obj;
  }

  /**
   * Get the number of rows updated
   *
   * @return number of rows updated
   */
  public int getUpdated() {
    return updated;
  }

  public UpdateResult setUpdated(int updated) {
    this.updated = updated;
    return this;
  }

  /**
   * Get any generated keys
   *
   * @return generated keys
   */
  public JsonArray getKeys() {
    return keys;
  }

  public UpdateResult setKeys(JsonArray keys) {
    this.keys = keys;
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    UpdateResult that = (UpdateResult) o;

    if (updated != that.updated) return false;
    if (keys != null ? !keys.equals(that.keys) : that.keys != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = updated;
    result = 31 * result + (keys != null ? keys.hashCode() : 0);
    return result;
  }
}
