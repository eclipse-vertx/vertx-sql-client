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
import com.julienviet.pgclient.Row;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;

public class SimplePgQueryImpl implements PgQuery {

  private final Handler<CommandBase<?>> execHandler;
  private final String sql;
  private SimpleQueryResultHandler<Row> result;

  public SimplePgQueryImpl(String sql, Handler<CommandBase<?>> execHandler) {
    this.execHandler = execHandler;
    this.sql = sql;
  }

  @Override
  public PgQuery fetch(int size) {
    return this;
  }

  @Override
  public boolean hasMore() {
    return result.hasNext();
  }

  @Override
  public void execute(Handler<AsyncResult<PgResult<Row>>> handler) {
    if (result == null) {
      result = new SimpleQueryResultHandler<>(handler);
      execHandler.handle(new SimpleQueryCommand<>(sql, new RowResultDecoder(), result));
    } else if (result.hasNext()) {
      handler.handle(Future.succeededFuture(result.next()));
    } else {
      throw new IllegalStateException();
    }
  }

  @Override
  public void close(Handler<AsyncResult<Void>> completionHandler) {

  }
}
