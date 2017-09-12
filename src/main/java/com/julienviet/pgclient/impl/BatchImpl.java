package com.julienviet.pgclient.impl;

import com.julienviet.pgclient.PgBatch;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.ext.sql.UpdateResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class BatchImpl implements PgBatch {

  private final PreparedStatementImpl ps;
  private final ArrayList<List<Object>> values = new ArrayList<>();

  BatchImpl(PreparedStatementImpl ps) {
    this.ps = ps;
  }

  @Override
  public PgBatch add(List<Object> params) {
    values.add(params);
    return this;
  }

  @Override
  public void execute(Handler<AsyncResult<List<UpdateResult>>> resultHandler) {
    ps.update(values, resultHandler);
  }
}
