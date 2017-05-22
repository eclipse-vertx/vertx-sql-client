package com.julienviet.pgclient.impl;

import com.julienviet.pgclient.PostgresConnection;
import com.julienviet.pgclient.PreparedStatement;
import com.julienviet.pgclient.Result;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
class PostgresConnectionImpl implements PostgresConnection {
  private DbConnection dbConnection;

  public PostgresConnectionImpl(DbConnection dbConnection) {
    this.dbConnection = dbConnection;
  }

  @Override
  public void execute(String sql, Handler<AsyncResult<Result>> handler) {
    CommandBase cmd = new QueryCommand(sql, handler);
    dbConnection.schedule(cmd);
  }

  @Override
  public void prepareAndExecute(String sql, Object param, Handler<AsyncResult<Result>> handler) {
    prepareAndExecute(sql, Arrays.asList(param), handler);
  }

  @Override
  public void prepareAndExecute(String sql, Object param1, Object param2, Handler<AsyncResult<Result>> handler) {
    prepareAndExecute(sql, Arrays.asList(param1, param2), handler);
  }

  @Override
  public void prepareAndExecute(String sql, Object param1, Object param2, Object param3,
                                Handler<AsyncResult<Result>> handler) {
    prepareAndExecute(sql, Arrays.asList(param1, param2, param3), handler);
  }

  @Override
  public void prepareAndExecute(String sql, Object param1, Object param2, Object param3, Object param4,
                                Handler<AsyncResult<Result>> handler) {
    prepareAndExecute(sql, Arrays.asList(param1, param2, param3, param4), handler);
  }

  @Override
  public void prepareAndExecute(String sql, Object param1, Object param2, Object param3, Object param4, Object param5,
                                Handler<AsyncResult<Result>> handler) {
    prepareAndExecute(sql, Arrays.asList(param1, param2, param3, param4, param5), handler);
  }

  @Override
  public void prepareAndExecute(String sql, Object param1, Object param2, Object param3, Object param4, Object param5,
                                Object param6, Handler<AsyncResult<Result>> handler) {
    prepareAndExecute(sql, Arrays.asList(param1, param2, param3, param4, param5, param6), handler);
  }

  @Override
  public void prepareAndExecute(String sql, List<Object> params, Handler<AsyncResult<Result>> handler) {
    PreparedStatementImpl ps = new PreparedStatementImpl(dbConnection, sql, "");
    CommandBase cmd = new PreparedQueryCommand(ps, Collections.singletonList(params), ar -> {
      handler.handle(ar.map(results -> results.get(0)));
    });
    dbConnection.schedule(cmd);
  }

  @Override
  public void closeHandler(Handler<Void> handler) {
    dbConnection.closeHandler(handler);
  }

  @Override
  public void exceptionHandler(Handler<Throwable> handler) {
    dbConnection.exceptionHandler(handler);
  }

  @Override
  public void close() {
    dbConnection.doClose();
  }

  @Override
  public PreparedStatement prepare(String sql) {
    return new PreparedStatementImpl(dbConnection, sql, java.util.UUID.randomUUID().toString());
  }
}
