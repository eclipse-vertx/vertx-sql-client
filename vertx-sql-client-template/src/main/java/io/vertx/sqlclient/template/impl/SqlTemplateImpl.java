package io.vertx.sqlclient.template.impl;

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
import io.vertx.sqlclient.template.SqlTemplate;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class SqlTemplateImpl<T, R> implements SqlTemplate<T, R> {

  //
  public static final Collector<Row, Void, Void> NULL_COLLECTOR = Collector.of(() -> null, (v, row) -> {}, (a, b) -> null);

  protected final SqlClient client;
  protected final io.vertx.sqlclient.template.impl.SqlTemplate sqlTemplate;
  protected final Function<T, Map<String, Object>> paramsMapper;
  protected Function<PreparedQuery<RowSet<Row>>, PreparedQuery<R>> queryMapper;

  public SqlTemplateImpl(SqlClient client,
                         io.vertx.sqlclient.template.impl.SqlTemplate sqlTemplate,
                         Function<PreparedQuery<RowSet<Row>>, PreparedQuery<R>> foobar,
                         Function<T, Map<String, Object>> paramsMapper) {
    this.client = client;
    this.sqlTemplate = sqlTemplate;
    this.queryMapper = foobar;
    this.paramsMapper = paramsMapper;
  }

  @Override
  public <T1> SqlTemplate<T1, R> mapFrom(Function<T1, Map<String, Object>> mapper) {
    return new SqlTemplateImpl<>(client, sqlTemplate, queryMapper, mapper);
  }

  @Override
  public <T1> SqlTemplate<T1, R> mapFrom(Class<T1> type) {
    return mapFrom(params -> JsonObject.mapFrom(params).getMap());
  }

  @Override
  public <U> SqlTemplate<T, SqlResult<U>> collecting(Collector<Row, ?, U> collector) {
    return new SqlTemplateImpl<>(client, sqlTemplate, query -> query.collecting(collector), paramsMapper);
  }

  @Override
  public <U> SqlTemplate<T, RowSet<U>> mapTo(Class<U> type) {
    return mapTo(row -> {
      JsonObject json = new JsonObject();
      for (int i = 0;i < row.size();i++) {
        json.getMap().put(row.getColumnName(i), row.getValue(i));
      }
      return json.mapTo(type);
    });
  }

  @Override
  public <U> SqlTemplate<T, RowSet<U>> mapTo(Function<Row, U> mapper) {
    return new SqlTemplateImpl<>(client, sqlTemplate, query -> query.mapping(mapper), paramsMapper);
  }

  private Tuple toTuple(T params) {
    return sqlTemplate.mapTuple(paramsMapper.apply(params));
  }

  @Override
  public void execute(T parameters, Handler<AsyncResult<R>> handler) {

    queryMapper
      .apply(client.preparedQuery(sqlTemplate.getSql()))
      .execute(toTuple(parameters), handler);
  }

  @Override
  public Future<R> execute(T params) {
    return queryMapper
      .apply(client.preparedQuery(sqlTemplate.getSql()))
      .execute(toTuple(params));
  }

  @Override
  public void executeBatch(List<T> batch, Handler<AsyncResult<R>> handler) {
    queryMapper.apply(client.preparedQuery(sqlTemplate.getSql()))
      .executeBatch(batch
        .stream()
        .map(this::toTuple)
        .collect(Collectors.toList()), handler);
  }

  @Override
  public Future<R> executeBatch(List<T> batch) {
    return queryMapper.apply(client.preparedQuery(sqlTemplate.getSql()))
      .executeBatch(batch
        .stream()
        .map(this::toTuple)
        .collect(Collectors.toList()));
  }
}
