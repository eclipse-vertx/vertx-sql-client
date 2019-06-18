package io.vertx.mysqlclient.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.SqlResult;
import io.vertx.sqlclient.Transaction;
import io.vertx.sqlclient.Tuple;
import io.vertx.sqlclient.impl.Connection;
import io.vertx.sqlclient.impl.PoolBase;
import io.vertx.sqlclient.impl.SqlConnectionImpl;

import java.util.List;
import java.util.stream.Collector;

public class MySQLPoolImpl extends PoolBase<MySQLPoolImpl> implements MySQLPool {
  private final MySQLConnectionFactory factory;

  public MySQLPoolImpl(Context context, boolean closeVertx, MySQLConnectOptions connectOptions, PoolOptions poolOptions) {
    super(context, closeVertx, poolOptions);
    this.factory = new MySQLConnectionFactory(context, Vertx.currentContext() != null, connectOptions);
  }

  @Override
  public void connect(Handler<AsyncResult<Connection>> completionHandler) {
    factory.connect(completionHandler);
  }

  @Override
  protected SqlConnectionImpl wrap(Context context, Connection conn) {
    return new MySQLConnectionImpl(factory, context, conn);
  }

  @Override
  public void begin(Handler<AsyncResult<Transaction>> handler) {
    throw new UnsupportedOperationException("Transaction is not supported for now");
  }

  @Override
  public MySQLPoolImpl preparedBatch(String sql, List<Tuple> batch, Handler<AsyncResult<RowSet>> handler) {
    throw new UnsupportedOperationException("PreparedBatch is not supported for now");
  }

  @Override
  public <R> MySQLPoolImpl preparedBatch(String sql, List<Tuple> batch, Collector<Row, ?, R> collector, Handler<AsyncResult<SqlResult<R>>> handler) {
    throw new UnsupportedOperationException("PreparedBatch is not supported for now");
  }

  @Override
  protected void doClose() {
    factory.close();
    super.doClose();
  }
}
