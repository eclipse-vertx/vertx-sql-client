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

import io.reactiverse.pgclient.*;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;

import java.util.*;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class PgCursorImpl implements PgCursor {

  private final PgPreparedQueryImpl ps;
  private final Tuple params;

  private String portal;
  private boolean closed;
  private PgResultBuilder<PgRowSet, PgRowSetImpl, PgRowSet> result;

  PgCursorImpl(PgPreparedQueryImpl ps, Tuple params) {
    this.ps = ps;
    this.params = params;
  }

  @Override
  public synchronized boolean hasMore() {
    if (result == null) {
      throw new IllegalStateException("No current cursor read");
    }
    return result.isSuspended();
  }

  @Override
  public synchronized void read(int count, Handler<AsyncResult<PgRowSet>> handler) {
    if (portal == null) {
      portal = UUID.randomUUID().toString();
      result = new PgResultBuilder<>(PgRowSetImpl.FACTORY, handler);
      ps.execute(params, count, portal, false, false, PgRowSetImpl.COLLECTOR, result, result);
    } else if (result.isSuspended()) {
      result = new PgResultBuilder<>(PgRowSetImpl.FACTORY, handler);
      ps.execute(params, count, portal, true, false, PgRowSetImpl.COLLECTOR, result, result);
    } else {
      throw new IllegalStateException();
    }
  }

  @Override
  public synchronized void close(Handler<AsyncResult<Void>> completionHandler) {
    if (!closed) {
      closed = true;
      if (portal == null) {
        completionHandler.handle(Future.succeededFuture());
      } else {
        String p = portal;
        portal = null;
        result = null;
        ps.closePortal(p, completionHandler);
      }
    }
  }
}
