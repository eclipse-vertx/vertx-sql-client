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
import com.julienviet.pgclient.ResultSet;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class PreparedQueryImpl implements PgQuery, QueryResultHandler {

  private final PreparedStatementImpl ps;
  private final List<Object> params;
  private int fetch;

  private Handler<ResultSet> resultHandler;
  private Handler<Throwable> exceptionHandler;
  private Handler<Void> endHandler;

  private String portal;
  private ResultSet result;
  private boolean completed;
  private boolean closed;

  PreparedQueryImpl(PreparedStatementImpl ps, List<Object> params) {
    this.ps = ps;
    this.params = params;
  }

  @Override
  public PgQuery exceptionHandler(Handler<Throwable> handler) {
    exceptionHandler = handler;
    return this;
  }
  @Override
  public PgQuery handler(Handler<ResultSet> handler) {
    if (handler != null) {
      resultHandler = handler;
      if (portal == null) {
        portal = fetch > 0 ? UUID.randomUUID().toString() : "";
        ps.execute(params, fetch, portal, false, this);
      }
    } else {
      if (!completed) {
        throw new UnsupportedOperationException("Todo : unsubscribe");
      }
    }
    return this;
  }

  @Override
  public PgQuery pause() {
    return this;
  }

  @Override
  public PgQuery resume() {
    return this;
  }

  @Override
  public PgQuery endHandler(Handler<Void> handler) {
    endHandler = handler;
    return this;
  }

  private <T> void callHandler(Handler<T> handler, T event) {
    if (handler != null) {
      handler.handle(event);
    }
  }

  @Override
  public PreparedQueryImpl fetch(int size) {
    if (size < 0) {
      throw new IllegalArgumentException("Fetch size must be 0 (disabled) or a positive number");
    }
    this.fetch = size;
    return this;
  }

  @Override
  public void beginResult(List<String> columnNames) {
    this.result = new ResultSet().setColumnNames(columnNames).setResults(new ArrayList<>());
  }

  @Override
  public void endResult(boolean suspended) {
    if (closed) {
      return;
    }
    ResultSet tmp = result;
    result = null;
    callHandler(resultHandler, tmp);
    if (closed) {
      return;
    }
    if (suspended) {
      ps.execute(params, fetch, portal, true, this);
    } else {
      if (!completed) {
        completed = true;
        callHandler(endHandler, null);
      }
    }
  }
/*
  @Override
  public void execute(Handler<AsyncResult<ResultSet>> handler) {
    ps.execute(params, fetch, portal, true, new PreparedQueryResultHandler(handler));
  }
*/
  @Override
  public void handleRow(JsonArray row) {
    result.getResults().add(row);
  }

  @Override
  public void end() {
  }

  @Override
  public void fail(Throwable cause) {
    if (!completed) {
      completed = true;
      callHandler(exceptionHandler, cause);
    }
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
          callHandler(endHandler, null);
          ps.closePortal(portal, completionHandler);
        }
      }
    }
  }
}
