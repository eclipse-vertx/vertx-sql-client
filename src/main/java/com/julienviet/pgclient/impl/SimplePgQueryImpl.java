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

import com.julienviet.pgclient.PgQuery;
import com.julienviet.pgclient.ResultSet;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

public class SimplePgQueryImpl implements PgQuery, QueryResultHandler {

  private final Handler<CommandBase> execHandler;
  private final String sql;
  private Handler<ResultSet> resultHandler;
  private Handler<Throwable> exceptionHandler;
  private Handler<Void> endHandler;

  public SimplePgQueryImpl(String sql, Handler<CommandBase> execHandler) {
    this.execHandler = execHandler;
    this.sql = sql;
  }

  @Override
  public PgQuery fetch(int size) {
    return this;
  }

  @Override
  public void close(Handler<AsyncResult<Void>> completionHandler) {

  }

  @Override
  public PgQuery exceptionHandler(Handler<Throwable> handler) {
    exceptionHandler = handler;
    return this;
  }

  @Override
  public PgQuery handler(Handler<ResultSet> handler) {
    resultHandler = handler;
    execHandler.handle(new SimpleQueryCommand(sql, this));
    return this;
  }

  @Override
  public PgQuery pause() {
    return this;
  }

  @Override
  public PgQuery resume() {
    return this;
  }

  @Override
  public PgQuery endHandler(Handler<Void> handler) {
    endHandler = handler;
    return this;
  }

  @Override
  public void result(ResultSet result, boolean suspended) {
    Handler<ResultSet> handler = resultHandler;
    if (handler != null) {
      handler.handle(result);
    }
  }

  @Override
  public void fail(Throwable cause) {
    Handler<Throwable> handler = exceptionHandler;
    if (handler != null) {
      handler.handle(cause);
    }
  }

  @Override
  public void end() {
    Handler<Void> handler = endHandler;
    if (handler != null) {
      handler.handle(null);
    }
  }

}
