package io.vertx.sqlclient.templates;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowStream;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.impl.SqlClientInternal;
import io.vertx.sqlclient.templates.impl.SqlTemplateStreamImpl;

import java.util.Map;

/**
 * An SQL template for streaming query results.
 *
 * <p>Stream templates execute queries using named instead of positional parameters and return results
 * as a {@link RowStream} that reads rows progressively using a cursor with a configurable fetch size.
 *
 * @param <I> the input parameters type
 * @param <T> the row output type
 */
@VertxGen
public interface SqlTemplateStream<I, T> {

  /**
   * Create an SQL template for streaming query results consuming map parameters and returning {@link Row}.
   *
   * <p>The returned stream template uses a cursor with the given {@code fetchSize} to read rows progressively.
   *
   * @param client the wrapped SQL connection
   * @param template the template query string
   * @param fetchSize the cursor fetch size
   * @return the template
   */
  static SqlTemplateStream<Map<String, Object>, Row> forStream(SqlConnection client, String template, int fetchSize) {
    SqlClientInternal clientInternal = (SqlClientInternal) client;
    io.vertx.sqlclient.templates.impl.SqlTemplate sqlTemplate = io.vertx.sqlclient.templates.impl.SqlTemplate.create(clientInternal, template);
    return new SqlTemplateStreamImpl<>(client, sqlTemplate, sqlTemplate::mapTuple, null, fetchSize);
  }

  /**
   * @return the computed SQL for this template
   */
  String sql();

  /**
   * Set a parameters user defined mapping function.
   *
   * <p> At query execution, the {@code mapper} is called to map the parameters object
   * to a {@link io.vertx.sqlclient.Tuple} that configures the prepared query.
   *
   * @param mapper the mapping function
   * @return a new template
   */
  <T2> SqlTemplateStream<T2, T> mapFrom(TupleMapper<T2> mapper);

  /**
   * Set a parameters user defined class mapping.
   *
   * <p> At query execution, the parameters object is mapped to a {@code Map<String, Object>} that
   * configures the prepared query.
   *
   * <p> This feature relies on {@link io.vertx.core.json.JsonObject#mapFrom} feature. This likely requires
   * to use Jackson databind in the project.
   *
   * @param type the mapping type
   * @return a new template
   */
  default <T2> SqlTemplateStream<T2, T> mapFrom(Class<T2> type) {
    return mapFrom(TupleMapper.mapper(type));
  }

  /**
   * Set a row user defined mapping function.
   *
   * <p>When rows are emitted by the stream, the {@code mapper} function is called to map each {@link Row}
   * to the target type.
   *
   * @param mapper the mapping function
   * @return a new template
   */
  <U> SqlTemplateStream<I, U> mapTo(RowMapper<U> mapper);

  /**
   * Set a row user defined mapping function.
   *
   * <p>When rows are emitted by the stream, resulting rows are mapped to {@code type} instances.
   *
   * <p> This feature relies on {@link io.vertx.core.json.JsonObject#mapFrom} feature. This likely requires
   * to use Jackson databind in the project.
   *
   * @param type the mapping type
   * @return a new template
   */
  default <U> SqlTemplateStream<I, U> mapTo(Class<U> type) {
    return mapTo(RowMapper.mapper(type));
  }

  /**
   * Returns a new template, using the specified {@code connection}.
   *
   * @param connection the connection that will execute requests
   * @return a new template
   */
  SqlTemplateStream<I, T> withConnection(SqlConnection connection);

  /**
   * Execute the query with the {@code parameters}
   *
   * @param params the query parameters
   * @return a future notified with the result
   */
  Future<RowStream<T>> execute(I params);

}
