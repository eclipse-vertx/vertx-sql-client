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

import io.reactiverse.sqlclient.impl.command.SimpleQueryCommand;

class SimpleQueryCodec<T> extends QueryCommandBaseCodec<T, SimpleQueryCommand<T>> {

  SimpleQueryCodec(SimpleQueryCommand<T> cmd) {
    super(cmd);
  }

  @Override
  void encode(PgEncoder encoder) {
    encoder.writeQuery(new Query(cmd.sql()));
  }

  @Override
  void handleRowDescription(PgRowDesc rowDescription) {
    decoder = new RowResultDecoder<>(cmd.collector(), cmd.isSingleton(), rowDescription);
  }
}
