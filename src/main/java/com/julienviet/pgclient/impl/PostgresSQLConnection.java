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
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.sql.SQLRowStream;
import io.vertx.ext.sql.TransactionIsolation;
import io.vertx.ext.sql.UpdateResult;

import java.util.List;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class PostgresSQLConnection implements SQLConnection {

  private static final Logger log = LoggerFactory.getLogger(PostgresSQLConnection.class);

  private final DbConnection conn;

  public PostgresSQLConnection(DbConnection conn) {
    this.conn = conn;
  }

  @Override
  public SQLConnection setAutoCommit(boolean autoCommit, Handler<AsyncResult<Void>> handler) {
    throw new UnsupportedOperationException();
  }

  @Override
  public SQLConnection execute(String s, Handler<AsyncResult<Void>> handler) {
    throw new UnsupportedOperationException();
  }

  @Override
  public SQLConnection query(String s, Handler<AsyncResult<ResultSet>> handler) {
    conn.schedule(new QueryCommand(s, new ResultSetBuilder(handler)));
    return this;
  }

  @Override
  public SQLConnection queryStream(String sql, Handler<AsyncResult<SQLRowStream>> handler) {
    throw new UnsupportedOperationException();
  }

  @Override
  public SQLConnection queryWithParams(String s, JsonArray jsonArray, Handler<AsyncResult<ResultSet>> handler) {
    CommandBase cmd = new PreparedQueryCommand(s, jsonArray.getList(), new PreparedQueryResultHandler(ar -> {
      handler.handle(ar.map(results -> results));
    }));
    conn.schedule(cmd);
    return this;
  }

  @Override
  public SQLConnection queryStreamWithParams(String s, JsonArray jsonArray, Handler<AsyncResult<SQLRowStream>> handler) {
    throw new UnsupportedOperationException();
  }

  @Override
  public SQLConnection update(String s, Handler<AsyncResult<UpdateResult>> handler) {
    conn.schedule(new UpdateCommand(s, handler));
    return this;
  }

  @Override
  public SQLConnection updateWithParams(String sql, JsonArray jsonArray, Handler<AsyncResult<UpdateResult>> handler) {
    conn.schedule(new PreparedUpdateWithParamsCommand(sql, jsonArray.getList(), ar -> {
      handler.handle(ar.map(results -> results));
    }));
    return this;
  }

  @Override
  public SQLConnection call(String s, Handler<AsyncResult<ResultSet>> handler) {
    throw new UnsupportedOperationException();
  }

  @Override
  public SQLConnection callWithParams(String s, JsonArray jsonArray, JsonArray out, Handler<AsyncResult<ResultSet>> handler) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void close(Handler<AsyncResult<Void>> handler) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void close() {
    throw new UnsupportedOperationException();
  }

  @Override
  public SQLConnection commit(Handler<AsyncResult<Void>> handler) {
    throw new UnsupportedOperationException();
  }

  @Override
  public SQLConnection rollback(Handler<AsyncResult<Void>> handler) {
    throw new UnsupportedOperationException();
  }

  @Override
  public SQLConnection setQueryTimeout(int i) {
    throw new UnsupportedOperationException();
  }

  @Override
  public SQLConnection batch(List<String> list, Handler<AsyncResult<List<Integer>>> handler) {
    throw new UnsupportedOperationException();
  }

  @Override
  public SQLConnection batchWithParams(String s, List<JsonArray> list, Handler<AsyncResult<List<Integer>>> handler) {
    throw new UnsupportedOperationException();
  }

  @Override
  public SQLConnection batchCallableWithParams(String s, List<JsonArray> list, List<JsonArray> list1, Handler<AsyncResult<List<Integer>>> handler) {
    throw new UnsupportedOperationException();
  }

  @Override
  public SQLConnection setTransactionIsolation(TransactionIsolation isolation, Handler<AsyncResult<Void>> handler) {
    if(isolation == TransactionIsolation.NONE) {
      handler.handle(Future.failedFuture("None transaction isolation is not supported"));
    } else {
      conn.schedule(new PreparedTxUpdateCommand(isolation, handler));
    }
    return this;
  }

  @Override
  public SQLConnection getTransactionIsolation(Handler<AsyncResult<TransactionIsolation>> handler) {
    conn.schedule(new PreparedTxQueryCommand(handler));
    return this;
  }
}
