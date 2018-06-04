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

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collector;

public class ExtendedBatchQueryCommand<T> extends ExtendedQueryCommandBase<T> {

  private final Iterator<Tuple> paramsIterator;

  ExtendedBatchQueryCommand(PreparedStatement ps,
                            Iterator<Tuple> paramsIterator,
                            boolean singleton,
                            Collector<Row, ?, T> collector,
                            QueryResultHandler<T> resultHandler,
                            Handler<AsyncResult<Boolean>> handler) {
    this(ps, paramsIterator, 0, null, false, singleton, collector, resultHandler, handler);
  }

  ExtendedBatchQueryCommand(PreparedStatement ps,
                            Iterator<Tuple> paramsIterator,
                            int fetch,
                            String portal,
                            boolean suspended,
                            boolean singleton,
                            Collector<Row, ?, T> collector,
                            QueryResultHandler<T> resultHandler,
                            Handler<AsyncResult<Boolean>> handler) {
    super(ps, fetch, portal, suspended, singleton, collector, resultHandler, handler);
    this.paramsIterator = paramsIterator;
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
      while (paramsIterator.hasNext()) {
        List<Object> params = (List<Object>) paramsIterator.next();
        out.writeBind(ps.bind, portal, params);
        out.writeExecute(portal, fetch);
      }
      out.writeSync();
    }
  }
}
