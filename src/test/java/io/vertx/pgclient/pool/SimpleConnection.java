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

package io.vertx.pgclient.pool;

import io.vertx.sqlclient.impl.command.CommandBase;
import io.vertx.sqlclient.impl.Connection;

class SimpleConnection implements Connection {

  Holder holder;
  int closed;

  @Override
  public void init(Holder holder) {
    this.holder = holder;
  }

  @Override
  public boolean isSsl() {
    return false;
  }

  @Override
  public void close(Holder holder) {
    closed++;
  }

  void close() {
    holder.handleClosed();
  }

  @Override
  public void schedule(CommandBase<?> cmd) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int getProcessId() {
    return -1;
  }

  @Override
  public int getSecretKey() {
    return -1;
  }
}
