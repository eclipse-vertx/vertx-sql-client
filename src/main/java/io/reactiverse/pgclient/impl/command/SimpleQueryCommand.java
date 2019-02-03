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

package io.reactiverse.pgclient.impl.command;

import io.reactiverse.pgclient.Row;
import io.reactiverse.pgclient.impl.QueryResultHandler;

import java.util.stream.Collector;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */

public class SimpleQueryCommand<T> extends QueryCommandBase<T> {

  private final String sql;
  private final boolean singleton;

  public SimpleQueryCommand(String sql,
                     boolean singleton,
                     Collector<Row, ?, T> collector,
                     QueryResultHandler<T> resultHandler) {
    super(collector, resultHandler);
    this.sql = sql;
    this.singleton = singleton;
  }

  public boolean isSingleton() {
    return singleton;
  }

  @Override
  public String sql() {
    return sql;
  }

}
