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

import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.TargetServerType;
import io.vertx.sqlclient.PoolOptions;

import java.util.List;

public class PgPoolOptions extends PoolOptions {

  private boolean pipelined;
  private TargetServerType targetServerType = TargetServerType.ANY;
  private List<PgConnectOptions> servers;

  public PgPoolOptions() {
  }

  public PgPoolOptions(PoolOptions other) {
    super(other);
    if (other instanceof PgPoolOptions) {
      PgPoolOptions pgOther = (PgPoolOptions) other;
      this.pipelined = pgOther.pipelined;
      this.targetServerType = pgOther.targetServerType;
      this.servers = pgOther.servers;
    }
  }

  public boolean isPipelined() {
    return pipelined;
  }

  public PgPoolOptions setPipelined(boolean pipelined) {
    this.pipelined = pipelined;
    return this;
  }

  public TargetServerType getTargetServerType() {
    return targetServerType;
  }

  public PgPoolOptions setTargetServerType(TargetServerType targetServerType) {
    this.targetServerType = targetServerType;
    return this;
  }

  public List<PgConnectOptions> getServers() {
    return servers;
  }

  public PgPoolOptions setServers(List<PgConnectOptions> servers) {
    this.servers = servers;
    return this;
  }
}
