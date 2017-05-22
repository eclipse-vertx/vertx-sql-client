package com.julienviet.pgclient.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
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

  private final DbConnection conn;

  public PostgresSQLConnection(DbConnection conn) {
    this.conn = conn;
  }

  @Override
  public SQLConnection setAutoCommit(boolean b, Handler<AsyncResult<Void>> handler) {
    throw new UnsupportedOperationException();
  }

  @Override
  public SQLConnection execute(String s, Handler<AsyncResult<Void>> handler) {
    throw new UnsupportedOperationException();
  }

  @Override
  public SQLConnection query(String s, Handler<AsyncResult<ResultSet>> handler) {
    throw new UnsupportedOperationException();
  }

  @Override
  public SQLConnection queryStream(String s, Handler<AsyncResult<SQLRowStream>> handler) {
    throw new UnsupportedOperationException();
  }

  @Override
  public SQLConnection queryWithParams(String s, JsonArray jsonArray, Handler<AsyncResult<ResultSet>> handler) {
    throw new UnsupportedOperationException();
  }

  @Override
  public SQLConnection queryStreamWithParams(String s, JsonArray jsonArray, Handler<AsyncResult<SQLRowStream>> handler) {
    throw new UnsupportedOperationException();
  }

  @Override
  public SQLConnection update(String s, Handler<AsyncResult<UpdateResult>> handler) {
    throw new UnsupportedOperationException();
  }

  @Override
  public SQLConnection updateWithParams(String s, JsonArray jsonArray, Handler<AsyncResult<UpdateResult>> handler) {
    throw new UnsupportedOperationException();
  }

  @Override
  public SQLConnection call(String s, Handler<AsyncResult<ResultSet>> handler) {
    throw new UnsupportedOperationException();
  }

  @Override
  public SQLConnection callWithParams(String s, JsonArray jsonArray, JsonArray jsonArray1, Handler<AsyncResult<ResultSet>> handler) {
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
  public SQLConnection setTransactionIsolation(TransactionIsolation transactionIsolation, Handler<AsyncResult<Void>> handler) {
    throw new UnsupportedOperationException();
  }

  @Override
  public SQLConnection getTransactionIsolation(Handler<AsyncResult<TransactionIsolation>> handler) {
    throw new UnsupportedOperationException();
  }
}
