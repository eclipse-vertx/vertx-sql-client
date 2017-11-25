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

package com.julienviet.pgclient.impl;

import com.julienviet.pgclient.PgOperations;
import com.julienviet.pgclient.PgQuery;
import com.julienviet.pgclient.ResultSet;
import com.julienviet.pgclient.UpdateResult;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

import java.util.Collections;
import java.util.List;

public abstract class PgOperationsImpl implements PgOperations {

  protected abstract void schedule(CommandBase cmd);

  @Override
  public PgQuery query(String sql) {
    return new SimplePgQueryImpl(sql, this::schedule);
  }

  @Override
  public void preparedQuery(String sql, List<Object> params, Handler<AsyncResult<ResultSet>> handler) {
    schedule(new ExtendedQueryCommand(sql, params, new PreparedQueryResultHandler(handler)));
  }

  @Override
  public void update(String sql, Handler<AsyncResult<UpdateResult>> handler) {
    schedule(new UpdateCommand(sql, handler));
  }

  @Override
  public void preparedUpdate(String sql, List<Object> params, Handler<AsyncResult<UpdateResult>> handler) {
    schedule(new PreparedUpdateCommand(
      sql,
      Collections.singletonList(params),
      ar -> handler.handle(ar.map(l -> l.get(0)))));
  }
}
