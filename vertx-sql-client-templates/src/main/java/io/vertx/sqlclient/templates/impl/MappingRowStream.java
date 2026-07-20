package io.vertx.sqlclient.templates.impl;

import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowStream;
import io.vertx.sqlclient.desc.RowDescriptor;
import io.vertx.sqlclient.templates.RowMapper;

public class MappingRowStream<T> implements RowStream<T> {

  private final RowStream<Row> delegate;
  private final RowMapper<T> mapper;

  public MappingRowStream(RowStream<Row> delegate, RowMapper<T> mapper) {
    this.delegate = delegate;
    this.mapper = mapper;
  }

  @Override
  public RowDescriptor rowDescriptor() {
    return delegate.rowDescriptor();
  }

  @Override
  public RowStream<T> exceptionHandler(Handler<Throwable> handler) {
    delegate.exceptionHandler(handler);
    return this;
  }

  @Override
  public RowStream<T> handler(Handler<T> handler) {
    if (handler != null) {
      delegate.handler(row -> handler.handle(mapper.map(row)));
    } else {
      delegate.handler(null);
    }
    return this;
  }

  @Override
  public RowStream<T> pause() {
    delegate.pause();
    return this;
  }

  @Override
  public RowStream<T> resume() {
    delegate.resume();
    return this;
  }

  @Override
  public RowStream<T> endHandler(Handler<Void> handler) {
    delegate.endHandler(handler);
    return this;
  }

  @Override
  public RowStream<T> fetch(long l) {
    delegate.fetch(l);
    return this;
  }

  @Override
  public Future<Void> close() {
    return delegate.close();
  }
}
