package io.vertx.clickhouse.clickhousenative.impl.codec;

import io.vertx.clickhouse.clickhousenative.impl.ClickhouseNativeRowDesc;
import io.vertx.sqlclient.impl.ParamDesc;
import io.vertx.sqlclient.impl.PreparedStatement;
import io.vertx.sqlclient.impl.RowDesc;
import io.vertx.sqlclient.impl.TupleInternal;

public class ClickhouseNativePreparedStatement implements PreparedStatement {
  private final String sql;
  private final ClickhouseNativeParamDesc paramDesc;
  private final ClickhouseNativeRowDesc rowDesc;
  private final QueryParsers.QueryType queryType;
  private final boolean sentQuery;

  public ClickhouseNativePreparedStatement(String sql, ClickhouseNativeParamDesc paramDesc, ClickhouseNativeRowDesc rowDesc,
                                           QueryParsers.QueryType queryType, boolean sentQuery) {
    this.sql = sql;
    this.paramDesc = paramDesc;
    this.rowDesc = rowDesc;
    this.queryType = queryType;
    this.sentQuery = sentQuery;
  }

  @Override
  public ParamDesc paramDesc() {
    return paramDesc;
  }

  @Override
  public RowDesc rowDesc() {
    return rowDesc;
  }

  @Override
  public String sql() {
    return sql;
  }

  @Override
  public String prepare(TupleInternal values) {
    return null;
  }

  public QueryParsers.QueryType queryType() {
    return queryType;
  }

  public boolean isSentQuery() {
    return sentQuery;
  }
}
