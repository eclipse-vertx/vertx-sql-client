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

package io.vertx.sqlclient.impl;

import io.vertx.core.Promise;
import io.vertx.sqlclient.impl.command.CommandBase;

public interface Connection {

  <R> void schedule(CommandBase<R> cmd, Promise<R> handler);

  void init(Holder holder);

  boolean isSsl();

  void close(Holder holder);

  int getProcessId();

  int getSecretKey();

  interface Holder {

    void handleNotification(int processId, String channel, String payload);

    void handleClosed();

    void handleException(Throwable err);

  }
}
