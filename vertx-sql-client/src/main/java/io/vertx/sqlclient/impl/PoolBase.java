/*
 * Copyright (C) 2019,2020 IBM Corporation
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
 */
package io.vertx.sqlclient.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.impl.CloseFuture;
import io.vertx.core.impl.ContextInternal;
import io.vertx.core.impl.VertxInternal;
import io.vertx.core.impl.future.PromiseInternal;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PrepareOptions;
import io.vertx.sqlclient.PreparedQuery;
import io.vertx.sqlclient.Query;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.SqlConnection;

import java.util.function.Function;

public class PoolBase<P extends Pool> implements Pool {

  private final VertxInternal vertx;
  private final CloseFuture closeFuture;
  private final Pool delegate;

  public PoolBase(VertxInternal vertx, CloseFuture closeFuture, Pool delegate) {
    this.vertx = vertx;
    this.closeFuture = closeFuture;
    this.delegate = delegate;
  }

  @Override
  public void getConnection(Handler<AsyncResult<SqlConnection>> handler) {
    delegate.getConnection(handler);
  }

  @Override
  public Future<SqlConnection> getConnection() {
    return delegate.getConnection();
  }

  @Override
  public Query<RowSet<Row>> query(String sql) {
    return delegate.query(sql);
  }

  @Override
  public PreparedQuery<RowSet<Row>> preparedQuery(String sql) {
    return delegate.preparedQuery(sql);
  }

  @Override
  public P connectHandler(Handler<SqlConnection> handler) {
    delegate.connectHandler(handler);
    return (P) this;
  }

  @Override
  public P connectionProvider(Function<Context, Future<SqlConnection>> provider) {
    delegate.connectionProvider(provider);
    return (P) this;
  }

  @Override
  public int size() {
    return delegate.size();
  }

  @Override
  public PreparedQuery<RowSet<Row>> preparedQuery(String sql, PrepareOptions options) {
    return delegate.preparedQuery(sql, options);
  }

  @Override
  public void close(Handler<AsyncResult<Void>> handler) {
    ContextInternal closingCtx = vertx.getOrCreateContext();
    closeFuture.close(handler != null ? closingCtx.promise(handler) : null);
  }

  @Override
  public Future<Void> close() {
    ContextInternal closingCtx = vertx.getOrCreateContext();
    PromiseInternal<Void> promise = closingCtx.promise();
    closeFuture.close(promise);
    return promise.future();
  }
}
