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

import io.reactiverse.sqlclient.Row;
import io.reactiverse.pgclient.impl.PreparedStatement;
import io.reactiverse.pgclient.impl.QueryResultHandler;

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

  ExtendedQueryCommandBase(PreparedStatement ps,
                           int fetch,
                           String portal,
                           boolean suspended,
                           boolean singleton,
                           Collector<Row, ?, R> collector,
                           QueryResultHandler<R> resultHandler) {
    super(collector, resultHandler);
    this.ps = ps;
    this.fetch = fetch;
    this.portal = portal;
    this.suspended = suspended;
    this.singleton = singleton;
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

  @Override
  public String sql() {
    return ps.sql();
  }

}
