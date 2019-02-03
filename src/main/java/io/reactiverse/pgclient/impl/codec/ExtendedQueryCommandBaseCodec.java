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
package io.reactiverse.pgclient.impl.codec;

import io.reactiverse.pgclient.impl.ExtendedQueryCommandBase;
import io.reactiverse.pgclient.impl.RowResultDecoder;
import io.reactiverse.pgclient.impl.SimpleQueryCommand;

abstract class ExtendedQueryCommandBaseCodec<R, C extends ExtendedQueryCommandBase<R>> extends QueryCommandBaseCodec<R, C> {

  ExtendedQueryCommandBaseCodec(C cmd) {
    super(cmd);
  }

  @Override
  void handleRowDescription(RowDescription rowDescription) {
    cmd.decoder = new RowResultDecoder<>(cmd.collector(), cmd.isSingleton(), rowDescription);
  }

  @Override
  void handleParseComplete() {
    // Response to Parse
  }

  @Override
  void handlePortalSuspended() {
    R result = cmd.decoder.complete();
    RowDescription desc = cmd.decoder.description();
    int size = cmd.decoder.size();
    cmd.decoder.reset();
    this.result = true;
    cmd.resultHandler().handleResult(0, size, desc, result);
  }

  @Override
  void handleBindComplete() {
    // Response to Bind
  }
}
