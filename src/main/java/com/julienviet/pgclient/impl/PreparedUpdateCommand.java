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

import com.julienviet.pgclient.codec.Message;
import com.julienviet.pgclient.codec.decoder.message.BindComplete;
import com.julienviet.pgclient.codec.decoder.message.NoData;
import com.julienviet.pgclient.codec.decoder.message.ParameterDescription;
import com.julienviet.pgclient.codec.decoder.message.ParseComplete;
import com.julienviet.pgclient.codec.decoder.message.ReadyForQuery;
import com.julienviet.pgclient.codec.encoder.message.Bind;
import com.julienviet.pgclient.codec.encoder.message.Describe;
import com.julienviet.pgclient.codec.encoder.message.Execute;
import com.julienviet.pgclient.codec.encoder.message.Parse;
import com.julienviet.pgclient.codec.encoder.message.Sync;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.ext.sql.UpdateResult;

import java.util.ArrayList;
import java.util.List;

import static com.julienviet.pgclient.codec.util.Util.*;

/**
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */

class PreparedUpdateCommand extends UpdateCommandBase {


  final boolean parse;
  final String sql;
  final String stmt;
  final List<List<Object>> paramsList;
  final Handler<AsyncResult<List<UpdateResult>>> handler;
  private ArrayList<UpdateResult> results;
  private Throwable failure;

  PreparedUpdateCommand(String sql, List<List<Object>> paramsList, Handler<AsyncResult<List<UpdateResult>>> handler) {
    this(true, sql, "", paramsList, handler);
  }

  PreparedUpdateCommand(boolean parse, String sql, String stmt, List<List<Object>> paramsList, Handler<AsyncResult<List<UpdateResult>>> handler) {
    this.parse = parse;
    this.sql = sql;
    this.stmt = stmt;
    this.paramsList = paramsList;
    this.handler = handler;
    this.results = new ArrayList<>(paramsList.size()); // Should reuse the paramsList for this as it's already allocated
  }

  @Override
  void exec(DbConnection conn, Handler<Void> handler) {
    doneHandler = handler;
    if (parse) {
      conn.writeMessage(new Parse(sql).setStatement(stmt));
    }
    for (List<Object> params : paramsList) {
      conn.writeMessage(new Bind().setParamValues(paramValues(params)).setStatement(stmt));
      conn.writeMessage(new Describe().setStatement(stmt));
      conn.writeMessage(new Execute().setRowCount(0));
    }
    conn.writeMessage(Sync.INSTANCE);
  }

  @Override
  public void handleMessage(Message msg) {
    if (msg.getClass() == ReadyForQuery.class) {
      doneHandler.handle(null);
      if (failure != null) {
        handler.handle(Future.failedFuture(failure));
      } else {
        handler.handle(Future.succeededFuture(results));
      }
    } else if (msg.getClass() == ParameterDescription.class) {
    } else if (msg.getClass() == NoData.class) {
    } else if (msg.getClass() == ParseComplete.class) {
    } else if (msg.getClass() == BindComplete.class) {
    } else {
      super.handleMessage(msg);
    }
  }

  @Override
  void handleResult(UpdateResult result) {
    results.add(result);
  }

  @Override
  void fail(Throwable cause) {
    failure = cause;
  }
}
