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

package io.vertx.sqlclient.impl;

import io.vertx.sqlclient.Cursor;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.RowStream;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

import java.util.Iterator;

public class RowStreamImpl implements RowStream<Row>, Handler<AsyncResult<RowSet>> {

  private final PreparedQueryImpl ps;
  private final int fetch;
  private final Tuple params;

  private Handler<Void> endHandler;
  private Handler<Row> rowHandler;
  private Handler<Throwable> exceptionHandler;
  private long demand;
  private boolean emitting;
  private Cursor cursor;

  private Iterator<Row> result;

  RowStreamImpl(PreparedQueryImpl ps, int fetch, Tuple params) {
    this.ps = ps;
    this.fetch = fetch;
    this.params = params;
    this.demand = Long.MAX_VALUE;
  }

  @Override
  public synchronized RowStream<Row> exceptionHandler(Handler<Throwable> handler) {
    exceptionHandler = handler;
    return this;
  }

  @Override
  public RowStream<Row> handler(Handler<Row> handler) {
    Cursor c;
    synchronized (this) {
      if (handler != null) {
        if (cursor == null) {
          rowHandler = handler;
          c = cursor = ps.cursor(params);
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
    c.read(fetch, this);
    return this;
  }

  @Override
  public synchronized RowStream<Row> pause() {
    demand = 0L;
    return this;
  }

  public RowStream<Row> fetch(long amount) {
    if (amount < 0L) {
      throw new IllegalArgumentException("Invalid fetch amount " + amount);
    }
    synchronized (this) {
      demand += amount;
      if (demand < 0L) {
        demand = Long.MAX_VALUE;
      }
      if (cursor == null) {
        return this;
      }
    }
    checkPending();
    return this;
  }

  @Override
  public RowStream<Row> resume() {
    return fetch(Long.MAX_VALUE);
  }

  @Override
  public synchronized RowStream<Row> endHandler(Handler<Void> handler) {
    endHandler = handler;
    return this;
  }

  @Override
  public void handle(AsyncResult<RowSet> ar) {
    if (ar.failed()) {
      Handler<Throwable> handler;
      synchronized (RowStreamImpl.this) {
        cursor = null;
        handler = exceptionHandler;
      }
      if (handler != null) {
        handler.handle(ar.cause());
      }
    } else {
      result = ar.result().iterator();
      checkPending();
    }
  }

  @Override
  public void close() {
    close(ar -> {});
  }

  @Override
  public void close(Handler<AsyncResult<Void>> completionHandler) {
    Cursor c;
    synchronized (this) {
      if ((c = cursor) == null) {
        return;
      }
      cursor = null;
    }
    c.close(completionHandler);
  }

  private void checkPending() {
    synchronized (RowStreamImpl.this) {
      if (emitting) {
        return;
      }
      emitting = true;
    }
    while (true) {
      synchronized (RowStreamImpl.this) {
        if (demand == 0L || result == null) {
          emitting = false;
          break;
        }
        Handler handler;
        Object event;
        if (result.hasNext()) {
          handler = rowHandler;
          event = result.next();
          if (demand != Long.MAX_VALUE) {
            demand--;
          }
        } else {
          result = null;
          emitting = false;
          if (cursor.hasMore()) {
            cursor.read(fetch, this);
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
}
