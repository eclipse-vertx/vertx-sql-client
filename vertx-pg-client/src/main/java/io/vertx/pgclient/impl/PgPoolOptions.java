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
package io.vertx.pgclient.impl;

import io.vertx.sqlclient.PoolOptions;

public class PgPoolOptions extends PoolOptions {

  public PgPoolOptions(PoolOptions other) {
    super(other);
  }

  public PgPoolOptions() {
  }

  private boolean pipelined;

  public boolean isPipelined() {
    return pipelined;
  }

  public PgPoolOptions setPipelined(boolean pipelined) {
    this.pipelined = pipelined;
    return this;
  }
}
