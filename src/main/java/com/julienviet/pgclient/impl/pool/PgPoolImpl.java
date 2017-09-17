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

package com.julienviet.pgclient.impl.pool;

import com.julienviet.pgclient.PgConnection;
import com.julienviet.pgclient.PgPool;
import com.julienviet.pgclient.PoolingMode;
import com.julienviet.pgclient.impl.PgClientImpl;
import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.UpdateResult;

import java.util.List;
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
public class PgPoolImpl implements PgPool {

  private final Context context;
  private final PoolingStrategy pooling;

  interface PoolingStrategy {
    void acquire(Holder holder);
    void release(Holder holder);
    void close();
  }

  interface Holder extends Handler<AsyncResult<PgConnection>> {

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

  public PgPoolImpl(Context context, PgClientImpl client, int maxSize, PoolingMode mode) {
    if (maxSize < 1) {
      throw new IllegalArgumentException("Pool max size must be > 0");
    }
    this.context = context;
    this.pooling = mode == PoolingMode.STATEMENT ? new StatementPooling(client) : new ConnectionPooling(client, maxSize);
  }

  @Override
  public void getConnection(Handler<AsyncResult<PgConnection>> handler) {
    Context current = Vertx.currentContext();
    if (current == context) {
      Proxy proxy = new Proxy(pooling, current, handler);
      pooling.acquire(proxy);
    } else {
      context.runOnContext(v -> getConnection(handler));
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
          pooling.release(this);
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
      pooling.acquire(new Foo<>(current, handler, conn -> Future.future(f -> conn.query(sql, f))));
    } else {
      context.runOnContext(v -> query(sql, handler));
    }
  }

  @Override
  public void update(String sql, Handler<AsyncResult<UpdateResult>> handler) {
    Context current = Vertx.currentContext();
    if (current == context) {
      pooling.acquire(new Foo<>(current, handler, conn -> Future.future(f -> conn.update(sql, f))));
    } else {
      context.runOnContext(v -> update(sql, handler));
    }
  }

  @Override
  public void query(String sql, List<Object> params, Handler<AsyncResult<ResultSet>> handler) {
    Context current = Vertx.currentContext();
    if (current == context) {
      pooling.acquire(new Foo<>(current, handler, conn -> Future.future(f -> conn.query(sql, params, f))));
    } else {
      context.runOnContext(v -> query(sql, params, handler));
    }
  }

  @Override
  public void update(String sql, List<Object> params, Handler<AsyncResult<UpdateResult>> handler) {
    Context current = Vertx.currentContext();
    if (current == context) {
      pooling.acquire(new Foo<>(current, handler, conn -> Future.future(f -> conn.update(sql, params, f))));
    } else {
      context.runOnContext(v -> update(sql, params, handler));
    }
  }

  @Override
  public void close() {
  }
}
