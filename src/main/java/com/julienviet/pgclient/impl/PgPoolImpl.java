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

import com.julienviet.pgclient.PgConnection;
import com.julienviet.pgclient.PgPool;
import com.julienviet.pgclient.PgPreparedStatement;
import com.julienviet.pgclient.PoolingMode;
import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.UpdateResult;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

/**
 * Todo :
 *
 * - handle timeout when acquiring a connection
 * - for per statement pooling, have several physical connection and use the less busy one to avoid head of line blocking effect
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */
class PgPoolImpl implements PgPool {

  private final PgClientImpl client;
  private final Context context;
  private final PoolingStrategy available;

  private interface PoolingStrategy {
    void acquire(Holder holder);
    void release(Holder holder);
    void close();
  }

  private interface Holder extends Handler<AsyncResult<PgConnection>> {

    Context context();

    void handleClosed();

    void handleException(Throwable err);

    default void use(PgConnection conn) {
      Context current = Vertx.currentContext();
      Context context = context();
      if (current == context) {
        handle(Future.succeededFuture(conn));
      } else {
        context.runOnContext(v -> {
          use(conn);
        });
      }
    }

    default void fail(Throwable err) {
      Context current = Vertx.currentContext();
      Context context = context();
      if (current == context) {
        handle(Future.failedFuture(err));
      } else {
        context.runOnContext(v -> {
          fail(err);
        });
      }
    }

    PgConnection connection();

  }

  private class ConnectionPooling implements PoolingStrategy {

    private final ArrayDeque<Holder> waiters = new ArrayDeque<>();
    private final int maxSize;
    private final Set<PgConnection> all = new HashSet<>();
    private final ArrayDeque<PgConnection> available = new ArrayDeque<>();
    private int connCount;

    public ConnectionPooling(int maxSize) {
      this.maxSize = maxSize;
    }

    @Override
    public void acquire(Holder holder) {
      waiters.add(holder);
      check();
    }

    @Override
    public void close() {
      for (PgConnection conn : new ArrayList<>(all)) {
        conn.close();
      }
    }

    private void doAcq(Handler<AsyncResult<PgConnection>> handler) {
      if (available.size() > 0) {
        PgConnection conn = available.poll();
        handler.handle(Future.succeededFuture(conn));
      } else {
        if (connCount < maxSize) {
          connCount++;
          client.connect(ar -> {
            if (ar.succeeded()) {
              PgConnection conn = ar.result();
              all.add(conn);
              available.add(conn);
              doAcq(handler);
            } else {
              handler.handle(Future.failedFuture(ar.cause()));
            }
          });
        }
      }
    }

    private void check() {
      if (waiters.size() > 0) {
        doAcq(ar -> {
          if (ar.succeeded()) {
            PgConnection conn = ar.result();
            Holder waiter = waiters.poll();
            conn.exceptionHandler(waiter::handleException);
            conn.closeHandler(v -> {
              all.remove(conn);
              connCount--;
              check();
              waiter.handleClosed();
            });
            waiter.use(conn);
          } else {
            Holder waiter;
            while ((waiter = waiters.poll()) != null) {
              waiter.fail(ar.cause());
            }
          }
        });
      }
    }

    @Override
    public void release(Holder holder) {
      PgConnection conn = holder.connection();
      conn.closeHandler(null);
      conn.exceptionHandler(null);
      available.add(conn);
      check();
    }

    public void release(Proxy proxy) {
      proxy.conn.closeHandler(null);
      proxy.conn.exceptionHandler(null);
      available.add(proxy.conn);
      check();
    }
  }

  private class StatementPooling implements PoolingStrategy {

    final Set<Holder> proxies = new HashSet<>();
    private PgConnection shared;
    private boolean connecting;
    private ArrayDeque<Holder> waiters = new ArrayDeque<>();

    @Override
    public void close() {
      if (shared != null) {
        shared.close();
      }
    }

