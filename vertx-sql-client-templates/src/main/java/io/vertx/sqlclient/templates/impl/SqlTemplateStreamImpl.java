package io.vertx.sqlclient.templates.impl;

import io.vertx.core.Future;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowStream;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.Tuple;
import io.vertx.sqlclient.templates.RowMapper;
import io.vertx.sqlclient.templates.SqlTemplateStream;
import io.vertx.sqlclient.templates.TupleMapper;

import java.util.function.Function;

public class SqlTemplateStreamImpl<I, T> implements SqlTemplateStream<I, T> {

  private final SqlConnection connection;
  private final SqlTemplate sqlTemplate;
  private final Function<I, Tuple> tupleMapper;
  private final RowMapper<T> rowMapper;
  private final int fetchSize;

  public SqlTemplateStreamImpl(SqlConnection connection, SqlTemplate sqlTemplate, Function<I, Tuple> tupleMapper, RowMapper<T> rowMapper, int fetchSize) {
    this.connection = connection;
    this.sqlTemplate = sqlTemplate;
    this.tupleMapper = tupleMapper;
    this.rowMapper = rowMapper;
    this.fetchSize = fetchSize;
  }

  @Override
  public String sql() {
    return sqlTemplate.getSql();
  }

  @Override
  public <T2> SqlTemplateStream<T2, T> mapFrom(TupleMapper<T2> mapper) {
    return new SqlTemplateStreamImpl<>(connection, sqlTemplate, params -> mapper.map(sqlTemplate, sqlTemplate.numberOfParams(), params), rowMapper, fetchSize);
  }

  @Override
  public <U> SqlTemplateStream<I, U> mapTo(RowMapper<U> mapper) {
    return new SqlTemplateStreamImpl<>(connection, sqlTemplate, tupleMapper, mapper, fetchSize);
  }

  @Override
  public SqlTemplateStream<I, T> withConnection(SqlConnection connection) {
    return new SqlTemplateStreamImpl<>(connection, sqlTemplate, tupleMapper, rowMapper, fetchSize);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Future<RowStream<T>> execute(I params) {
    Tuple tuple = tupleMapper.apply(params);
    return connection.prepare(sqlTemplate.getSql()).map(ps -> {
      RowStream<Row> stream = ps.createStream(fetchSize, tuple);
      if (rowMapper != null) {
        return new MappingRowStream<>(stream, rowMapper);
      } else {
        return (RowStream<T>) stream;
      }
    });
  }
}
