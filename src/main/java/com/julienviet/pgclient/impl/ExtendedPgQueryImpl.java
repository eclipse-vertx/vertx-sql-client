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
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;

import java.util.*;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class ExtendedPgQueryImpl implements PgQuery {

  private final PgPreparedStatementImpl ps;
  private final List<Object> params;
  private int fetch;

  private String portal;
  private boolean completed;
  private boolean closed;
  private ExtendedQueryResultHandler result;

  ExtendedPgQueryImpl(PgPreparedStatementImpl ps, List<Object> params) {
    this.ps = ps;
    this.params = params;
  }

  private <T> void callHandler(Handler<T> handler, T event) {
    if (handler != null) {
      handler.handle(event);
    }
  }

  @Override
  public ExtendedPgQueryImpl fetch(int size) {
    if (size < 0) {
      throw new IllegalArgumentException("Fetch size must be 0 (disabled) or a positive number");
    }
    this.fetch = size;
    return this;
  }

  @Override
  public boolean hasNext() {
    return result.isSuspended();
  }

  @Override
  public void next(Handler<AsyncResult<PgResult>> handler) {
    if (!result.isSuspended()) {
      handler.handle(Future.failedFuture(new NoSuchElementException()));
    } else {
      result = new ExtendedQueryResultHandler(handler);
      ps.execute(params, fetch, portal, true, result);
    }
  }

  @Override
  public void execute(Handler<AsyncResult<PgResult>> handler) {
    if (result != null) {
      throw new IllegalStateException();
    }
    result = new ExtendedQueryResultHandler(handler);
    portal = fetch > 0 ? UUID.randomUUID().toString() : null;
    ps.execute(params, fetch, portal, false, result);
  }

  @Override
  public void close(Handler<AsyncResult<Void>> completionHandler) {
    if (!closed) {
      closed = true;
      if (portal == null) {
        // Nothing to do
        completionHandler.handle(Future.succeededFuture());
      } else {
        if (!completed) {
          completed = true;
          ps.closePortal(portal, completionHandler);
        }
      }
    }
  }
}
