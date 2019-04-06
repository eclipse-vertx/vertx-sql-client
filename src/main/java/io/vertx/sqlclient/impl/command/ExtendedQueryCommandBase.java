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
  protected final String portal;
  protected final boolean suspended;
  private final boolean singleton;
  private final ExecutionMode mode;

  ExtendedQueryCommandBase(PreparedStatement ps,
                           int fetch,
                           String portal,
                           boolean suspended,
                           boolean singleton,
                           ExecutionMode mode,
                           Collector<Row, ?, R> collector,
                           QueryResultHandler<R> resultHandler) {
    super(collector, resultHandler);
    this.ps = ps;
    this.fetch = fetch;
    this.portal = portal;
    this.suspended = suspended;
    this.singleton = singleton;
    this.mode = mode;
  }

  public PreparedStatement preparedStatement() {
    return ps;
  }

  public int fetch() {
    return fetch;
  }

  public String portal() {
    return portal;
  }

  public boolean isSuspended() {
    return suspended;
  }

  public boolean isSingleton() {
    return singleton;
  }

  public ExecutionMode mode() {
    return mode;
  }

  @Override
  public String sql() {
    return ps.sql();
  }

  public enum ExecutionMode {
    STATEMENT_EXECUTE, FETCH_WITH_OPEN_CURSOR, FETCH
  }
}
