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

public abstract class PgClientBase<C extends PgClient> implements PgClient {

  protected abstract void schedule(CommandBase cmd);

  @Override
  public PgClient query(String sql, Handler<AsyncResult<PgResult<Row>>> handler) {
    schedule(new SimpleQueryCommand<>(sql, new RowResultDecoder(), new SimpleQueryResultHandler<>(handler)));
    return this;
  }

  @Override
  public C preparedQuery(String sql, Tuple arguments, Handler<AsyncResult<PgResult<Row>>> handler) {
    schedule(new PrepareStatementCommand(sql, ar -> {
      if (ar.succeeded()) {
        return new ExtendedQueryCommand<>(ar.result(), arguments, new RowResultDecoder(), new ExtendedQueryResultHandler<>(handler));
      } else {
        handler.handle(Future.failedFuture(ar.cause()));
        return null;
      }
    }));
    return (C) this;
  }

  @Override
  public C preparedBatch(String sql, List<Tuple> batch, Handler<AsyncResult<PgBatchResult<Row>>> handler) {
    schedule(new PrepareStatementCommand(sql,  ar -> {
      if (ar.succeeded()) {
        return new ExtendedBatchQueryCommand<>(
          ar.result(),
          batch.iterator(),
          new RowResultDecoder()
          , new BatchQueryResultHandler(batch.size(), handler));
      } else {
        handler.handle(Future.failedFuture(ar.cause()));
        return null;
      }
    }));
    return (C) this;
  }
}
