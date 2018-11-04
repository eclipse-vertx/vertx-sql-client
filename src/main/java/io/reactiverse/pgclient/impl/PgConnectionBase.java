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

import io.reactiverse.pgclient.PgPreparedQuery;
import io.vertx.core.*;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public abstract class PgConnectionBase<C extends PgConnectionBase> extends PgClientBase<C> {

  protected final Context context;
  protected final Connection conn;

  PgConnectionBase(Context context, Connection conn) {
    this.context = context;
    this.conn = conn;
  }

  public C prepare(String sql, Handler<AsyncResult<PgPreparedQuery>> handler) {
    schedule(new PrepareStatementCommand(sql), cr -> {
      if (cr.succeeded()) {
        handler.handle(Future.succeededFuture(new PgPreparedQueryImpl(conn, context, cr.result())));
      } else {
        handler.handle(Future.failedFuture(cr.cause()));
      }
    });
    return (C) this;
  }
}
