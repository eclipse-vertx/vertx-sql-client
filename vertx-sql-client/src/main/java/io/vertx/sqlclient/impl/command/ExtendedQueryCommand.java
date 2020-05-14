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

import java.util.stream.Collector;

public class ExtendedQueryCommand<T> extends ExtendedQueryCommandBase<T> {

  private final Tuple params;

  public ExtendedQueryCommand(PreparedStatement ps,
                       Tuple params,
                       boolean autoCommit,
                       Collector<Row, ?, T> collector,
                       QueryResultHandler<T> resultHandler) {
    this(ps, params, 0, null, false, autoCommit, collector, resultHandler);
  }

  public ExtendedQueryCommand(PreparedStatement ps,
                       Tuple params,
                       int fetch,
                       String cursorId,
                       boolean suspended,
                       boolean autoCommit,
                       Collector<Row, ?, T> collector,
                       QueryResultHandler<T> resultHandler) {
    super(ps, fetch, cursorId, suspended, autoCommit, collector, resultHandler);
    this.params = params;
  }

  public Tuple params() {
    return params;
  }

}
