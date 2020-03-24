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
import io.vertx.sqlclient.PreparedStatement;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.impl.command.PrepareStatementCommand;
import io.vertx.core.*;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public abstract class SqlConnectionBase<C extends SqlClient> extends SqlClientBase<C> {

  protected final ContextInternal context;
  protected final Connection conn;

  protected SqlConnectionBase(ContextInternal context, Connection conn) {
    this.context = context;
    this.conn = conn;
  }

  public C prepare(String sql, Handler<AsyncResult<PreparedStatement<RowSet<Row>>>> handler) {
    Future<PreparedStatement<RowSet<Row>>> fut = prepare(sql);
    if (handler != null) {
      fut.onComplete(handler);
    }
    return (C)this;
  }

  public Future<PreparedStatement<RowSet<Row>>> prepare(String sql) {
    Promise<io.vertx.sqlclient.impl.PreparedStatement> promise = promise();
    schedule(new PrepareStatementCommand(sql), promise);
    return promise.future().map(cr -> PreparedStatementImpl.create(conn, context, cr, autoCommit()));
  }
}