    @Override
    public void acquire(Holder holder) {
      if (shared != null) {
        proxies.add(holder);
        holder.use(shared);
      } else {
        waiters.add(holder);
        if (!connecting) {
          connecting = true;
          client.connect(ar -> {
            connecting = false;
            if (ar.succeeded()) {
              PgConnection conn = ar.result();
              shared = conn;
              conn.exceptionHandler(err -> {
                for (Holder proxy : new ArrayList<>(proxies)) {
                  proxy.handleException(err);
                }
              });
              conn.closeHandler(v -> {
                shared = null;
                conn.exceptionHandler(null);
                conn.closeHandler(null);
                ArrayList<Holder> list = new ArrayList<>(proxies);
                proxies.clear();
                for (Holder proxy : list) {
                  proxy.handleClosed();
                }
              });
              Holder waiter;
              while ((waiter = waiters.poll()) != null) {
                proxies.add(waiter);
                waiter.use(conn);
              }
            } else {
              Holder waiter;
              while ((waiter = waiters.poll()) != null) {
                waiter.fail(ar.cause());
              }
            }
          });
        }
      }
    }

    @Override
    public void release(Holder holder) {
      proxies.remove(holder);
    }
  }

  private static class Waiter {

    private final Handler<AsyncResult<Proxy>> handler;
    private final Context context;

    Waiter(Handler<AsyncResult<Proxy>> handler, Context context) {
      this.handler = handler;
      this.context = context;
    }

    void use(Proxy conn) {
      Context current = Vertx.currentContext();
      if (current == context) {
        handler.handle(Future.succeededFuture(conn));
      } else {
        context.runOnContext(v -> {
          use(conn);
        });
      }
    }

    void fail(Throwable err) {
      Context current = Vertx.currentContext();
      if (current == context) {
        handler.handle(Future.failedFuture(err));
      } else {
        context.runOnContext(v -> {
          fail(err);
        });
      }
    }
  }

  PgPoolImpl(PgClientImpl client, int maxSize, PoolingMode mode) {
    if (maxSize < 1) {
      throw new IllegalArgumentException("Pool max size must be > 0");
    }
    this.context = client.vertx.getOrCreateContext();
    this.client = client;
    this.available = mode == PoolingMode.STATEMENT ? new StatementPooling() : new ConnectionPooling(maxSize);
  }

  @Override
  public void getConnection(Handler<AsyncResult<PgConnection>> handler) {
    Context current = Vertx.currentContext();
    if (current == context) {
      Proxy proxy = new Proxy(current, handler);
      available.acquire(proxy);
    } else {
      context.runOnContext(v -> getConnection(handler));
    }
  }

  private class Proxy implements PgConnection, Holder {

    private final Context context;
    private final Handler<AsyncResult<PgConnection>> handler;
    private PgConnection conn;
    private final AtomicBoolean closed = new AtomicBoolean();
    private volatile Handler<Throwable> exceptionHandler;
    private volatile Handler<Void> closeHandler;

    private Proxy(Context context, Handler<AsyncResult<PgConnection>> handler) {
      this.context = context;
      this.handler = handler;
    }

    public void handleException(Throwable err) {
      Handler<Throwable> handler = this.exceptionHandler;
      if (!closed.get() && handler != null) {
        handler.handle(err);
      }
    }

    @Override
    public Context context() {
      return context;
    }

    @Override
    public PgConnection connection() {
      return conn;
    }

    @Override
    public void handle(AsyncResult<PgConnection> event) {
      conn = event.result();
      handler.handle(event.map(this));
    }

    public void handleClosed() {
      Handler<Void> handler = this.closeHandler;
      if (closed.compareAndSet(false, true) && handler != null) {
        handler.handle(null);
      }
    }

    private void checkClosed() {
      if (closed.get()) {
        throw new IllegalStateException("Connection closed");
      }
    }

    @Override
    public void query(String sql, List<Object> params, Handler<AsyncResult<ResultSet>> handler) {
      if (closed.get()) {
        handler.handle(Future.failedFuture("Connection closed"));
        return;
      }
      conn.query(sql, handler);
    }

