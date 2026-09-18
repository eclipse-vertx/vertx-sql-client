package io.vertx.sqlclient.templates.impl;

import io.vertx.core.Future;
import io.vertx.sqlclient.*;
import io.vertx.sqlclient.templates.RowMapper;
import io.vertx.sqlclient.templates.TupleMapper;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collector;

public class CursorSqlTemplateImpl<I> implements io.vertx.sqlclient.templates.SqlTemplate<I, Cursor> {

  private final SqlConnection connection;
  private final SqlTemplate sqlTemplate;
  private final Function<I, Tuple> tupleMapper;

  public CursorSqlTemplateImpl(SqlConnection connection, SqlTemplate sqlTemplate, Function<I, Tuple> tupleMapper) {
    this.connection = connection;
    this.sqlTemplate = sqlTemplate;
    this.tupleMapper = tupleMapper;
  }

  @Override
  public String sql() {
    return sqlTemplate.getSql();
  }

  @Override
  public <T> io.vertx.sqlclient.templates.SqlTemplate<T, Cursor> mapFrom(TupleMapper<T> mapper) {
    return new CursorSqlTemplateImpl<>(connection, sqlTemplate, params -> mapper.map(sqlTemplate, sqlTemplate.numberOfParams(), params));
  }

  @Override
  public <U> io.vertx.sqlclient.templates.SqlTemplate<I, RowSet<U>> mapTo(RowMapper<U> mapper) {
    throw new UnsupportedOperationException("mapTo is not supported on cursor templates, use forStream instead");
  }

  @Override
  public <U> io.vertx.sqlclient.templates.SqlTemplate<I, RowSet<U>> mapTo(Class<U> type) {
    throw new UnsupportedOperationException("mapTo is not supported on cursor templates, use forStream instead");
  }

  @Override
  public <U> io.vertx.sqlclient.templates.SqlTemplate<I, SqlResult<U>> collecting(Collector<Row, ?, U> collector) {
    throw new UnsupportedOperationException("collecting is not supported on cursor templates");
  }

  @Override
  public io.vertx.sqlclient.templates.SqlTemplate<I, Cursor> withClient(SqlClient client) {
    if (!(client instanceof SqlConnection)) {
      throw new IllegalArgumentException("Cursor templates require a SqlConnection");
    }
    return new CursorSqlTemplateImpl<>((SqlConnection) client, sqlTemplate, tupleMapper);
  }

  @Override
  public Future<Cursor> execute(I params) {
    Tuple tuple = tupleMapper.apply(params);
    return connection.prepare(sqlTemplate.getSql()).map(ps -> ps.cursor(tuple));
  }

  @Override
  public Future<Cursor> executeBatch(List<I> batch) {
    throw new UnsupportedOperationException("executeBatch is not supported on cursor templates");
  }
}
