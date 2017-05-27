package com.julienviet.pgclient.impl;

import com.julienviet.pgclient.PgResultSet;
import com.julienviet.pgclient.Query;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;

import java.util.List;
import java.util.UUID;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class QueryImpl implements Query {

  private static final int READY = 0, IN_PROGRESS = 1, SUSPENDED = 2;

  final PreparedStatementImpl ps;
  final List<Object> params;
  private int fetch;
  private int status;
  private String portal;

  QueryImpl(PreparedStatementImpl ps, List<Object> params) {
    this.ps = ps;
    this.params = params;
  }

  @Override
  public Query fetch(int size) {
    this.fetch = size;
    return this;
  }

  @Override
  public void execute(Handler<AsyncResult<PgResultSet>> handler) {
    if (status == IN_PROGRESS) {
      throw new IllegalStateException();
    }
    Handler<AsyncResult<PgResultSet>> completionHandler = ar -> {
      if (ar.succeeded()) {
        if (ar.result().isComplete()) {
          status = READY;
        } else {
          status = SUSPENDED;
        }
        handler.handle(Future.succeededFuture(ar.result()));
      } else {
        status = READY;
        handler.handle(Future.failedFuture(ar.cause()));
      }
    };
    if (status == READY) {
      status = IN_PROGRESS;
      portal = fetch > 0 ? UUID.randomUUID().toString() : "";
      ps.conn.schedule(new PreparedQueryCommand(ps, params, fetch, portal, false, completionHandler));
    } else {
      status = IN_PROGRESS;
      ps.conn.schedule(new PreparedQueryCommand(ps, params, fetch, portal, true, completionHandler));
    }
  }
}
