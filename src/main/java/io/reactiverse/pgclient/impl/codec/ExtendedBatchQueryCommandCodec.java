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

import io.reactiverse.pgclient.Tuple;
import io.reactiverse.pgclient.impl.ExtendedBatchQueryCommand;
import io.reactiverse.pgclient.impl.ExtendedQueryCommand;
import io.reactiverse.pgclient.impl.PreparedStatement;

import java.util.List;

class ExtendedBatchQueryCommandCodec<R> extends ExtendedQueryCommandBaseCodec<R, ExtendedBatchQueryCommand<R>> {

  ExtendedBatchQueryCommandCodec(ExtendedBatchQueryCommand<R> cmd) {
    super(cmd);
  }

  @Override
  void encode(PgEncoder encoder) {
    if (cmd.isSuspended()) {
      encoder.writeExecute(cmd.portal(), cmd.fetch());
      encoder.writeSync();
    } else {
      PreparedStatement ps = cmd.preparedStatement();
      if (ps.bind().statement == 0) {
        encoder.writeParse(new Parse(ps.sql()));
      }
      if (cmd.params().isEmpty()) {
        // We set suspended to false as we won't get a command complete command back from Postgres
        this.result = false;
      } else {
        for (Tuple param : cmd.params()) {
          encoder.writeBind(ps.bind(), cmd.portal(), (List<Object>) param);
          encoder.writeExecute(cmd.portal(), cmd.fetch());
        }
      }
      encoder.writeSync();
    }
  }
}
