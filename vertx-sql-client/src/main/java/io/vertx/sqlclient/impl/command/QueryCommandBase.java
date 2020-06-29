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
import io.vertx.sqlclient.impl.QueryResultHandler;

import java.util.function.Function;
import java.util.stream.Collector;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */

public abstract class QueryCommandBase<T> extends CommandBase<Boolean> {

  public static final Collector<Row, Void, Void> NULL_COLLECTOR = Collector.of(() -> null, (v,row) -> {}, (v1, v2) -> null, Function.identity());

  private final QueryResultHandler<T> resultHandler;
  private final Collector<Row, ?, T> collector;
  private final boolean autoCommit;

  QueryCommandBase(boolean autoCommit, Collector<Row, ?, T> collector, QueryResultHandler<T> resultHandler) {
  this.autoCommit = autoCommit;
    this.resultHandler = resultHandler;
    this.collector = collector;
  }

  public QueryResultHandler<T> resultHandler() {
    return resultHandler;
  }

  public boolean autoCommit() {
  return autoCommit;
  }

  public Collector<Row, ?, T> collector() {
    return collector;
  }

  public abstract String sql();

}
