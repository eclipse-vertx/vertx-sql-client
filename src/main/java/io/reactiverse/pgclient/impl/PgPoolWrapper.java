package io.reactiverse.pgclient.impl;
import io.reactiverse.pgclient.*;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

import java.util.List;
import java.util.stream.Collector;

/**
 * Wraps a pool client with the {@link PgPoolHolder} in order to keep track of the references.
 *
 * @author <a href="https://github.com/mystdeim">Roman Novikov</a>
 */
public class PgPoolWrapper implements PgPool {

  private final PgPoolHolder holder;
  private final PgPool pool;

  public PgPoolWrapper(PgPoolHolder holder) {
    this.holder = holder;
    this.pool = holder.client();
  }

  @Override
  public PgPool preparedQuery(String sql, Handler<AsyncResult<PgRowSet>> handler) {
    return pool.preparedQuery(sql, handler);
  }

  @Override
  public <R> PgPool preparedQuery(String sql, Collector<Row, ?, R> collector, Handler<AsyncResult<PgResult<R>>> handler) {
    return pool.preparedQuery(sql, collector, handler);
  }

  @Override
  public PgPool query(String sql, Handler<AsyncResult<PgRowSet>> handler) {
    return pool.query(sql, handler);
  }

  @Override
  public <R> PgPool query(String sql, Collector<Row, ?, R> collector, Handler<AsyncResult<PgResult<R>>> handler) {
    return pool.query(sql, collector, handler);
  }

  @Override
  public PgPool preparedQuery(String sql, Tuple arguments, Handler<AsyncResult<PgRowSet>> handler) {
    return pool.preparedQuery(sql, arguments, handler);
  }

  @Override
  public <R> PgPool preparedQuery(String sql, Tuple arguments, Collector<Row, ?, R> collector, Handler<AsyncResult<PgResult<R>>> handler) {
    return pool.preparedQuery(sql, arguments, collector, handler);
  }

  @Override
  public PgPool preparedBatch(String sql, List<Tuple> batch, Handler<AsyncResult<PgRowSet>> handler) {
    return pool.preparedBatch(sql, batch, handler);
  }

  @Override
  public <R> PgPool preparedBatch(String sql, List<Tuple> batch, Collector<Row, ?, R> collector, Handler<AsyncResult<PgResult<R>>> handler) {
    return pool.preparedBatch(sql, batch, collector, handler);
  }

  @Override
  public void getConnection(Handler<AsyncResult<PgConnection>> handler) {
    pool.getConnection(handler);
  }

  @Override
  public void begin(Handler<AsyncResult<PgTransaction>> handler) {
    pool.begin(handler);
  }

  @Override
  public void close() {
    holder.close();
  }
}
