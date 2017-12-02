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

import com.julienviet.pgclient.*;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public abstract class PgOperationsImpl implements PgOperations {

  protected abstract void schedule(CommandBase cmd);

  protected abstract void schedulePrepared(String sql, Function<AsyncResult<PreparedStatement>, CommandBase> supplier);

  @Override
  public PgQuery query(String sql) {
    return new SimplePgQueryImpl(sql, this::schedule);
  }

  @Override
  public void preparedQuery(String sql, List<Object> params, Handler<AsyncResult<PgResult<PgRow>>> handler) {
    schedulePrepared(sql, ar -> {
      if (ar.succeeded()) {
        return new ExtendedQueryCommand(ar.result(), params, new ExtendedQueryResultHandler(handler));
      } else {
        handler.handle(Future.failedFuture(ar.cause()));
        return null;
      }
    });
  }

  @Override
  public void update(String sql, Handler<AsyncResult<PgResult>> handler) {
    schedule(new UpdateCommand(sql, handler));
  }

  @Override
  public void preparedUpdate(String sql, List<Object> params, Handler<AsyncResult<PgResult<PgRow>>> handler) {
    schedulePrepared(sql, ar -> {
      if (ar.succeeded()) {
        return new PreparedUpdateCommand(
          ar.result(),
          Collections.singletonList(params),
          ar2 -> handler.handle(ar2.map(l -> new PgResultImpl(l.get(0).getUpdated()))));
      } else {
        handler.handle(Future.failedFuture(ar.cause()));
        return null;
      }
    });
  }

  @Override
  public void preparedBatchUpdate(String sql, List<List<Object>> list, Handler<AsyncResult<PgResult<PgRow>>> handler) {
    schedulePrepared(sql, ar -> {
      if (ar.succeeded()) {
        return new PreparedUpdateCommand(
          ar.result(),
          list,
          ar2 -> handler.handle(ar2.map(l -> new PgResultImpl(l.get(0).getUpdated()))));
      } else {
        handler.handle(Future.failedFuture(ar.cause()));
        return null;
      }
    });
  }
}
