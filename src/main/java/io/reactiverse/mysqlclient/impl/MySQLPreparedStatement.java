package io.reactiverse.mysqlclient.impl;

import io.reactiverse.mysqlclient.impl.protocol.backend.ColumnDefinition;

import java.util.List;

public class MySQLPreparedStatement {
  public long statementId;
  public ColumnDefinition[] paramsColumnDefinitions;
  public ColumnMetadata columnMetadata;

  public String prepare(List<Object> args) {
    //FIXME should reuse previous command
    return null;
  }
}
