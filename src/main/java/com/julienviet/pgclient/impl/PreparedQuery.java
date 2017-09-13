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
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLRowStream;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class PreparedQuery implements PgQuery, SQLRowStream {

  private static final int READY = 0, PENDING = 1, IN_PROGRESS = 2, COMPLETED = 3;

  final PreparedStatementImpl ps;
  final List<Object> params;
  private int fetch;
  private int status;
  private String portal;
  private Handler<JsonArray> rowHandler;
  private Handler<Void> endHandler;
  private Handler<Throwable> exceptionHandler;
  private Handler<Void> resultSetClosedHandler;
  private final AtomicBoolean closed = new AtomicBoolean();

  PreparedQuery(PreparedStatementImpl ps, List<Object> params) {
    this.ps = ps;
    this.params = params;
  }

  @Override
  public PreparedQuery fetch(int size) {
    if (size < 0) {
      throw new IllegalArgumentException("Fetch size must be 0 (disabled) or a positive number");
    }
    this.fetch = size;
    return this;
  }

  @Override
  public boolean inProgress() {
    return status == IN_PROGRESS;
  }

  @Override
  public boolean completed() {
    return status == COMPLETED;
  }

  @Override
  public void execute(Handler<AsyncResult<ResultSet>> handler) {
    execute(new PreparedQueryResultHandler(handler));
  }

  private void execute(QueryResultHandler handler) {
    if (closed.get()) {
      throw new IllegalStateException("Query closed");
    }
    QueryResultHandler adapter = new QueryResultHandler() {
      @Override
      public void beginResult(List<String> columnNames) {
        handler.beginResult(columnNames);
      }
      @Override
      public void handleRow(JsonArray row) {
        handler.handleRow(row);
      }
      @Override
      public void endResult(boolean suspended) {
        status = suspended ? IN_PROGRESS : COMPLETED;
        handler.endResult(suspended);
      }
      @Override
      public void fail(Throwable cause) {
        status = COMPLETED;
        handler.fail(cause);
      }
      @Override
      public void end() {
        handler.end();
      }
    };
    switch (status) {
      case READY:
        status = PENDING;
        portal = fetch > 0 ? UUID.randomUUID().toString() : "";
        ps.execute(params, fetch, portal, false, adapter);
        break;
      case IN_PROGRESS:
        status = PENDING;
        ps.execute(params, fetch, portal, true, adapter);
        break;
      case PENDING:
        throw new IllegalStateException("Query in progress");
      case COMPLETED:
        throw new IllegalStateException("Already executed");
    }
  }

  @Override
  public int column(String s) {
    return 0;
  }

  @Override
  public List<String> columns() {
    return null;
  }

  @Override
  public SQLRowStream resultSetClosedHandler(Handler<Void> handler) {
    resultSetClosedHandler = handler;
    return this;
  }

  @Override
  public void moreResults() {
    Handler<JsonArray> _rowHandler = rowHandler;
    Handler<Void> _endHandler = endHandler;
    Handler<Throwable> _exceptionHandler = exceptionHandler;
    Handler<Void> _resultSetClosedHandler = resultSetClosedHandler;
    PreparedQuery.this.execute(new QueryResultHandler() {
      @Override
      public void beginResult(List<String> columnNames) {
      }
      @Override
      public void handleRow(JsonArray row) {
        if (_rowHandler != null) {
          _rowHandler.handle(row);
        }
      }
      @Override
      public void endResult(boolean suspended) {
        if (suspended) {
          if (_resultSetClosedHandler != null) {
            _resultSetClosedHandler.handle(null);
          }
        } else {
          if (_endHandler != null) {
            _endHandler.handle(null);
          }
        }
      }
      @Override
      public void fail(Throwable cause) {
        if (_exceptionHandler != null) {
          _exceptionHandler.handle(cause);
        }
      }
      @Override
      public void end() {
      }
    });
  }

  @Override
  public void close() {
    close(ar -> {});
  }

  @Override
  public void close(Handler<AsyncResult<Void>> completionHandler) {
    if (closed.compareAndSet(false, true)) {
      switch (status) {
        case READY:
          status = COMPLETED;
          completionHandler.handle(Future.succeededFuture());
          break;
        case IN_PROGRESS:
          status = COMPLETED;
          ps.closePortal(portal, completionHandler);
          break;
        case PENDING:
          ps.closePortal(portal, ar -> {
            status = COMPLETED;
            completionHandler.handle(ar);
          });
          break;
        case COMPLETED:
          completionHandler.handle(Future.succeededFuture());
          break;
      }
    } else {
      completionHandler.handle(Future.succeededFuture());
    }
  }

  @Override
  public SQLRowStream exceptionHandler(Handler<Throwable> handler) {
    exceptionHandler = handler;
    return this;
  }

  @Override
  public SQLRowStream handler(Handler<JsonArray> handler) {
    rowHandler = handler;
    return this;
  }

  @Override
  public SQLRowStream pause() {
    return this;
  }

  @Override
  public SQLRowStream resume() {
    return this;
  }

  @Override
  public SQLRowStream endHandler(Handler<Void> handler) {
    endHandler = handler;
    return this;
  }
}
