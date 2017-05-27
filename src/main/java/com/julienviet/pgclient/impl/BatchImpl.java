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
  public PgBatch add(Object param1) {
    return add(Collections.singletonList(param1));
  }

  @Override
  public PgBatch add(Object param1, Object param2) {
    return add(Arrays.asList(param1, param2));
  }

  @Override
  public PgBatch add(Object param1, Object param2, Object param3) {
    return add(Arrays.asList(param1, param2, param3));
  }

  @Override
  public PgBatch add(Object param1, Object param2, Object param3, Object param4) {
    return add(Arrays.asList(param1, param2, param3, param4));
  }

  @Override
  public PgBatch add(Object param1, Object param2, Object param3, Object param4, Object param5) {
    return add(Arrays.asList(param1, param2, param3, param4, param5));
  }

  @Override
  public PgBatch add(Object param1, Object param2, Object param3, Object param4, Object param5, Object param6) {
    return add(Arrays.asList(param1, param2, param3, param4, param5, param6));
  }

  @Override
  public void execute(Handler<AsyncResult<List<UpdateResult>>> resultHandler) {
    ps.conn.schedule(new PreparedUpdateCommand(ps, values, resultHandler));
  }
}
