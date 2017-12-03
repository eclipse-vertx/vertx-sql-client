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
  public void preparedQuery(String sql, Tuple params, Handler<AsyncResult<PgResult<Tuple>>> handler) {
    schedulePrepared(sql, ar -> {
      if (ar.succeeded()) {
        return new ExtendedQueryCommand<>(ar.result(), params, new RowResultDecoder(), new ExtendedQueryResultHandler<>(handler));
      } else {
        handler.handle(Future.failedFuture(ar.cause()));
        return null;
      }
    });
  }

  @Override
  public void preparedBatch(String sql, List<Tuple> list, Handler<AsyncResult<PgResult<Tuple>>> handler) {
    schedulePrepared(sql, ar -> {
      if (ar.succeeded()) {
        return new ExtendedQueryCommand<>(
          ar.result(),
          list.iterator(),
          new RowResultDecoder()
          , new BatchQueryResultHandler(list.size(), (Handler) handler));
      } else {
        handler.handle(Future.failedFuture(ar.cause()));
        return null;
      }
    });
  }
}
