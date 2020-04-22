package io.vertx.mysqlclient.impl;

import io.vertx.mysqlclient.impl.protocol.ColumnDefinition;
import io.vertx.sqlclient.impl.ParamDesc;
import io.vertx.sqlclient.impl.TupleInternal;

public class MySQLParamDesc extends ParamDesc {
  private final ColumnDefinition[] paramDefinitions;

  public MySQLParamDesc(ColumnDefinition[] paramDefinitions) {
    this.paramDefinitions = paramDefinitions;
  }

  public ColumnDefinition[] paramDefinitions() {
    return paramDefinitions;
  }

  @Override
  public String prepare(TupleInternal values) {
    throw new IllegalStateException();
  }
}
