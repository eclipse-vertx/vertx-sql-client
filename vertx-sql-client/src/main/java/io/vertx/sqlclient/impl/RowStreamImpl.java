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

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.internal.ContextInternal;
import io.vertx.sqlclient.*;
import io.vertx.sqlclient.desc.RowDescriptor;

import java.util.Iterator;

public class RowStreamImpl implements RowStreamInternal, Handler<AsyncResult<RowSet<Row>>> {

  private final PreparedStatementBase ps;
  private final ContextInternal context;
  private final int fetch;
  private final Tuple params;

  private Handler<Void> endHandler;
  private Handler<Row> rowHandler;
  private Handler<Throwable> exceptionHandler;
  private long demand;
  private boolean emitting;
  private Cursor cursor;
  private boolean readInProgress;
  private Iterator<Row> result;

  RowStreamImpl(PreparedStatementBase ps, ContextInternal context, int fetch, Tuple params) {
    this.ps = ps;
    this.context = context;
    this.fetch = fetch;
    this.params = params;
    this.demand = Long.MAX_VALUE;
  }

  public synchronized Cursor cursor() {
    return cursor;
  }

  @Override
  public synchronized RowDescriptor rowDescriptor() {
    return cursor != null ? cursor.rowDescriptor() : null;
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
          if (readInProgress) {
            return this;
          }
          readInProgress = true;
        } else {
          throw new UnsupportedOperationException("Handle me gracefully");
        }
      } else {
        rowHandler = null;
        if (cursor != null) {
          cursor.close();
          readInProgress = false;
          cursor = null;
          result = null; // Will stop the current emission if any
        }
        return this;
      }
    }
    c.read(fetch).onComplete(this);
    return this;
  }

  @Override
  public synchronized RowStream<Row> pause() {
    demand = 0L;
    return this;
  }

  @Override
  public RowStream<Row> fetch(long amount) {
    if (amount < 0L) {
      throw new IllegalArgumentException("Invalid fetch amount " + amount);
    }
    synchronized (this) {
      demand += amount;
      if (demand < 0L) {
        demand = Long.MAX_VALUE;
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
  public void handle(AsyncResult<RowSet<Row>> ar) {
    if (ar.failed()) {
      Handler<Throwable> handler;
      synchronized (this) {
        readInProgress = false;
        cursor = null;
        result = null;
        handler = exceptionHandler;
      }
      if (handler != null) {
        handler.handle(ar.cause());
      }
    } else {
      synchronized (this) {
        readInProgress = false;
        RowIterator<Row> it = ar.result().iterator();
        if (it.hasNext()) {
          result = it;
        }
      }
      checkPending();
    }
  }

  @Override
  public Future<Void> close() {
    Cursor c;
    synchronized (this) {
      c = cursor;
      cursor = null;
    }
    if (c != null) {
      return c.close();
    } else {
      return context.succeededFuture();
    }
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
        if (demand == 0L) {
          emitting = false;
          break;
        }
        Handler handler;
        Object event;
        if (result != null) {
          handler = rowHandler;
          event = result.next();
          if (demand != Long.MAX_VALUE) {
            demand--;
          }
          if (!result.hasNext()) {
            result = null;
          }
        } else {
          emitting = false;
          if (readInProgress) {
            break;
          } else {
            if (cursor == null) {
              break;
            } else if (cursor.hasMore()) {
              readInProgress = true;
              cursor.read(fetch).onComplete(this);
              break;
            } else {
              cursor.close();
              handler = v -> {
                endHandler.handle(null);
                synchronized (RowStreamImpl.this) {
                  cursor = null;
                }
              };
              event = null;
            }
          }
        }
        if (handler != null) {
          handler.handle(event);
        }
      }
    }
  }
}
