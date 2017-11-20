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

import com.julienviet.pgclient.ResultSet;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class PreparedQueryResultHandler implements QueryResultHandler {

  private ResultSet result;
  private boolean suspended;
  private final Handler<AsyncResult<ResultSet>> handler;

  public PreparedQueryResultHandler(Handler<AsyncResult<ResultSet>> handler) {
    this.handler = handler;
  }

  public boolean suspended() {
    return suspended;
  }

  @Override
  public void beginResult(List<String> columnNames) {
    result = new ResultSet().setColumnNames(columnNames).setResults(new ArrayList<>());
  }

  @Override
  public void handleRow(JsonArray row) {
    result.getResults().add(row);
  }

  @Override
  public void endResult(boolean suspended) {
    this.suspended = suspended;
    handler.handle(Future.succeededFuture(result));
  }

  @Override
  public void fail(Throwable cause) {
    handler.handle(Future.failedFuture(cause));
  }

  @Override
  public void end() {
  }
}
