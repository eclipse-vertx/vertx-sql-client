package com.julienviet.pgclient.impl;

import com.julienviet.pgclient.PgUpdate;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.ext.sql.UpdateResult;

import java.util.List;

public class PreparedUpdateWithParams implements PgUpdate {

  final PreparedStatementImpl ps;
  final List<Object> params;

  PreparedUpdateWithParams(PreparedStatementImpl ps, List<Object> params) {
    this.ps = ps;
    this.params = params;
  }

  @Override
  public void execute(Handler<AsyncResult<UpdateResult>> handler) {
    ps.update(params, handler);
  }
}
