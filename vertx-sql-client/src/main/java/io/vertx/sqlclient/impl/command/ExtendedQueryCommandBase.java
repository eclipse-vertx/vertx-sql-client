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
import io.vertx.sqlclient.impl.PreparedStatement;
import io.vertx.sqlclient.impl.QueryResultHandler;

import java.util.stream.Collector;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public abstract class ExtendedQueryCommandBase<R> extends QueryCommandBase<R> {

  protected final PreparedStatement ps;
  protected final int fetch;
  protected final String cursorId;
  protected final boolean suspended;

  ExtendedQueryCommandBase(PreparedStatement ps,
                           int fetch,
                           String cursorId,
                           boolean suspended,
                           boolean autoCommit,
                           Collector<Row, ?, R> collector,
                           QueryResultHandler<R> resultHandler) {
    super(autoCommit, collector, resultHandler);
    this.ps = ps;
    this.fetch = fetch;
    this.cursorId = cursorId;
    this.suspended = suspended;
  }

  public PreparedStatement preparedStatement() {
    return ps;
  }

  public int fetch() {
    return fetch;
  }

  public String cursorId() {
    return cursorId;
  }

  public boolean isSuspended() {
    return suspended;
  }
  
  @Override
  public String sql() {
    return ps.sql();
  }
}
