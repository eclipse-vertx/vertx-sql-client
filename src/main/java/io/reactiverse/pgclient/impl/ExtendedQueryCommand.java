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

package io.reactiverse.pgclient.impl;

import io.reactiverse.pgclient.Row;
import io.reactiverse.pgclient.Tuple;
import io.reactiverse.pgclient.impl.codec.encoder.MessageEncoder;
import io.reactiverse.pgclient.impl.codec.encoder.Parse;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

import java.util.List;
import java.util.stream.Collector;

public class ExtendedQueryCommand<T> extends ExtendedQueryCommandBase<T> {

  private final Tuple params;

  ExtendedQueryCommand(PreparedStatement ps,
                       Tuple params,
                       boolean singleton,
                       Collector<Row, ?, T> collector,
                       QueryResultHandler<T> resultHandler,
                       Handler<AsyncResult<Boolean>> handler) {
    this(ps, params, 0, null, false, singleton, collector, resultHandler, handler);
  }

  ExtendedQueryCommand(PreparedStatement ps,
                       Tuple params,
                       int fetch,
                       String portal,
                       boolean suspended,
                       boolean singleton,
                       Collector<Row, ?, T> collector,
                       QueryResultHandler<T> resultHandler,
                       Handler<AsyncResult<Boolean>> handler) {
    super(ps, fetch, portal, suspended, singleton, collector, resultHandler, handler);
    this.params = params;
  }

  @Override
  void exec(MessageEncoder out) {
    if (suspended) {
      out.writeExecute(portal, fetch);
      out.writeSync();
    } else {
      if (ps.bind.statement == 0) {
        out.writeParse(new Parse(ps.sql));
      }
      out.writeBind(ps.bind, portal, (List<Object>) params);
      out.writeExecute(portal, fetch);
      out.writeSync();
    }
  }
}
