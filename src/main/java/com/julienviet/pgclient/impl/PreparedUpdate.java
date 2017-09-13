package com.julienviet.pgclient.impl;

import com.julienviet.pgclient.PgUpdate;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.ext.sql.UpdateResult;

public class PreparedUpdate implements PgUpdate {

  final PreparedStatementImpl ps;

  PreparedUpdate(PreparedStatementImpl ps) {
    this.ps = ps;
  }

  @Override
  public void execute(Handler<AsyncResult<UpdateResult>> handler) {
    ps.update(handler);
  }
}
