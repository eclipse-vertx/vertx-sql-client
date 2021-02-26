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

  public ClickhouseNativePreparedStatement(String sql, ClickhouseNativeParamDesc paramDesc, ClickhouseNativeRowDesc rowDesc) {
    this.sql = sql;
    this.paramDesc = paramDesc;
    this.rowDesc = rowDesc;
  }

  @Override
  public ParamDesc paramDesc() {
    return null;
  }

  @Override
  public RowDesc rowDesc() {
    return null;
  }

  @Override
  public String sql() {
    return null;
  }

  @Override
  public String prepare(TupleInternal values) {
    return null;
  }
}
