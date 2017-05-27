package com.julienviet.pgclient.impl;

import com.julienviet.pgclient.Query;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.ext.sql.ResultSet;

import java.util.List;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class QueryImpl implements Query {

  final PreparedStatementImpl ps;
  final List<Object> params;

  QueryImpl(PreparedStatementImpl ps, List<Object> params) {
    this.ps = ps;
    this.params = params;
  }

  @Override
  public Query setLimit(int limit) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean done() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void execute(Handler<AsyncResult<ResultSet>> handler) {
    ps.conn.schedule(new PreparedQueryCommand(ps, params, ar -> {
      if (ar.succeeded()) {
        handler.handle(Future.succeededFuture(ar.result()));
      } else {
        handler.handle(Future.failedFuture(ar.cause()));
      }
    }));
  }
}
