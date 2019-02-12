package io.reactiverse.mysqlclient.impl;

import io.reactiverse.mysqlclient.impl.protocol.backend.ColumnDefinition;

public class MySQLPreparedStatement {
  public long statementId;
  public ColumnDefinition[] paramsColumnDefinitions;
  public ColumnMetadata columnMetadata;
}
