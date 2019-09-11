package io.vertx.mysqlclient;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.mysqlclient.impl.MySQLConnectionImpl;
import io.vertx.sqlclient.PreparedQuery;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.SqlResult;
import io.vertx.sqlclient.Tuple;

import java.util.stream.Collector;

import static io.vertx.mysqlclient.MySQLConnectOptions.*;

/**
 * A connection to MySQL server.
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
    MySQLConnectionImpl.connect(vertx, connectOptions, handler);
  }

  /**
   * Like {@link #connect(Vertx, MySQLConnectOptions, Handler)} with options build from {@code connectionUri}.
   */
  static void connect(Vertx vertx, String connectionUri, Handler<AsyncResult<MySQLConnection>> handler) {
    connect(vertx, fromUri(connectionUri), handler);
  }

  @Override
  MySQLConnection prepare(String sql, Handler<AsyncResult<PreparedQuery>> handler);

  @Override
  MySQLConnection exceptionHandler(Handler<Throwable> handler);

  @Override
  MySQLConnection closeHandler(Handler<Void> handler);

  @Override
  MySQLConnection preparedQuery(String sql, Handler<AsyncResult<RowSet<Row>>> handler);

  @GenIgnore
  @Override
  <R> MySQLConnection preparedQuery(String sql, Collector<Row, ?, R> collector, Handler<AsyncResult<SqlResult<R>>> handler);

  @Override
  MySQLConnection query(String sql, Handler<AsyncResult<RowSet<Row>>> handler);

  @GenIgnore
  @Override
  <R> MySQLConnection query(String sql, Collector<Row, ?, R> collector, Handler<AsyncResult<SqlResult<R>>> handler);

  @Override
  MySQLConnection preparedQuery(String sql, Tuple arguments, Handler<AsyncResult<RowSet<Row>>> handler);

  @GenIgnore
  @Override
  <R> MySQLConnection preparedQuery(String sql, Tuple arguments, Collector<Row, ?, R> collector, Handler<AsyncResult<SqlResult<R>>> handler);

  /**
   * Send a PING command to check if the server is alive.
   *
   * @param handler the handler notified when the server responses to client
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  MySQLConnection ping(Handler<AsyncResult<Void>> handler);

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
   * Send a STATISTICS command to get a human readable string of the server internal status.
   *
   * @param handler the handler notified with the execution result
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  MySQLConnection getInternalStatistics(Handler<AsyncResult<String>> handler);


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
   * Send a RESET_CONNECTION command to reset the session state.
   *
   * @param handler the handler notified with the execution result
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  MySQLConnection resetConnection(Handler<AsyncResult<Void>> handler);

  /**
   * Send a DEBUG command to dump debug information to the server's stdout.
   *
   * @param handler the handler notified with the execution result
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  MySQLConnection debug(Handler<AsyncResult<Void>> handler);

  /**
   * Send a CHANGE_USER command to change the user of the current connection, this operation will also reset connection state.
   *
   * @param options authentication options, only username, password, database, collation, serverRsaPublicKey and properties will be used.
   * @param handler the handler
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  MySQLConnection changeUser(MySQLConnectOptions options, Handler<AsyncResult<Void>> handler);
}
