package io.vertx.sqlclient.templates.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.PreparedQuery;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.SqlResult;
import io.vertx.sqlclient.Tuple;
import io.vertx.sqlclient.templates.RowMapper;
import io.vertx.sqlclient.templates.TupleMapper;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class SqlTemplateImpl<I, R> implements io.vertx.sqlclient.templates.SqlTemplate<I, R> {

  //
  public static final Collector<Row, Void, Void> NULL_COLLECTOR = Collector.of(() -> null, (v, row) -> {}, (a, b) -> null);

  protected final SqlClient client;
  protected final SqlTemplate sqlTemplate;
  protected final Function<I, Tuple> tupleMapper;
  protected Function<PreparedQuery<RowSet<Row>>, PreparedQuery<R>> queryMapper;

  public SqlTemplateImpl(SqlClient client,
                         SqlTemplate sqlTemplate,
                         Function<PreparedQuery<RowSet<Row>>,
                         PreparedQuery<R>> queryMapper,
                         Function<I, Tuple> tupleMapper) {
    this.client = client;
    this.sqlTemplate = sqlTemplate;
    this.queryMapper = queryMapper;
    this.tupleMapper = tupleMapper;
  }

  @Override
  public <T> io.vertx.sqlclient.templates.SqlTemplate<T, R> mapFrom(TupleMapper<T> mapper) {
    return new SqlTemplateImpl<>(client, sqlTemplate, queryMapper, params -> mapper.map(sqlTemplate, sqlTemplate.numberOfParams(), params));
  }

  @Override
  public <U> io.vertx.sqlclient.templates.SqlTemplate<I, SqlResult<U>> collecting(Collector<Row, ?, U> collector) {
    return new SqlTemplateImpl<>(client, sqlTemplate, query -> query.collecting(collector), tupleMapper);
  }

  @Override
  public <U> io.vertx.sqlclient.templates.SqlTemplate<I, RowSet<U>> mapTo(Class<U> type) {
    return mapTo(row -> {
      JsonObject json = new JsonObject();
      for (int i = 0;i < row.size();i++) {
        json.getMap().put(row.getColumnName(i), row.getValue(i));
      }
      return json.mapTo(type);
    });
  }

  @Override
  public <U> io.vertx.sqlclient.templates.SqlTemplate<I, RowSet<U>> mapTo(RowMapper<U> mapper) {
    return new SqlTemplateImpl<>(client, sqlTemplate, query -> query.mapping(mapper::map), tupleMapper);
  }

  @Override
  public void execute(I parameters, Handler<AsyncResult<R>> handler) {

    queryMapper
      .apply(client.preparedQuery(sqlTemplate.getSql()))
      .execute(tupleMapper.apply(parameters))
      .onComplete(handler);
  }

  @Override
  public Future<R> execute(I params) {
    return queryMapper
      .apply(client.preparedQuery(sqlTemplate.getSql()))
      .execute(tupleMapper.apply(params));
  }

  @Override
  public void executeBatch(List<I> batch, Handler<AsyncResult<R>> handler) {
    queryMapper.apply(client.preparedQuery(sqlTemplate.getSql()))
      .executeBatch(batch
        .stream()
        .map(tupleMapper)
        .collect(Collectors.toList()))
      .onComplete(handler);
  }

  @Override
  public Future<R> executeBatch(List<I> batch) {
    return queryMapper.apply(client.preparedQuery(sqlTemplate.getSql()))
      .executeBatch(batch
        .stream()
        .map(tupleMapper)
        .collect(Collectors.toList()));
  }
}
