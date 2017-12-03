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

import com.julienviet.pgclient.PgQuery;
import com.julienviet.pgclient.PgResult;
import com.julienviet.pgclient.PgTuple;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;

import java.util.*;

public class SimplePgQueryImpl implements PgQuery {

  private final Handler<CommandBase> execHandler;
  private final String sql;
  private SimpleQueryResultHandler<PgTuple> result;

  public SimplePgQueryImpl(String sql, Handler<CommandBase> execHandler) {
    this.execHandler = execHandler;
    this.sql = sql;
  }

  @Override
  public PgQuery fetch(int size) {
    return this;
  }

  @Override
  public boolean hasNext() {
    return result.hasNext();
  }

  @Override
  public void next(Handler<AsyncResult<PgResult<PgTuple>>> handler) {
    if (result.hasNext()) {
      handler.handle(Future.succeededFuture(result.next()));
    } else {
      handler.handle(Future.failedFuture(new NoSuchElementException()));
    }
  }

  @Override
  public void execute(Handler<AsyncResult<PgResult<PgTuple>>> handler) {
    if (result != null) {
      throw new IllegalStateException();
    }
    result = new SimpleQueryResultHandler<>(handler);
    execHandler.handle(new SimpleQueryCommand<>(sql, new RowResultDecoder(), result));
  }

  @Override
  public void close(Handler<AsyncResult<Void>> completionHandler) {

  }
}
