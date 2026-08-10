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
package io.vertx.sqlclient.impl.pool;

import io.vertx.codegen.annotations.Nullable;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.internal.*;
import io.vertx.sqlclient.*;
import io.vertx.sqlclient.impl.SqlClientInternal;
import io.vertx.sqlclient.spi.Driver;

import java.time.Duration;
import java.util.function.Function;

public class CloseablePool implements Pool, SqlClientInternal {

  private final VertxInternal vertx;
  private final CloseableResource<? extends Pool> delegate;

  public CloseablePool(VertxInternal vertx, CloseableResource<? extends Pool> delegate) {
    this.vertx = vertx;
    this.delegate = delegate;
  }

  @Override
  public Driver driver() {
    return ((SqlClientInternal)delegate.get()).driver();
  }

  @Override
  public void group(Handler<SqlClient> block) {

  }

  @Override
  public Future<SqlConnection> getConnection() {
    return delegate.get().getConnection();
  }

  @Override
  public Query<RowSet<Row>> query(String sql) {
    return delegate.get().query(sql);
  }

  @Override
  public PreparedQuery<RowSet<Row>> preparedQuery(String sql) {
    return delegate.get().preparedQuery(sql);
  }

  @Override
  public <T> Future<@Nullable T> withTransaction(TransactionPropagation txPropagation,
                                                 Function<SqlConnection, Future<@Nullable T>> function) {
    return delegate.get().withTransaction(txPropagation, function);
  }

  @Override
  public int size() {
    return delegate.get().size();
  }

  @Override
  public PreparedQuery<RowSet<Row>> preparedQuery(String sql, PrepareOptions options) {
    return delegate.get().preparedQuery(sql, options);
  }

  @Override
  public Future<Void> close() {
    return delegate.shutdown(Duration.ZERO);
  }
}
