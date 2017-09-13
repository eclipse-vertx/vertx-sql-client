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
import io.vertx.core.json.JsonObject;

/**
 * The options for configuring a connection pool.
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@DataObject(generateConverter = true)
public class PgPoolOptions {

  public static final int DEFAULT_MAX_POOL_SIZE = 4;
  public static final PoolingMode DEFAULT_MODE = PoolingMode.CONNECTION;

  private int maxSize = DEFAULT_MAX_POOL_SIZE;
  private PoolingMode mode = DEFAULT_MODE;

  public PgPoolOptions() {
  }

  public PgPoolOptions(JsonObject json) {
    PgPoolOptionsConverter.fromJson(json, this);
  }

  public PgPoolOptions(PgPoolOptions other) {
    maxSize = other.maxSize;
    mode = other.mode;
  }

  public int getMaxSize() {
    return maxSize;
  }

  public PgPoolOptions setMaxSize(int maxSize) {
    if (maxSize < 0) {
      throw new IllegalArgumentException("Max size cannot be negative");
    }
    this.maxSize = maxSize;
    return this;
  }

  public PoolingMode getMode() {
    return mode;
  }

  public PgPoolOptions setMode(PoolingMode mode) {
    this.mode = mode;
    return this;
  }
}
