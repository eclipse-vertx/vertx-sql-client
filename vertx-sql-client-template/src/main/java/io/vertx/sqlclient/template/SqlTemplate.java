package io.vertx.sqlclient.template;

import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.SqlResult;
import io.vertx.sqlclient.impl.SqlClientInternal;
import io.vertx.sqlclient.template.impl.SqlTemplateImpl;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collector;

/**
 * An SQL template.
 *
 * <p>SQL templates are useful for interacting with a relational database.
 *
 * <p>SQL templates execute queries using named instead of positional parameters. Query execution is parameterized
 * by a map of string to objects instead of a {@link io.vertx.sqlclient.Tuple}. The default source of parameters is a
 * simple map, a user defined mapping can be used instead given it maps the source to such a map.
 *
 * <p>SQL template default results are {@link Row}, a user defined mapping can be used instead, mapping the
 * result set {@link Row} to a {@link RowSet} of the mapped type.
 */
@VertxGen
public interface SqlTemplate<I, R> {

  /**
   * Create an SQL template for query purpose consuming map parameters and returning {@link Row}.
   *
   * @param client the wrapped SQL client
   * @param template the template query string
   * @return the template
   */
  static SqlTemplate<Map<String, Object>, RowSet<Row>> forQuery(SqlClient client, String template) {
    io.vertx.sqlclient.template.impl.SqlTemplate sqlTemplate = io.vertx.sqlclient.template.impl.SqlTemplate.create((SqlClientInternal) client, template);
    return new SqlTemplateImpl<>(client, sqlTemplate, Function.identity(), Function.identity());
  }

  /**
   * Create an SQL template for query purpose consuming map parameters and returning void.
   *
   * @param client the wrapped SQL client
   * @param template the template update string
   * @return the template
   */
  static SqlTemplate<Map<String, Object>, SqlResult<Void>> forUpdate(SqlClient client, String template) {
    io.vertx.sqlclient.template.impl.SqlTemplate sqlTemplate = io.vertx.sqlclient.template.impl.SqlTemplate.create((SqlClientInternal) client, template);
    return new SqlTemplateImpl<>(client, sqlTemplate, query -> query.collecting(SqlTemplateImpl.NULL_COLLECTOR), Function.identity());
  }

  /**
   * Set a parameters user defined mapping function.
   *
   * <p> At query execution, the {@code mapper} function is called to map the parameters object
   * to a {@code Map<String, Object>} that configures the prepared query.
   *
   * @param mapper the mapping function
   * @return a new template
   */
  <T> SqlTemplate<T, R> mapFrom(Function<T, Map<String, Object>> mapper);

  /**
   * Set a parameters user defined class mapping.
   *
   * <p> At query execution, the parameters object is is mapped to a {@code Map<String, Object>} that
   * configures the prepared query.
   *
   * <p> This feature relies on {@link io.vertx.core.json.JsonObject#mapFrom} feature. This likely requires
   * to use Jackson databind in the project.
   *
   * @param type the mapping type
   * @return a new template
   */
  <T> SqlTemplate<T, R> mapFrom(Class<T> type);

  /**
   * Set a row user defined mapping function.
   *
   * <p> When the query execution completes, the {@code mapper} function is called to map the resulting
   * rows to objects.
   *
   * @param mapper the mapping function
   * @return a new template
   */
  <U> SqlTemplate<I, RowSet<U>> mapTo(Function<Row, U> mapper);

  /**
   * Set a row user defined mapping function.
   *
   * <p> When the query execution completes, resulting rows are mapped to {@code type} instances.
   *
   * <p> This feature relies on {@link io.vertx.core.json.JsonObject#mapFrom} feature. This likely requires
   * to use Jackson databind in the project.
   *
   * @param type the mapping type
   * @return a new template
   */
  <U> SqlTemplate<I, RowSet<U>> mapTo(Class<U> type);

  /**
   * Set a collector that will process the output and produce a custom result.
   *
   * @param collector the collector
   * @return a new template
   */
  @GenIgnore
  <U> SqlTemplate<I, SqlResult<U>> collecting(Collector<Row, ?, U> collector);

  /**
   * Execute the query with the {@code parameters}
   *
   * @param parameters the query parameters
   * @param handler the result handler
   */
  void execute(I parameters, Handler<AsyncResult<R>> handler);

  /**
   * Like {@link #execute(Object, Handler)} but returns a {@code Future} of the asynchronous result
   */
  Future<R> execute(I params);

  /**
   * Execute a batch query with the {@code batch}.
   *
   * <p>Each item in the batch is mapped to a tuple.
   *
   * @param batch the batch
   * @param handler the result handler
   */
  void executeBatch(List<I> batch, Handler<AsyncResult<R>> handler);

  /**
   * Like {@link #executeBatch(List, Handler)} but returns a {@code Future} of the asynchronous result
   */
  Future<R> executeBatch(List<I> batch);

}
