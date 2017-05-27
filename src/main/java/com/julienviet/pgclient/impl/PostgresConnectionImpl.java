package com.julienviet.pgclient.impl;

import com.julienviet.pgclient.PgConnection;
import com.julienviet.pgclient.PgPreparedStatement;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.UpdateResult;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
class PostgresConnectionImpl implements PgConnection {
  private DbConnection dbConnection;

  public PostgresConnectionImpl(DbConnection dbConnection) {
    this.dbConnection = dbConnection;
  }

  @Override
  public void execute(String sql, Handler<AsyncResult<ResultSet>> handler) {
    dbConnection.schedule(new QueryCommand(sql, handler));
  }

  @Override
  public void update(String sql, Handler<AsyncResult<UpdateResult>> handler) {
    dbConnection.schedule(new UpdateCommand(sql, handler));
  }

  @Override
  public void query(String sql, Handler<AsyncResult<ResultSet>> handler) {
    dbConnection.schedule(new QueryCommand(sql, handler));
  }

  @Override
  public void prepareAndQuery(String sql, Object param, Handler<AsyncResult<ResultSet>> handler) {
    prepareAndQuery(sql, Arrays.asList(param), handler);
  }

  @Override
  public void prepareAndQuery(String sql, Object param1, Object param2, Handler<AsyncResult<ResultSet>> handler) {
    prepareAndQuery(sql, Arrays.asList(param1, param2), handler);
  }

  @Override
  public void prepareAndQuery(String sql, Object param1, Object param2, Object param3,
                              Handler<AsyncResult<ResultSet>> handler) {
    prepareAndQuery(sql, Arrays.asList(param1, param2, param3), handler);
  }

  @Override
  public void prepareAndQuery(String sql, Object param1, Object param2, Object param3, Object param4,
                              Handler<AsyncResult<ResultSet>> handler) {
    prepareAndQuery(sql, Arrays.asList(param1, param2, param3, param4), handler);
  }

  @Override
  public void prepareAndQuery(String sql, Object param1, Object param2, Object param3, Object param4, Object param5,
                              Handler<AsyncResult<ResultSet>> handler) {
    prepareAndQuery(sql, Arrays.asList(param1, param2, param3, param4, param5), handler);
  }

  @Override
  public void prepareAndQuery(String sql, Object param1, Object param2, Object param3, Object param4, Object param5,
                              Object param6, Handler<AsyncResult<ResultSet>> handler) {
    prepareAndQuery(sql, Arrays.asList(param1, param2, param3, param4, param5, param6), handler);
  }

  @Override
  public void prepareAndQuery(String sql, List<Object> params, Handler<AsyncResult<ResultSet>> handler) {
    PreparedStatementImpl ps = new PreparedStatementImpl(dbConnection, sql, "");
    CommandBase cmd = new PreparedQueryCommand(ps, params, ar -> {
      if (ar.succeeded()) {
        handler.handle(Future.succeededFuture(ar.result()));
      } else {
        handler.handle(Future.failedFuture(ar.cause()));
      }
    });
    dbConnection.schedule(cmd);
  }

  @Override
  public void prepareAndExecute(String sql, Object param, Handler<AsyncResult<UpdateResult>> handler) {
    prepareAndExecute(sql, Arrays.asList(param), handler);
  }

  @Override
  public void prepareAndExecute(String sql, Object param1, Object param2, Handler<AsyncResult<UpdateResult>> handler) {
    prepareAndExecute(sql, Arrays.asList(param1, param2), handler);
  }

  @Override
  public void prepareAndExecute(String sql, Object param1, Object param2, Object param3, Handler<AsyncResult<UpdateResult>> handler) {
    prepareAndExecute(sql, Arrays.asList(param1, param2, param3), handler);
  }

  @Override
  public void prepareAndExecute(String sql, Object param1, Object param2, Object param3, Object param4, Handler<AsyncResult<UpdateResult>> handler) {
    prepareAndExecute(sql, Arrays.asList(param1, param2, param3, param4), handler);
  }

  @Override
  public void prepareAndExecute(String sql, Object param1, Object param2, Object param3, Object param4, Object param5, Handler<AsyncResult<UpdateResult>> handler) {
    prepareAndExecute(sql, Arrays.asList(param1, param2, param3, param4, param5), handler);
  }

  @Override
  public void prepareAndExecute(String sql, Object param1, Object param2, Object param3, Object param4, Object param5, Object param6, Handler<AsyncResult<UpdateResult>> handler) {
    prepareAndExecute(sql, Arrays.asList(param1, param2, param3, param4, param5, param6), handler);
  }

  @Override
  public void prepareAndExecute(String sql, List<Object> params, Handler<AsyncResult<UpdateResult>> handler) {
    PreparedStatementImpl ps = new PreparedStatementImpl(dbConnection, sql, "");
    CommandBase cmd = new PreparedUpdateCommand(ps, Collections.singletonList(params), ar -> {
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
  public PgPreparedStatement prepare(String sql) {
    return new PreparedStatementImpl(dbConnection, sql, java.util.UUID.randomUUID().toString());
  }
}
