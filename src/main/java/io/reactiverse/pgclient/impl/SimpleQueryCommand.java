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
import io.reactiverse.pgclient.impl.codec.decoder.RowDescription;
import io.reactiverse.pgclient.impl.codec.encoder.MessageEncoder;
import io.reactiverse.pgclient.impl.codec.encoder.Query;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

import java.util.stream.Collector;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */

class SimpleQueryCommand<T> extends QueryCommandBase<T> {

  private final String sql;
  private final boolean singleton;

  SimpleQueryCommand(String sql,
                     boolean singleton,
                     Collector<Row, ?, T> collector,
                     QueryResultHandler<T> resultHandler) {
    super(collector, resultHandler);
    this.sql = sql;
    this.singleton = singleton;
  }

  @Override
  String sql() {
    return sql;
  }

  @Override
  void exec(MessageEncoder out) {
    out.writeQuery(new Query(sql));
  }

  @Override
  public void handleRowDescription(RowDescription rowDescription) {
    decoder = new RowResultDecoder<>(collector, singleton, rowDescription);
  }

  public String getSql() {
    return sql;
  }
}
