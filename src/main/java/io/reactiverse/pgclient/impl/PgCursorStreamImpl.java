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
import io.reactiverse.pgclient.impl.codec.decoder.RowDescription;
import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Handler;

import java.util.Iterator;
import java.util.UUID;

public class PgCursorStreamImpl implements PgStream<Row> {

  private final PgPreparedQueryImpl ps;
  private final int fetch;
  private final Tuple params;

  private Handler<Void> endHandler;
  private Handler<Row> rowHandler;
  private Handler<Throwable> exceptionHandler;
  private boolean paused;
  private boolean emitting;
  private QueryCursor cursor;

  class QueryCursor implements QueryResultHandler<PgRowSet>, Handler<AsyncResult<Boolean>> {

    private final String portal = UUID.randomUUID().toString();
    private Iterator<Row> result;
    private boolean suspended;
    private boolean closed;

    @Override
    public void handleResult(int updatedCount, int size, RowDescription desc, PgRowSet rowSet) {
      synchronized (PgCursorStreamImpl.this) {
        result = rowSet.iterator();
      }
    }

    @Override
    public void handle(AsyncResult<Boolean> res) {
      if (res.failed()) {
        Handler<Throwable> handler;
        synchronized (PgCursorStreamImpl.this) {
          cursor = null;
          handler = exceptionHandler;
        }
        if (handler != null) {
          handler.handle(res.cause());
        }
      } else {
        synchronized (PgCursorStreamImpl.this) {
          suspended = res.result();
        }
        checkPending();
      }
    }

    private void checkPending() {
      synchronized (PgCursorStreamImpl.this) {
        if (emitting) {
          return;
        }
      }
      while (true) {
        synchronized (PgCursorStreamImpl.this) {
          if (paused || result == null) {
            emitting = false;
            break;
          }
          Handler handler;
          Object event;
          if (result.hasNext()) {
            handler = rowHandler;
            event = result.next();
          } else {
            result = null;
            if (suspended) {
              emitting = false;
              ps.execute(params, fetch, portal, true, false, PgRowSetImpl.COLLECTOR, this, this);
              break;
            } else {
              cursor = null;
              handler = endHandler;
              event = null;
            }
          }
          if (handler != null) {
            handler.handle(event);
          }
        }
      }
    }

    public void close(Handler<AsyncResult<Void>> completionHandler) {
      synchronized (PgCursorStreamImpl.this) {
        if (closed) {
          return;
        }
        closed = true;
      }
      ps.closePortal(portal, completionHandler);
    }
  }

  PgCursorStreamImpl(PgPreparedQueryImpl ps, int fetch, Tuple params) {
    this.ps = ps;
    this.fetch = fetch;
    this.params = params;
  }

  @Override
  public synchronized PgStream<Row> exceptionHandler(Handler<Throwable> handler) {
    exceptionHandler = handler;
    return this;
  }

  @Override
  public PgStream<Row> handler(Handler<Row> handler) {
    QueryCursor c;
    synchronized (this) {
      if (handler != null) {
        if (cursor == null) {
          rowHandler = handler;
          c = cursor = new QueryCursor();
        } else {
          throw new UnsupportedOperationException("Handle me gracefully");
        }
      } else {
        if (cursor != null) {
          cursor = null;
        } else {
          rowHandler = null;
        }
        return this;
      }
    }
    ps.execute(params, fetch, c.portal, false, false, PgRowSetImpl.COLLECTOR, c, c);
    return this;
  }

  @Override
  public synchronized PgStream<Row> pause() {
    paused = true;
    return this;
  }

  @Override
  public PgStream<Row> resume() {
    QueryCursor c;
    synchronized (this) {
      paused = false;
      if ((c = cursor) == null) {
        return this;
      }
    }
    c.checkPending();
    return this;
  }

  @Override
  public synchronized PgStream<Row> endHandler(Handler<Void> handler) {
    endHandler = handler;
    return this;
  }
}
