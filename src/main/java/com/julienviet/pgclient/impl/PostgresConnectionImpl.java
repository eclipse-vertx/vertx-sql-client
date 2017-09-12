package com.julienviet.pgclient.impl;

import com.julienviet.pgclient.PgConnection;
import com.julienviet.pgclient.PgPreparedStatement;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.UpdateResult;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
class PostgresConnectionImpl implements PgConnection {

  private DbConnection dbConnection;
  private final Map<String, PgPreparedStatement> psCache;

  public PostgresConnectionImpl(DbConnection dbConnection, boolean cachePreparedStatements) {
    this.dbConnection = dbConnection;
    this.psCache = cachePreparedStatements ? new ConcurrentHashMap<>() : null;
  }

  @Override
  public PgConnection execute(String sql, Handler<AsyncResult<ResultSet>> handler) {
    dbConnection.schedule(new QueryCommand(sql, new ResultSetBuilder(handler)));
    return this;
  }

  @Override
  public PgConnection update(String sql, Handler<AsyncResult<UpdateResult>> handler) {
    dbConnection.schedule(new UpdateCommand(sql, handler));
    return this;
  }

  @Override
  public PgConnection query(String sql, Handler<AsyncResult<ResultSet>> handler) {
    dbConnection.schedule(new QueryCommand(sql, new ResultSetBuilder(handler)));
    return this;
  }

  @Override
  public PgConnection prepareAndQuery(String sql, List<Object> params, Handler<AsyncResult<ResultSet>> handler) {
    dbConnection.schedule(new PreparedQueryCommand(sql, params, new PreparedQueryResultHandler(ar -> {
      if (ar.succeeded()) {
        handler.handle(Future.succeededFuture(ar.result()));
      } else {
        handler.handle(Future.failedFuture(ar.cause()));
      }
    })));
    return this;
  }

  @Override
  public PgConnection prepareAndExecute(String sql, List<Object> params, Handler<AsyncResult<UpdateResult>> handler) {
    CommandBase cmd = new PreparedUpdateCommand(sql, Collections.singletonList(params), ar -> {
      handler.handle(ar.map(results -> results.get(0)));
    });
    dbConnection.schedule(cmd);
    return this;
  }

  @Override
  public PgConnection closeHandler(Handler<Void> handler) {
    dbConnection.closeHandler(handler);
    return this;
  }

  @Override
  public PgConnection exceptionHandler(Handler<Throwable> handler) {
    dbConnection.exceptionHandler(handler);
    return this;
  }

  @Override
  public void close() {
    dbConnection.doClose();
  }

  @Override
  public PgPreparedStatement prepare(String sql) {
    if (psCache != null) {
      return psCache.computeIfAbsent(sql, this::createCachedPreparedStatement);
    } else {
      return new PreparedStatementImpl(dbConnection, sql, UUID.randomUUID().toString(), false);
    }
  }

  private PreparedStatementImpl createCachedPreparedStatement(String sql) {
    return new PreparedStatementImpl(dbConnection, sql, UUID.randomUUID().toString(), true);
  }
}
