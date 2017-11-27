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
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class PrepareCommand extends CommandBase {

  private final String sql;
  private final Handler<AsyncResult<PreparedStatement>> handler;
  private String statement;
  private CompletableFuture<PreparedStatement> cf;
  private Map<String, CompletableFuture<PreparedStatement>> cache;
  private ParameterDescription parameterDesc;
  private RowDescription rowDesc;
  private Future<PreparedStatement> fut;

  PrepareCommand(String sql, Handler<AsyncResult<PreparedStatement>> handler) {
    this.sql = sql;
    this.handler = handler;
    this.fut= Future.future();
  }

  @Override
  void exec(SocketConnection conn) {
    conn.decodeQueue.add(new DecodeContext(false, null, null));
    cache = conn.psCache;
    if (cache != null) {
      CompletableFuture<PreparedStatement> cached = cache.get(sql);
      if (cached == null) {
        cached = new CompletableFuture<>();
        cache.put(sql, cached);
        statement = UUID.randomUUID().toString();
        cf = cached;
      } else {
        cached.whenComplete((ps, err) -> {
          if (err == null) {
            handler.handle(Future.succeededFuture(ps));
          } else {
            handler.handle(Future.failedFuture(err));
          }
        });
        return;
      }
    } else {
      cf = new CompletableFuture<>();
      statement = "";
    }
    conn.writeMessage(new Parse(sql).setStatement(statement));
    conn.writeMessage(new Describe().setStatement(statement));
    conn.writeMessage(Sync.INSTANCE);
    fut.setHandler(ar -> {
      handler.handle(ar);
      if (cf != null) {
        if (ar.succeeded()) {
          cf.complete(ar.result());
        } else {
          if (cache != null) {
            cache.remove(sql, cf);
          }
          cf.completeExceptionally(ar.cause());
        }
      }
    });
  }

  @Override
  public void handleMessage(InboundMessage msg) {
    if (msg.getClass() == ParseComplete.class) {
      // Ok
    } else if (msg.getClass() == ParameterDescription.class) {
      parameterDesc = (ParameterDescription) msg;
    } else if (msg.getClass() == RowDescription.class) {
      rowDesc = (RowDescription) msg;
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