    @Override
    public void update(String sql, List<Object> params, Handler<AsyncResult<UpdateResult>> handler) {
      if (closed.get()) {
        handler.handle(Future.failedFuture("Connection closed"));
        return;
      }
      conn.update(sql, handler);
    }

    @Override
    public PgConnection execute(String sql, Handler<AsyncResult<ResultSet>> handler) {
      if (closed.get()) {
        handler.handle(Future.failedFuture("Connection closed"));
        return this;
      }
      conn.execute(sql, handler);
      return this;
    }

    @Override
    public void update(String sql, Handler<AsyncResult<UpdateResult>> handler) {
      if (closed.get()) {
        handler.handle(Future.failedFuture("Connection closed"));
        return;
      }
      conn.update(sql, handler);
    }

    @Override
    public void query(String sql, Handler<AsyncResult<ResultSet>> handler) {
      if (closed.get()) {
        handler.handle(Future.failedFuture("Connection closed"));
        return;
      }
      conn.query(sql, handler);
    }

    @Override
    public PgConnection exceptionHandler(Handler<Throwable> handler) {
      exceptionHandler = handler;
      return this;
    }

    @Override
    public PgConnection closeHandler(Handler<Void> handler) {
      closeHandler = handler;
      return this;
    }

    @Override
    public PgPreparedStatement prepare(String sql) {
      checkClosed();
      return conn.prepare(sql);
    }

    @Override
    public boolean isSSL() {
      checkClosed();
      return conn.isSSL();
    }

    @Override
    public void close() {
      if (closed.compareAndSet(false, true)) {
        available.release(this);
      }
    }
  }

  private class Foo<T> implements Holder {

    final Context context;
    final Handler<AsyncResult<T>> handler;
    final Function<PgConnection, Future<T>> f;

    private Foo(Context context, Handler<AsyncResult<T>> handler, Function<PgConnection, Future<T>> f) {
      this.context = context;
      this.handler = handler;
      this.f = f;
    }

    private PgConnection conn;

    @Override
    public Context context() {
      return context;
    }

    @Override
    public void handleClosed() {
    }

    @Override
    public void handleException(Throwable err) {
    }

    @Override
    public PgConnection connection() {
      return conn;
    }

    @Override
    public void handle(AsyncResult<PgConnection> event) {
      if (event.succeeded()) {
        conn = event.result();
        Future<T> fut = f.apply(conn);
        fut.setHandler(ar -> {
          available.release(this);
          handler.handle(ar);
        });
      } else {
        handler.handle(Future.failedFuture(event.cause()));
      }
    }
  }

  @Override
  public void query(String sql, Handler<AsyncResult<ResultSet>> handler) {
    Context current = Vertx.currentContext();
    if (current == context) {
      available.acquire(new Foo<>(current, handler, conn -> Future.future(f -> conn.query(sql, f))));
    } else {
      context.runOnContext(v -> query(sql, handler));
    }
  }

  @Override
  public void update(String sql, Handler<AsyncResult<UpdateResult>> handler) {
    Context current = Vertx.currentContext();
    if (current == context) {
      available.acquire(new Foo<>(current, handler, conn -> Future.future(f -> conn.update(sql, f))));
    } else {
      context.runOnContext(v -> update(sql, handler));
    }
  }

  @Override
  public void query(String sql, List<Object> params, Handler<AsyncResult<ResultSet>> handler) {
    Context current = Vertx.currentContext();
    if (current == context) {
      available.acquire(new Foo<>(current, handler, conn -> Future.future(f -> conn.query(sql, params, f))));
    } else {
      context.runOnContext(v -> query(sql, params, handler));
    }
  }

  @Override
  public void update(String sql, List<Object> params, Handler<AsyncResult<UpdateResult>> handler) {
    Context current = Vertx.currentContext();
    if (current == context) {
      available.acquire(new Foo<>(current, handler, conn -> Future.future(f -> conn.update(sql, params, f))));
    } else {
      context.runOnContext(v -> update(sql, params, handler));
    }
  }

  @Override
  public void close() {
  }
}
