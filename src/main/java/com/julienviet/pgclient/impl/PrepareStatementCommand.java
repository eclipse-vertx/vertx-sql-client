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

import com.julienviet.pgclient.PgException;
import com.julienviet.pgclient.codec.decoder.DecodeContext;
import com.julienviet.pgclient.codec.decoder.InboundMessage;
import com.julienviet.pgclient.codec.decoder.message.*;
import com.julienviet.pgclient.codec.encoder.message.Describe;
import com.julienviet.pgclient.codec.encoder.message.Parse;
import com.julienviet.pgclient.codec.encoder.message.Sync;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;

import java.util.Map;
import java.util.function.Function;

public class PrepareStatementCommand extends CommandBase {

  final String sql;
  private long statement; // 0 means unamed statement otherwise CString
  private ParameterDescription parameterDesc;
  private RowDescription rowDesc;
  private Future<PreparedStatement> fut;
  private final Function<AsyncResult<PreparedStatement>, CommandBase> supplier;

  PrepareStatementCommand(String sql, Function<AsyncResult<PreparedStatement>, CommandBase> supplier) {
    this.sql = sql;
    this.supplier = supplier;
  }

  @Override
  void foo(SocketConnection conn) {

    Future<PreparedStatement> bilto = Future.<PreparedStatement>future().setHandler(ar -> {
      CommandBase command = supplier.apply(ar);
      if (command != null) {
        conn.schedule(command);
      }
    });

    Map<String, SocketConnection.CachedPreparedStatement> psCache = conn.psCache;
    if (psCache != null) {
      SocketConnection.CachedPreparedStatement cached = psCache.get(sql);
      if (cached == null) {
        statement = conn.psSeq.next();
        cached = new SocketConnection.CachedPreparedStatement();
        Future<PreparedStatement> futal = cached.fut;
        psCache.put(sql, cached);
        fut = Future.<PreparedStatement>future().setHandler(ar -> {
          futal.handle(ar);
          bilto.handle(ar);
        });
        super.foo(conn);
      } else {
        cached.get(ar -> {
          bilto.handle(ar);
        });
      }
    } else {
      fut = bilto;
      super.foo(conn);
    }
  }

  @Override
  void exec(SocketConnection conn) {
    conn.decodeQueue.add(new DecodeContext(false, null, null, null));
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
      fut.tryFail(new PgException(error));
    } else {
      if (msg.getClass() == ReadyForQuery.class) {
        fut.tryComplete(new PreparedStatement(sql, statement, parameterDesc, rowDesc));
      }
      super.handleMessage(msg);
    }
  }

  @Override
  void fail(Throwable err) {
    fut.tryFail(err);
  }
}
