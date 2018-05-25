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

import io.reactiverse.pgclient.PgResult;
import io.reactiverse.pgclient.Row;

import java.util.stream.Collector;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
abstract class ExtendedQueryCommandBase<T> extends QueryCommandBase<T> {

  protected final PreparedStatement ps;
  protected final int fetch;
  protected final String portal;
  protected final boolean suspended;

  ExtendedQueryCommandBase(PreparedStatement ps,
                           int fetch,
                           String portal,
                           boolean suspended,
                           Collector<Row, ?, T> collector,
                           QueryResultHandler<T> handler) {
    super(collector, handler);
    this.ps = ps;
    this.fetch = fetch;
    this.portal = portal;
    this.suspended = suspended;
    this.decoder = new RowResultDecoder<>(collector, ps.rowDesc);
  }

  @Override
  String sql() {
    return ps.sql;
  }

  @Override
  public void handleParseComplete() {
    // Response to Parse
  }

  @Override
  public void handlePortalSuspended() {
    PgResult<T> result = decoder.complete(0);
    this.result = true;
    resultHandler.handleResult(result);
  }

  @Override
  public void handleBindComplete() {
    // Response to Bind
  }
}
