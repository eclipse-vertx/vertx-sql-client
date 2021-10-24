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

import io.vertx.core.impl.ContextInternal;
import io.vertx.core.spi.metrics.ClientMetrics;
import io.vertx.sqlclient.PrepareOptions;
import io.vertx.sqlclient.PreparedStatement;
import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.impl.command.PrepareStatementCommand;
import io.vertx.core.*;
import io.vertx.sqlclient.impl.tracing.QueryTracer;
import io.vertx.sqlclient.spi.ConnectionFactory;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public abstract class SqlConnectionBase<C extends SqlClient> extends SqlClientBase<C> {

  protected final ContextInternal context;
  protected final ConnectionFactory factory;
  protected final Connection conn;

  protected SqlConnectionBase(ContextInternal context, ConnectionFactory factory, Connection conn, QueryTracer tracer, ClientMetrics metrics) {
    super(tracer, metrics);
    this.context = context;
    this.factory = factory;
    this.conn = conn;
  }

  public ConnectionFactory factory() {
    return factory;
  }

  public Connection unwrap() {
    return conn;
  }

  public C prepare(String sql, PrepareOptions options, Handler<AsyncResult<PreparedStatement>> handler) {
    Future<PreparedStatement> fut = prepare(sql, options);
    if (handler != null) {
      fut.onComplete(handler);
    }
    return (C)this;
  }

  public Future<PreparedStatement> prepare(String sql, PrepareOptions options) {
    return schedule(context, new PrepareStatementCommand(sql, options, true))
      .compose(
      cr -> Future.succeededFuture(PreparedStatementImpl.create(conn, tracer, metrics, context, cr, autoCommit())),
      err -> {
        if (conn.isIndeterminatePreparedStatementError(err)) {
          return Future.succeededFuture(PreparedStatementImpl.create(conn, tracer, metrics, context, options, sql, autoCommit()));
        } else {
          return Future.failedFuture(err);
        }
      });
  }

  public C prepare(String sql, Handler<AsyncResult<PreparedStatement>> handler) {
    return prepare(sql, null, handler);
  }

  public Future<PreparedStatement> prepare(String sql) {
    return prepare(sql, (PrepareOptions) null);
  }
}
