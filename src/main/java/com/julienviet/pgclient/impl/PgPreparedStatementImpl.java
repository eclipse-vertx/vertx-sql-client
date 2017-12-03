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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
class PgPreparedStatementImpl implements PgPreparedStatement {

  private final Connection conn;
  private final PreparedStatement ps;
  private final AtomicBoolean closed = new AtomicBoolean();

  PgPreparedStatementImpl(Connection conn, PreparedStatement ps) {
    this.conn = conn;
    this.ps = ps;
  }

  @Override
  public PgQuery query(List<Object> args) {
    String msg = ps.paramDesc.validate(args);
    if (msg != null) {
      throw new IllegalArgumentException(msg);
    }
    return new ExtendedPgQueryImpl(this, args);
  }

  @Override
  public PgUpdate update(List<Object> params) {
    return new PgUpdateImpl(this, params);
  }

  @Override
  public PgBatch batch() {
    return new BatchImpl(this, ps.paramDesc);
  }

  @Override
  public void close() {
    close(ar -> {
    });
  }

  void execute(List<Object> params,
               int fetch,
               String portal,
               boolean suspended,
               QueryResultHandler<PgRow> handler) {
    conn.schedule(new ExtendedQueryCommand<>(ps, params, fetch, portal, suspended, new JsonResultDecoder(), handler));
  }

  void update(List<List<Object>> paramsList, Handler<AsyncResult<List<PgResult>>> handler) {
    conn.schedule(new PreparedUpdateCommand(ps, paramsList, ar -> {
      handler.handle(ar.map(ur -> {
        return ur.stream().map(a -> new PgResultImpl(a.updatedCount())).collect(Collectors.toList());
      }));
    }));
  }

  @Override
  public void close(Handler<AsyncResult<Void>> completionHandler) {
    if (closed.compareAndSet(false, true)) {
      conn.schedule(new CloseStatementCommand(completionHandler));
    } else {
      completionHandler.handle(Future.failedFuture("Already closed"));
    }
  }

  void closePortal(String portal, Handler<AsyncResult<Void>> handler) {
    conn.schedule(new ClosePortalCommand(portal, handler));
  }
}
