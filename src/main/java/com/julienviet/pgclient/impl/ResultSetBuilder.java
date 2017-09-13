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

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.sql.ResultSet;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class ResultSetBuilder implements QueryResultHandler {

  private ResultSet result;
  private ResultSet current;
  private boolean completed;
  private final Handler<AsyncResult<ResultSet>> handler;

  public ResultSetBuilder(Handler<AsyncResult<ResultSet>> handler) {
    this.handler = handler;
  }

  @Override
  public void beginResult(List<String> columnNames) {
    ResultSet next = new ResultSet().setColumnNames(columnNames).setResults(new ArrayList<>());
    if (current != null) {
      current.setNext(next);
      current = next;
    } else {
      result = current = next;
    }
  }

  @Override
  public void handleRow(JsonArray row) {
    current.getResults().add(row);
  }

  @Override
  public void endResult(boolean suspended) {
  }

  @Override
  public void end() {
    if (!completed) {
      completed = true;
      handler.handle(Future.succeededFuture(result));
    }
  }

  @Override
  public void fail(Throwable cause) {
    if (!completed) {
      completed = true;
      handler.handle(Future.failedFuture(cause));
    }
  }
}
