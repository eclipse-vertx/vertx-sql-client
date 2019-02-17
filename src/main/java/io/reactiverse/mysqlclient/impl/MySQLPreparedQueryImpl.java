package io.reactiverse.mysqlclient.impl;

import io.reactiverse.mysqlclient.MySQLPreparedQuery;
import io.reactiverse.mysqlclient.impl.codec.encoder.PreparedStatementExecuteCommand;
import io.reactiverse.pgclient.PgResult;
import io.reactiverse.pgclient.PgRowSet;
import io.reactiverse.pgclient.Row;
import io.reactiverse.pgclient.Tuple;
import io.vertx.core.*;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collector;

public class MySQLPreparedQueryImpl implements MySQLPreparedQuery {
  private final MySQLSocketConnection conn;
  private final Context context;

  private MySQLPreparedStatement mySQLPreparedStatement;

  public MySQLPreparedQueryImpl(MySQLSocketConnection conn, Context context, MySQLPreparedStatement mySQLPreparedStatement) {
    this.conn = conn;
    this.context = context;
    this.mySQLPreparedStatement = mySQLPreparedStatement;
  }

  @Override
  public MySQLPreparedQuery execute(Tuple args, Handler<AsyncResult<PgRowSet>> handler) {
    return execute(args, false, MySQLRowSetImpl.FACTORY, MySQLRowSetImpl.COLLECTOR, handler);
  }

  @Override
  public <R> MySQLPreparedQuery execute(Tuple args, Collector<Row, ?, R> collector, Handler<AsyncResult<PgResult<R>>> handler) {
    return execute(args, true, MySQLResultImpl::new, collector, handler);
  }

  private <R1, R2 extends MySQLResultBase<R1, R2>, R3 extends PgResult<R1>> MySQLPreparedQuery execute(
    Tuple args,
    boolean singleton,
    Function<R1, R2> factory,
    Collector<Row, ?, R1> collector,
    Handler<AsyncResult<R3>> handler) {
    MySQLResultBuilder<R1, R2, R3> b = new MySQLResultBuilder<>(factory, handler);
    return execute(args, 0, null, false, singleton, collector, b, b);
  }

  <A, R> MySQLPreparedQuery execute(Tuple args,
                                    int fetch,
                                    String portal,
                                    boolean suspended,
                                    boolean singleton,
                                    Collector<Row, A, R> collector,
                                    QueryResultHandler<R> resultHandler,
                                    Handler<AsyncResult<Boolean>> handler) {
    if (context == Vertx.currentContext()) {
      String msg = mySQLPreparedStatement.prepare((List<Object>) args);
      if (msg != null) {
        handler.handle(Future.failedFuture(msg));
      } else {
        PreparedStatementExecuteCommand cmd = new PreparedStatementExecuteCommand<>(
          mySQLPreparedStatement,
          args,
          singleton,
          collector,
          resultHandler);
        cmd.setHandler(handler);
        conn.schedule(cmd);
      }
    } else {
      context.runOnContext(v -> execute(args, fetch, portal, suspended, singleton, collector, resultHandler, handler));
    }
    return this;
  }

  @Override
  public void close() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void close(Handler<AsyncResult<Void>> completionHandler) {
    throw new UnsupportedOperationException();
  }
}
