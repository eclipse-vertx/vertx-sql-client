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

import io.reactiverse.pgclient.PgException;
import io.reactiverse.pgclient.impl.codec.decoder.DecodeContext;
import io.reactiverse.pgclient.impl.codec.decoder.InboundMessage;
import io.reactiverse.pgclient.impl.codec.decoder.message.*;
import io.reactiverse.pgclient.impl.codec.encoder.message.Describe;
import io.reactiverse.pgclient.impl.codec.encoder.message.Parse;
import io.reactiverse.pgclient.impl.codec.encoder.message.Sync;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;

import java.util.Map;

public class PrepareStatementCommand extends CommandBase<PreparedStatement> {

  final String sql;
  long statement; // 0 means unamed statement otherwise CString
  SocketConnection.CachedPreparedStatement cached;
  private ParameterDescription parameterDesc;
  private RowDescription rowDesc;
  final Handler<AsyncResult<PreparedStatement>> handler;

  PrepareStatementCommand(String sql, Handler<AsyncResult<PreparedStatement>> handler) {
    super(null); // Not pretty but well, that's fine for now
    this.sql = sql;
    this.handler = handler;
    super.handler = ar -> {
      handler.handle(ar);
      if (cached != null) {
        cached.fut.handle(ar);
      }
    };
  }

  @Override
  void exec(SocketConnection conn) {
    conn.decodeQueue.add(new DecodeContext(null, null, null));
    conn.writeMessage(new Parse(sql).setStatement(statement));
    conn.writeMessage(new Describe().setStatement(statement));
    conn.writeMessage(Sync.INSTANCE);
  }

  @Override
  public void handleMessage(InboundMessage msg) {
    if (msg.getClass() == ParseComplete.class) {
      // Response to Parse
    } else if (msg.getClass() == ParameterDescription.class) {
      // Response to Describe
      parameterDesc = (ParameterDescription) msg;
    } else if (msg.getClass() == RowDescription.class) {
      // Response to Describe
      rowDesc = (RowDescription) msg;
    } else if (msg.getClass() == NoData.class) {
      // Response to Describe
    } else if (msg.getClass() == ErrorResponse.class) {
      ErrorResponse error = (ErrorResponse) msg;
      failure = new PgException(error);
    } else {
      if (msg.getClass() == ReadyForQuery.class) {
        result = new PreparedStatement(sql, statement, parameterDesc, rowDesc);
      }
      super.handleMessage(msg);
    }
  }

  @Override
  void fail(Throwable err) {
    Future<PreparedStatement> failure = Future.failedFuture(err);
    handler.handle(failure);
    if (cached != null) {
      cached.fut.handle(failure);
    }
  }
}
