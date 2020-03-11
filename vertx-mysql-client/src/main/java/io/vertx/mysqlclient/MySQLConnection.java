package io.vertx.mysqlclient;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.mysqlclient.impl.MySQLConnectionImpl;
import io.vertx.sqlclient.PreparedQuery;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.SqlResult;
import io.vertx.sqlclient.Tuple;

import java.util.List;
import java.util.stream.Collector;

import static io.vertx.mysqlclient.MySQLConnectOptions.*;

/**
 * An interface which represents a connection to MySQL server.
 * <P>
 *   The connection object supports all the operations defined in the {@link SqlConnection} interface,
 *   in addition it provides MySQL utility command support:
 *   <ul>
 *     <li>COM_PING</li>
 *     <li>COM_CHANGE_USER</li>
 *     <li>COM_RESET_CONNECTION</li>
 *     <li>COM_DEBUG</li>
 *     <li>COM_INIT_DB</li>
 *     <li>COM_STATISTICS</li>
 *     <li>COM_SET_OPTION</li>
 *   </ul>
 * </P>
 */
@VertxGen
public interface MySQLConnection extends SqlConnection {
  /**
   * Create a connection to MySQL server with the given {@code connectOptions}.
   *
   * @param vertx the vertx instance
   * @param connectOptions the options for the connection
   * @param handler the handler called with the connection or the failure
   */
  static void connect(Vertx vertx, MySQLConnectOptions connectOptions, Handler<AsyncResult<MySQLConnection>> handler) {
    Future<MySQLConnection> fut = connect(vertx, connectOptions);
    if (handler != null) {
      fut.onComplete(handler);
    }
  }

  /**
   * Like {@link #connect(Vertx, MySQLConnectOptions, Handler)} but returns a {@code Future} of the asynchronous result
   */
  static Future<MySQLConnection> connect(Vertx vertx, MySQLConnectOptions connectOptions) {
    return MySQLConnectionImpl.connect(vertx, connectOptions);
  }

  /**
   * Like {@link #connect(Vertx, MySQLConnectOptions, Handler)} with options build from {@code connectionUri}.
   */
  static void connect(Vertx vertx, String connectionUri, Handler<AsyncResult<MySQLConnection>> handler) {
    connect(vertx, fromUri(connectionUri), handler);
  }

  /**
   * Like {@link #connect(Vertx, String, Handler)} but returns a {@code Future} of the asynchronous result
   */
  static Future<MySQLConnection> connect(Vertx vertx, String connectionUri) {
    return connect(vertx, fromUri(connectionUri));
  }

  /**
   * {@inheritDoc}
   */
  @Fluent
  @Override
  MySQLConnection prepare(String sql, Handler<AsyncResult<PreparedQuery>> handler);

  /**
   * {@inheritDoc}
   */
  @Fluent
  @Override
  MySQLConnection exceptionHandler(Handler<Throwable> handler);

  /**
   * {@inheritDoc}
   */
  @Fluent
  @Override
  MySQLConnection closeHandler(Handler<Void> handler);

  /**
   * {@inheritDoc}
   */
  @Fluent
  @Override
  MySQLConnection preparedQuery(String sql, Handler<AsyncResult<RowSet<Row>>> handler);

  /**
   * {@inheritDoc}
   */
  @Fluent
  @GenIgnore
  @Override
  <R> MySQLConnection preparedQuery(String sql, Collector<Row, ?, R> collector, Handler<AsyncResult<SqlResult<R>>> handler);

  /**
   * {@inheritDoc}
   */
  @Fluent
  @Override
  MySQLConnection query(String sql, Handler<AsyncResult<RowSet<Row>>> handler);

  /**
   * {@inheritDoc}
   */
  @Fluent
  @GenIgnore
  @Override
  <R> MySQLConnection query(String sql, Collector<Row, ?, R> collector, Handler<AsyncResult<SqlResult<R>>> handler);

  /**
   * {@inheritDoc}
   */
  @Fluent
  @Override
  MySQLConnection preparedQuery(String sql, Tuple arguments, Handler<AsyncResult<RowSet<Row>>> handler);

  /**
   * {@inheritDoc}
   */
  @Fluent
  @GenIgnore
  @Override
  <R> MySQLConnection preparedQuery(String sql, Tuple arguments, Collector<Row, ?, R> collector, Handler<AsyncResult<SqlResult<R>>> handler);

  /**
   * {@inheritDoc}
   */
  @Fluent
  @Override
  MySQLConnection preparedBatch(String sql, List<Tuple> batch, Handler<AsyncResult<RowSet<Row>>> handler);

  /**
   * {@inheritDoc}
   */
  @Fluent
  @GenIgnore
  @Override
  <R> MySQLConnection preparedBatch(String sql, List<Tuple> batch, Collector<Row, ?, R> collector, Handler<AsyncResult<SqlResult<R>>> handler);

  /**
   * Send a PING command to check if the server is alive.
   *
   * @param handler the handler notified when the server responses to client
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  MySQLConnection ping(Handler<AsyncResult<Void>> handler);

  /**
   * Like {@link #ping(Handler)} but returns a {@code Future} of the asynchronous result
   */
  Future<Void> ping();

  /**
   * Send a INIT_DB command to change the default schema of the connection.
   *
   * @param schemaName name of the schema to change to
   * @param handler the handler notified with the execution result
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  MySQLConnection specifySchema(String schemaName, Handler<AsyncResult<Void>> handler);

  /**
   * Like {@link #specifySchema(String, Handler)} but returns a {@code Future} of the asynchronous result
   */
  Future<Void> specifySchema(String schemaName);

  /**
   * Send a STATISTICS command to get a human readable string of the server internal status.
   *
   * @param handler the handler notified with the execution result
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  MySQLConnection getInternalStatistics(Handler<AsyncResult<String>> handler);

  /**
   * Like {@link #getInternalStatistics(Handler)} but returns a {@code Future} of the asynchronous result
   */
  Future<String> getInternalStatistics();


  /**
   * Send a SET_OPTION command to set options for the current connection.
   *
   * @param option the options to set
   * @param handler the handler notified with the execution result
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  MySQLConnection setOption(MySQLSetOption option, Handler<AsyncResult<Void>> handler);

  /**
   * Like {@link #setOption(MySQLSetOption, Handler)} but returns a {@code Future} of the asynchronous result
   */
  Future<Void> setOption(MySQLSetOption option);

  /**
   * Send a RESET_CONNECTION command to reset the session state.
   *
   * @param handler the handler notified with the execution result
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  MySQLConnection resetConnection(Handler<AsyncResult<Void>> handler);

  /**
   * Like {@link #resetConnection(Handler)} but returns a {@code Future} of the asynchronous result
   */
  Future<Void> resetConnection();

  /**
   * Send a DEBUG command to dump debug information to the server's stdout.
   *
   * @param handler the handler notified with the execution result
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  MySQLConnection debug(Handler<AsyncResult<Void>> handler);

  /**
   * Like {@link #debug(Handler)} but returns a {@code Future} of the asynchronous result
   */
  Future<Void> debug();

  /**
   * Send a CHANGE_USER command to change the user of the current connection, this operation will also reset connection state.
   *
   * @param options authentication options
   * @param handler the handler
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  MySQLConnection changeUser(MySQLAuthOptions options, Handler<AsyncResult<Void>> handler);

  /**
   * Like {@link #changeUser(MySQLAuthOptions, Handler)} but returns a {@code Future} of the asynchronous result
   */
  Future<Void> changeUser(MySQLAuthOptions options);
}
