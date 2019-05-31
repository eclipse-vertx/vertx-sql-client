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

package io.vertx.sqlclient.impl.command;

import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;
import io.vertx.sqlclient.impl.PreparedStatement;
import io.vertx.sqlclient.impl.QueryResultHandler;

import java.util.List;
import java.util.stream.Collector;

public class ExtendedBatchQueryCommand<T> extends ExtendedQueryCommandBase<T> {

  private final List<Tuple> params;

  public ExtendedBatchQueryCommand(PreparedStatement ps,
                            List<Tuple> params,
                            boolean singleton,
                            Collector<Row, ?, T> collector,
                            QueryResultHandler<T> resultHandler) {
    this(ps, params, 0, null, false, singleton, collector, resultHandler);
  }

  private ExtendedBatchQueryCommand(PreparedStatement ps,
                            List<Tuple> params,
                            int fetch,
                            String cursorId,
                            boolean suspended,
                            boolean singleton,
                            Collector<Row, ?, T> collector,
                            QueryResultHandler<T> resultHandler) {
    super(ps, fetch, cursorId, suspended, singleton, collector, resultHandler);
    this.params = params;
  }

  public List<Tuple> params() {
    return params;
  }

}
